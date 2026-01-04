package abaciarda.bankingsystem.service;

import abaciarda.bankingsystem.models.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionServiceTest {
    private static Connection testConnection;
    private AccountService accountService;
    private TransactionService transactionService;

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

        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("DELETE FROM transactions");
            stmt.execute("DELETE FROM accounts");
            stmt.execute("DELETE FROM users");
        }

        try (PreparedStatement stmt = testConnection.prepareStatement("INSERT INTO users (id, name, surname, ssn, password) VALUES (?, ?, ?, ?, ?)")) {
            stmt.setInt(1, 1);
            stmt.setString(2, "Test");
            stmt.setString(3, "User");
            stmt.setString(4, "12345678901");
            stmt.setString(5, "hashedpass");
            stmt.executeUpdate();
        }
    }

    @Test
    void testCreateTransaction() throws SQLException {
        User user = new User(1, "Test", "User", "12345678901");

        accountService.createAccount(user, AccountType.CHECKING, 0);
        List<Account> accounts = accountService.getAccounts(user);

        Account account = accounts.get(0);

        account.deposit(500);
        accountService.updateBalance(account);

        transactionService.createTransaction(account, TransactionType.DEPOSIT, 500);

        List<Transaction> transactions = transactionService.getTransactions(account);

        Transaction testTransaction = transactions.get(0);

        assertEquals(1, transactions.size());
        assertEquals(TransactionType.DEPOSIT, testTransaction.getType());
        assertEquals(500, testTransaction.getAmount());
        assertEquals(500, testTransaction.getBalanceAfter());
        assertEquals(account.getId(), testTransaction.getAccountId());
    }

    @Test
    void testGetMultipleTransactions() throws SQLException {
        User user = new User(1, "Test", "User", "12345678901");

        accountService.createAccount(user, AccountType.CHECKING, 0);
        List<Account> accounts = accountService.getAccounts(user);

        Account account = accounts.get(0);

        account.deposit(100);
        accountService.updateBalance(account);
        transactionService.createTransaction(account, TransactionType.DEPOSIT, 100);

        account.withdraw(40);
        accountService.updateBalance(account);
        transactionService.createTransaction(account, TransactionType.WITHDRAW, 40);

        List<Transaction> transactions = transactionService.getTransactions(account);

        assertEquals(2, transactions.size());
        assertEquals(TransactionType.DEPOSIT, transactions.get(0).getType());
        assertEquals(TransactionType.WITHDRAW, transactions.get(1).getType());
    }
}