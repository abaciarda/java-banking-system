package abaciarda.bankingsystem.models;

import abaciarda.bankingsystem.types.AccountOperationResponse;

public abstract class Account {
    private final int id;
    private final int userId;
    private final String iban;
    protected double balance;
    private final AccountType type;

    public Account(int id, int userId, String iban, double balance, AccountType type) {
        this.id = id;
        this.userId = userId;
        this.iban = iban;
        this.balance = balance;
        this.type = type;
    }

    public AccountOperationResponse deposit(double amount) {
        if (amount <= 0) {
            return new AccountOperationResponse(false, "Yatıracağınız para miktarı 0 veya negatif olamaz.");
        }

        balance += amount;
        return new AccountOperationResponse(true, "Para yatırma işlemi başarıyla gerçekleşti yeni bakiyeniz " + balance);
    }

    public abstract AccountOperationResponse withdraw(double amount);

    protected void decreaseBalance(double amount) {
        this.balance -= amount;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getIban() {
        return iban;
    }

    public double getBalance() {
        return balance;
    }

    public AccountType getType() {
        return type;
    }

}
