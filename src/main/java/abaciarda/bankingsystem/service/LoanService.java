package abaciarda.bankingsystem.service;

import abaciarda.bankingsystem.models.Bank;
import abaciarda.bankingsystem.models.Loan;
import abaciarda.bankingsystem.models.LoanStatus;
import abaciarda.bankingsystem.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class LoanService {
    private final Connection connection;

    public LoanService(Connection connection) {
        this.connection = connection;
    }

    public Loan createLoan(User user, double amount, double interestRate) throws SQLException {
        double effectiveRate = (interestRate / 365.0) * 30;
        double totalDebt = amount + (amount * effectiveRate);
        long now = Instant.now().toEpochMilli();

        String sql = "INSERT INTO loans (user_id, principal_amount, interest_rate, total_debt, remaining_debt, created_at, status) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try(PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, user.getId());
            stmt.setDouble(2, amount);
            stmt.setDouble(3, effectiveRate);
            stmt.setDouble(4, totalDebt);
            stmt.setDouble(5, totalDebt);
            stmt.setLong(6, now);
            stmt.setString(7, LoanStatus.ACTIVE.name());

            stmt.executeUpdate();
        }

        String selectSql = "SELECT * FROM loans WHERE user_id = ? AND created_at = ? LIMIT 1";

        try (PreparedStatement stmt = connection.prepareStatement(selectSql)) {
            stmt.setInt(1, user.getId());
            stmt.setLong(2, now);

            ResultSet res = stmt.executeQuery();
            if (res.next()) {
                return new Loan(
                    res.getInt("id"),
                    res.getInt("user_id"),
                    res.getDouble("principal_amount"),
                    res.getDouble("interest_rate"),
                    res.getDouble("total_debt"),
                    res.getDouble("remaining_debt"),
                    res.getLong("created_at"),
                    LoanStatus.valueOf(res.getString("status"))
                );
            }
        }


        throw new SQLException("Loan oluşturulamadı");
    }

    public List<Loan> getActiveLoans(User user) throws SQLException {
        List<Loan> loans = new ArrayList<>();

        String sql = "SELECT * FROM loans WHERE user_id = ? AND status = 'ACTIVE'";

        try(PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, user.getId());

            ResultSet result = stmt.executeQuery();
            while (result.next()) {
                loans.add(new Loan(
                    result.getInt("id"),
                    result.getInt("user_id"),
                    result.getDouble("principal_amount"),
                    result.getDouble("interest_rate"),
                    result.getDouble("total_debt"),
                    result.getDouble("remaining_debt"),
                    result.getLong("created_at"),
                    LoanStatus.valueOf(result.getString("status"))
                ));
            }
        }
        return loans;
    }

    public void updateLoan(Loan loan) throws SQLException {
        String sql = "UPDATE loans SET remaining_debt = ?, status = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, loan.getRemainingDebt());
            stmt.setString(2, loan.getStatus().name());
            stmt.setInt(3, loan.getId());
            stmt.executeUpdate();
        }
    }
}
