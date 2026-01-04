package abaciarda.bankingsystem.service;

import abaciarda.bankingsystem.models.User;
import abaciarda.bankingsystem.types.AuthResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    private static Connection testConnection;
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
        userService = new UserService(testConnection);

        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("DELETE FROM users");
        }
    }

    @Test
    void testRegisterUserSucces() throws SQLException {
        AuthResponse<User> response = userService.registerUser("Test", "User", "12345678901", "arda123");

        assertTrue(response.isSuccess());
        assertEquals("Hesap başarıyla oluşturuldu giriş yapabilirsiniz.", response.getMessage());
    }

    @Test
    void testRegisterUserDuplicateSSN() throws SQLException {
        userService.registerUser("Test", "User", "12345678901", "arda123");

        AuthResponse<User> response = userService.registerUser("Test2", "User2", "12345678901", "arda123");

        assertFalse(response.isSuccess());
    }

    @Test
    void testRegisterUserInvalidSSN() throws SQLException {
        AuthResponse<User> response = userService.registerUser("Test", "User", "1234", "arda123");

        assertFalse(response.isSuccess());
    }

    @Test
    void testAuthenticateSuccess() throws SQLException {
        userService.registerUser("Test", "User", "12345678901", "arda123");

        AuthResponse<User> response = userService.authenticateUser("12345678901", "arda123");

        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals("Test", response.getData().getName());
    }

    @Test
    void testAuthenticateWrongPassword() throws SQLException {
        userService.registerUser("Test", "User", "12345678901", "arda123");

        AuthResponse<User> response = userService.authenticateUser("12345678901", "wrongpassword123");

        assertFalse(response.isSuccess());
        assertNull(response.getData());
    }

    @Test
    void testAuthenticateNonExistingUser() throws SQLException {
        AuthResponse<User> response = userService.authenticateUser("12345678901", "arda123");

        assertFalse(response.isSuccess());
    }
}