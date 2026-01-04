package abaciarda.bankingsystem.service;

import abaciarda.bankingsystem.models.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LoanServiceTest {
    private static Connection testConnection;
    private LoanService loanService;

    @BeforeAll
    static void setupDatabase() throws SQLException {
        testConnection = DriverManager.getConnection(
                "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
                "sa",
                ""
        );

        try (Statement stmt = testConnection.createStatement()) {

            stmt.execute("DROP TABLE IF EXISTS loans CASCADE");
            stmt.execute("DROP TABLE IF EXISTS users CASCADE");

            stmt.execute("CREATE TABLE users (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(50) NOT NULL, surname VARCHAR(50) NOT NULL, ssn VARCHAR(20) UNIQUE NOT NULL, password VARCHAR(255) NOT NULL)");

            stmt.execute("CREATE TABLE loans (id INT AUTO_INCREMENT PRIMARY KEY, user_id INT NOT NULL, principal_amount DOUBLE, interest_rate DOUBLE, total_debt DOUBLE, remaining_debt DOUBLE, created_at BIGINT, status VARCHAR(20), FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)");
        }
    }

    @BeforeEach
    void setup() throws SQLException {
        loanService = new LoanService(testConnection);

        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("DELETE FROM loans");
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
    void testCreateLoan() throws SQLException {
        User user = new User(1, "Test", "User", "12345678901");

        Loan loan = loanService.createLoan(user, 1000, Bank.GLOBAL_INTEREST_RATE);

        assertNotNull(loan);
        assertEquals(1, loan.getUserId());
        assertEquals(1000, loan.getPrincipalAmount());
        
        double expectedRate = (Bank.GLOBAL_INTEREST_RATE / 365.0) * 30;
        assertEquals(expectedRate, loan.getInterestRate(), 0.0001);
            
        double expectedDebt = 1000 + (1000 * expectedRate);
        assertEquals(expectedDebt, loan.getTotalDebt(), 0.0001);
        assertEquals(expectedDebt, loan.getRemainingDebt(), 0.0001);
        assertEquals(LoanStatus.ACTIVE, loan.getStatus());
    }

    @Test
    void testGetActiveLoans() throws SQLException {
        User user = new User(1, "Test", "User", "12345678901");

        loanService.createLoan(user, 1000, 0.10);
        loanService.createLoan(user, 2000, 0.20);

        List<Loan> loans = loanService.getActiveLoans(user);

        assertEquals(2, loans.size());
        assertTrue(loans.stream().allMatch(l -> l.getStatus() == LoanStatus.ACTIVE));
    }

    @Test
    void testNotExistedLoan() throws SQLException {
        User user = new User(1, "Test", "User", "12345678901");

        List<Loan> loansList = loanService.getActiveLoans(user);
        assertTrue(loansList.isEmpty());
    }
}