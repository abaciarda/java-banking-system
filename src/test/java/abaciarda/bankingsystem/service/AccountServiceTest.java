package abaciarda.bankingsystem.service;

import abaciarda.bankingsystem.models.Account;
import abaciarda.bankingsystem.models.AccountType;
import abaciarda.bankingsystem.models.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccountServiceTest {
    private static Connection testConnection;
    private AccountService accountService;

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

        try (Statement stmt = testConnection.createStatement()) {
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
    void testCreateCheckingAccount() throws SQLException {
        User user = new User(1, "Test", "User", "12345678901");

        accountService.createAccount(user, AccountType.CHECKING, 0);

        List<Account> accounts = accountService.getAccounts(user);
        assertEquals(1, accounts.size());
        assertEquals(AccountType.CHECKING, accounts.get(0).getType());
        assertEquals(0.0, accounts.get(0).getBalance());
    }

    @Test
    void testCreateSavingsAccount() throws SQLException {
        User user = new User(1, "Test", "User", "12345678901");

        accountService.createAccount(user, AccountType.SAVINGS, 32);

        List<Account> accounts = accountService.getAccounts(user);
        assertEquals(1, accounts.size());
        assertEquals(AccountType.SAVINGS, accounts.get(0).getType());
        assertEquals(0.0, accounts.get(0).getBalance());
    }

    @Test
    void testMultipleAccountsForSameUser() throws SQLException {
        User user = new User(1, "Test", "User", "12345678901");

        accountService.createAccount(user, AccountType.CHECKING, 0);
        accountService.createAccount(user, AccountType.CHECKING, 0);

        List<Account> accounts = accountService.getAccounts(user);
        assertEquals(2, accounts.size());
    }

    @Test
    void testGetAccountByIBanExistingAccount() throws SQLException {
        User user = new User(1, "Test", "User", "12345678901");
        accountService.createAccount(user, AccountType.CHECKING, 0);
        List<Account> accounts = accountService.getAccounts(user);
        String accountIban = accounts.get(0).getIban();

        Account account = accountService.getAccountByIban(accountIban);

        assertNotNull(account);
        assertEquals(accountIban, account.getIban());
        assertEquals(user.getId(), account.getUserId());
    }

    @Test
    void testGetAccountByIdExistingAccount() throws SQLException {
        User user = new User(1, "Test", "User", "12345678901");
        accountService.createAccount(user, AccountType.CHECKING, 0);
        List<Account> accounts = accountService.getAccounts(user);
        int accountId = accounts.get(0).getId();

        Account account = accountService.getAccountById(user, accountId);

        assertNotNull(account);
        assertEquals(accountId, account.getId());
        assertEquals(user.getId(), account.getUserId());
    }

    @Test
    void testGetAccountByIdNonExistingAccount() throws SQLException {
        User user = new User(1, "Test", "User", "12345678901");

        Account account = accountService.getAccountById(user, 999);

        assertNull(account);
    }

    @Test
    void testUpdateBalance() throws SQLException {
        User user = new User(1, "Test", "User", "12345678901");

        accountService.createAccount(user, AccountType.CHECKING, 0);
        List<Account> accounts = accountService.getAccounts(user);
        Account account = accounts.get(0);

        account.deposit(1976.0);
        accountService.updateBalance(account);

        Account updated = accountService.getAccountById(user, account.getId());

        assertEquals(1976.0, updated.getBalance(), 0.01);
    }
}