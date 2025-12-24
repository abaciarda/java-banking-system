package abaciarda.bankingsystem.service;

import abaciarda.bankingsystem.models.Account;
import abaciarda.bankingsystem.models.TransactionType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TransactionService {
    private final Connection connection;

    public TransactionService(Connection connection) {
        this.connection = connection;
    }

    public void createTransaction(Account account, TransactionType type, double amount) throws SQLException {
        String sql = "INSERT INTO transactions (account_id, type, amount, balance_after, created_at) VALUES (?, ?, ?, ?, ?)";

        try(PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, account.getId());
            stmt.setString(2, type.name());
            stmt.setDouble(3, amount);
            stmt.setDouble(4, account.getBalance());
            stmt.setLong(5, System.currentTimeMillis());

            stmt.executeUpdate();
        }
    }
}
