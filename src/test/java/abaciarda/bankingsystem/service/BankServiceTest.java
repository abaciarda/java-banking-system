package abaciarda.bankingsystem.service;

import java.sql.*;
import java.util.List;

import abaciarda.bankingsystem.models.*;
import abaciarda.bankingsystem.types.AccountOperationResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

class BankServiceTest {
    private static Connection testConnection;
    private BankService bankService;
    private AccountService accountService;
    private TransactionService transactionService;
    private LoanService loanService;
    private UserService userService;

    @BeforeAll
    static void setupDatabase() throws SQLException {
        testConnection = DriverManager.getConnection(
                "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
                "sa",
                ""
        );

        try (Statement stmt = testConnection.createStatement()) {

            stmt.execute("DROP TABLE IF EXISTS transactions");
            stmt.execute("DROP TABLE IF EXISTS loans");
            stmt.execute("DROP TABLE IF EXISTS accounts");
            stmt.execute("DROP TABLE IF EXISTS users");

            stmt.execute("CREATE TABLE users (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(50) NOT NULL, surname VARCHAR(50) NOT NULL, ssn VARCHAR(20) UNIQUE NOT NULL, password VARCHAR(255) NOT NULL)");

            stmt.execute("CREATE TABLE accounts (id INT AUTO_INCREMENT PRIMARY KEY, user_id INT NOT NULL, iban VARCHAR(50) UNIQUE NOT NULL, balance DECIMAL(15,2) DEFAULT 0.00, type VARCHAR(20) NOT NULL, interest_rate DECIMAL(5,2) DEFAULT 0.00, maturity_date BIGINT DEFAULT 0, FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)");

            stmt.execute("CREATE TABLE transactions (id INT AUTO_INCREMENT PRIMARY KEY, account_id INT NOT NULL, type VARCHAR(20) NOT NULL, amount DECIMAL(15,2) NOT NULL, balance_after DECIMAL(15,2) NOT NULL, created_at BIGINT NOT NULL, FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE)");

            stmt.execute("CREATE TABLE loans (id INT AUTO_INCREMENT PRIMARY KEY, user_id INT NOT NULL, principal_amount DOUBLE, interest_rate DOUBLE, total_debt DOUBLE, remaining_debt DOUBLE, created_at BIGINT, status VARCHAR(20), FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)");
        }
    }

    @BeforeEach
    void setup() throws SQLException {
        accountService = new AccountService(testConnection);
        transactionService = new TransactionService(testConnection);
        loanService = new LoanService(testConnection);

        bankService = new BankService(
                accountService,
                transactionService,
                userService,
                loanService
        );

        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("DELETE FROM transactions");
            stmt.execute("DELETE FROM loans");
            stmt.execute("DELETE FROM accounts");
            stmt.execute("DELETE FROM users");
        }

        try (PreparedStatement stmt = testConnection.prepareStatement(
                "INSERT INTO users (id, name, surname, ssn, password) VALUES (?, ?, ?, ?, ?)"
        )) {
            stmt.setInt(1, 1);
            stmt.setString(2, "Test");
            stmt.setString(3, "User");
            stmt.setString(4, "12345678901");
            stmt.setString(5, "hashedpass");
            stmt.executeUpdate();
        }
    }

    @Test
    void testDeposit() throws SQLException {
        User user = new User(1, "Test", "User", "12345678901");

        accountService.createAccount(user, AccountType.CHECKING, 0);
        Account account = accountService.getAccounts(user).get(0);

        AccountOperationResponse response = bankService.deposit(user, account.getId(), 500);

        assertTrue(response.isSuccess());

        Account updated = accountService.getAccounts(user).get(0);

        assertEquals(500, updated.getBalance());

        List<Transaction> transactionList = transactionService.getTransactions(updated);

        assertEquals(1, transactionList.size());
        assertEquals(TransactionType.DEPOSIT, transactionList.get(0).getType());
    }

    @Test
    void testDepositNegativeAmount() throws SQLException {
        User user = new User(1, "Test", "User", "12345678901");

        accountService.createAccount(user, AccountType.CHECKING, 0);
        Account account = accountService.getAccounts(user).get(0);

        AccountOperationResponse response = bankService.deposit(user, account.getId(), -500);

        assertFalse(response.isSuccess());

        Account updated = accountService.getAccounts(user).get(0);

        assertEquals(0, updated.getBalance());

        List<Transaction> transactionList = transactionService.getTransactions(updated);

        assertEquals(0, transactionList.size());
    }

    @Test
    void testWithdrawNegativeAmount() throws SQLException {
        User user = new User(1, "Test", "User", "12345678901");

        accountService.createAccount(user, AccountType.CHECKING, 0);

        Account account = accountService.getAccounts(user).get(0);

        AccountOperationResponse response = account.withdraw(-500);

        assertFalse(response.isSuccess());

        assertEquals(0, account.getBalance());
    }

    @Test
    void testTransfer() throws SQLException {
        User user = new User(1, "Test", "User", "12345678901");

        accountService.createAccount(user, AccountType.CHECKING, 0);
        accountService.createAccount(user, AccountType.CHECKING, 0);

        List<Account> accounts = accountService.getAccounts(user);

        Account sender = accounts.get(0);
        Account receiver = accounts.get(1);

        sender.deposit(1000);
        accountService.updateBalance(sender);

        AccountOperationResponse response = bankService.transfer(user, sender, receiver.getIban(), 300);

        assertTrue(response.isSuccess());

        Account updatedSender = accountService.getAccountById(user, sender.getId());
        Account updatedReceiver = accountService.getAccountById(user, receiver.getId());

        assertEquals(700, updatedSender.getBalance());
        assertEquals(300, updatedReceiver.getBalance());

        assertEquals(2, (transactionService.getTransactions(updatedSender).size() + transactionService.getTransactions(updatedReceiver).size()));
    }

    @Test
    void testRequestLoan() throws SQLException {
        User user = new User(1, "Test", "User", "12345678901");

        accountService.createAccount(user, AccountType.CHECKING, 0);
        Account account = accountService.getAccounts(user).get(0);

        AccountOperationResponse response = bankService.requestLoan(user, account.getId(), 1000);

        assertTrue(response.isSuccess());

        Account updated = accountService.getAccountById(user, account.getId());

        assertEquals(1000, updated.getBalance());

        List<Loan> loans = loanService.getActiveLoans(user);
        assertEquals(1, loans.size());
        assertEquals(LoanStatus.ACTIVE, loans.get(0).getStatus());
    }

    @Test
    void testWithdrawInsufficientBalance() throws SQLException {
        User user = new User(1, "Test", "User", "12345678901");

        accountService.createAccount(user, AccountType.CHECKING, 0);
        Account account = accountService.getAccounts(user).get(0);

        AccountOperationResponse response = bankService.withdraw(user, account.getId(), 100);

        assertFalse(response.isSuccess());

        Account updated = accountService.getAccountById(user, account.getId());

        assertEquals(0, updated.getBalance());
        assertEquals(0, transactionService.getTransactions(updated).size());
    }

    @Test
    void testDepositInvalidAccount() throws SQLException {
        User user = new User(1, "Test", "User", "12345678901");

        AccountOperationResponse response = bankService.deposit(user, 129, 100);

        assertFalse(response.isSuccess());
    }

    @Test
    void testTransferInsufficientBalance() throws SQLException {
        User user = new User(1, "Test", "User", "12345678901");

        accountService.createAccount(user, AccountType.CHECKING, 0);
        accountService.createAccount(user, AccountType.CHECKING, 0);

        Account sender = accountService.getAccounts(user).get(0);
        Account receiver = accountService.getAccounts(user).get(1);

        AccountOperationResponse response = bankService.transfer(user, sender, receiver.getIban(), 1253);

        assertFalse(response.isSuccess());

        assertEquals(0, transactionService.getTransactions(sender).size());
        assertEquals(0, transactionService.getTransactions(receiver).size());
    }

    @Test
    void testRequestLoanOnSavingsAccount() throws SQLException {
        User user = new User(1, "Test", "User", "12345678901");

        accountService.createAccount(user, AccountType.SAVINGS, 32);
        Account account = accountService.getAccounts(user).get(0);

        AccountOperationResponse response = bankService.requestLoan(user, account.getId(), 1253);

        assertFalse(response.isSuccess());

        assertEquals(0, loanService.getActiveLoans(user).size());
    }

    @Test
    void testPayLoanMoreThanRemainingDebt() throws SQLException {
        User user = new User(1, "Test", "User", "12345678901");

        accountService.createAccount(user, AccountType.CHECKING, 0);
        Account account = accountService.getAccounts(user).get(0);

        bankService.requestLoan(user, account.getId(), 1000);
        Loan loan = loanService.getActiveLoans(user).get(0);

        AccountOperationResponse response = bankService.payLoan(user, account.getId(), loan, 1001);

        assertFalse(response.isSuccess());
    }
}