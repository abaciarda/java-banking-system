package abaciarda.bankingsystem.models;

public class Transaction {
    private final int id;
    private final int accountId;
    private final TransactionType type;
    private final double amount;
    private final double balanceAfter;
    private final long createdAt;

    public Transaction(int id, int accountId, TransactionType type, double amount, double balanceAfter, long createdAt) {
        this.id = id;
        this.accountId = accountId;
        this.type= type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public int getAccountId() {
        return accountId;
    }

    public TransactionType getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public double getBalanceAfter() {
        return balanceAfter;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
