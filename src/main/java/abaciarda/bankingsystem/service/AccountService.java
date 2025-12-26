package abaciarda.bankingsystem.service;

import abaciarda.bankingsystem.models.*;
import abaciarda.bankingsystem.types.AccountOperationResponse;
import abaciarda.bankingsystem.utils.IbanGenerator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static abaciarda.bankingsystem.models.Bank.GLOBAL_INTEREST_RATE;

public class AccountService {
    private final Connection connection;

    public AccountService(Connection connection) {
        this.connection = connection;
    }

    public List<Account> getAccounts(User user) throws SQLException {
        List<Account> accounts = new ArrayList<>();

        String sql = "SELECT id, user_id, iban, balance, type, interest_rate, maturity_date FROM accounts WHERE user_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, user.getId());
            ResultSet res = stmt.executeQuery();

            while (res.next()) {
                int id = res.getInt("id");
                int userId = res.getInt("user_id");
                String iban = res.getString("iban");
                double balance = res.getDouble("balance");
                AccountType type = AccountType.valueOf(res.getString("type"));

                Account account;

                switch (type) {
                    case CHECKING:
                        account = new CheckingAccount(id, userId, iban, balance, type);
                        break;

                    case SAVINGS:
                        double interestRate = res.getDouble("interest_rate");
                        long maturityDate = res.getLong("maturity_date");
                        account = new SavingsAccount(id, userId, iban, balance, type, interestRate, maturityDate);
                        break;

                    default:
                        throw new IllegalStateException("Unknown account type: " + type);
                }

                accounts.add(account);
            }
        }

        return accounts;
    }

    public Account getAccountById(User user, int accountId) throws SQLException {

        String sql = "SELECT id, user_id, iban, balance, type, interest_rate, maturity_date FROM accounts WHERE id = ? AND user_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            stmt.setInt(2, user.getId());

            ResultSet res = stmt.executeQuery();

            if (res.next()) {
                int id = res.getInt("id");
                int userId = res.getInt("user_id");
                String iban = res.getString("iban");
                double balance = res.getDouble("balance");
                AccountType type = AccountType.valueOf(res.getString("type"));

                switch (type) {
                    case CHECKING:
                        return new CheckingAccount(id, userId, iban, balance, type);

                    case SAVINGS:
                        double interestRate = res.getDouble("interest_rate");
                        long maturityDate = res.getLong("maturity_date");
                        return new SavingsAccount(id, userId, iban, balance, type, interestRate, maturityDate);
                }
            }
        }

        return null;
    }

    public Account getAccountByIban(String targetIban) throws SQLException {

        String sql = "SELECT id, user_id, iban, balance, type, interest_rate, maturity_date FROM accounts WHERE iban = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, targetIban);

            ResultSet res = stmt.executeQuery();

            if (res.next()) {
                int id = res.getInt("id");
                int userId = res.getInt("user_id");
                String iban = res.getString("iban");
                double balance = res.getDouble("balance");
                AccountType type = AccountType.valueOf(res.getString("type"));

                switch (type) {
                    case CHECKING:
                        return new CheckingAccount(id, userId, iban, balance, type);

                    case SAVINGS:
                        double interestRate = res.getDouble("interest_rate");
                        long maturityDate = res.getLong("maturity_date");
                        return new SavingsAccount(id, userId, iban, balance, type, interestRate, maturityDate);
                }
            }
        }

        return null;
    }

    public void createAccount(User user, AccountType type, int maturityDay) throws SQLException {
        String sql = "INSERT INTO accounts (user_id, iban, balance, type, interest_rate, maturity_date) VALUES (?, ?, ?, ?, ?, ?)";

        try(PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, user.getId());
            stmt.setString(2, IbanGenerator.generateIban(user.getName()));
            stmt.setInt(3, 0);

            if (type == AccountType.CHECKING) {
                stmt.setString(4, "CHECKING");
                stmt.setInt(5, 0);
                stmt.setInt(6, 0);
            }

            if (type == AccountType.SAVINGS) {
                stmt.setString(4, "SAVINGS");
                stmt.setDouble(5, GLOBAL_INTEREST_RATE);

                long maturityDate = Instant.now().plusSeconds(maturityDay * 24L * 60 * 60).toEpochMilli();
                stmt.setLong(6, maturityDate);
            }

            stmt.executeUpdate();
        }
    }

    public void updateBalance(Account account) throws SQLException {
        String sql = "UPDATE accounts SET balance = ? WHERE id = ?";

        try(PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, account.getBalance());
            stmt.setInt(2, account.getId());

            stmt.executeUpdate();
        }
    }

    public List<AccountType> showAccountTypes() {
        return Arrays.asList(AccountType.values());
    }
}
