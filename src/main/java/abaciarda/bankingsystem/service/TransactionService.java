package abaciarda.bankingsystem.service;

import abaciarda.bankingsystem.models.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

    public List<Transaction> getTransactions(Account account) throws SQLException{
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE account_id = ?";

        try(PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, account.getId());

            ResultSet res = stmt.executeQuery();

            while(res.next()) {
                int id = res.getInt("id");
                int accountId = res.getInt("account_id");
                TransactionType type = TransactionType.valueOf(res.getString("type"));
                double amount = res.getDouble("amount");
                double balanceAfter = res.getDouble("balance_after");
                long createdAt = res.getLong("created_at");

                Transaction transaction = new Transaction(id, accountId, type, amount, balanceAfter, createdAt);

                transactions.add(transaction);
            }

            return transactions;
        }
    }
}
