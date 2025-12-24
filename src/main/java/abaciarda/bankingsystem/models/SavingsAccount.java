package abaciarda.bankingsystem.models;

import abaciarda.bankingsystem.types.AccountOperationResponse;

import java.time.Instant;

public class SavingsAccount extends Account {
    private final double interestRate;
    private final long maturityDate;

    private static final double EARLY_WITHDRAW_PENALTY_RATE = 5.0;

    public SavingsAccount(int id, int userId, String iban, double balance, AccountType type, double interestRate, long maturityDate) {
        super(id, userId, iban, balance, type);
        this.interestRate = interestRate;
        this.maturityDate = maturityDate;
    }

    @Override
    public AccountOperationResponse withdraw(double amount) {
        if (amount <= 0) {
            return new AccountOperationResponse(false, "Çekmek istediğiniz para miktarı 0'dan büyük olmalı.");
        }

        double finalAmount = amount;

        boolean isBeforeMaturity = Instant.now().toEpochMilli() < maturityDate;

        if (isBeforeMaturity) {
            double penalty = amount * (EARLY_WITHDRAW_PENALTY_RATE / 100);
            finalAmount += penalty;
        } else {
            double interest = balance * (interestRate / 100);
            balance += interest;
        }

        if (finalAmount > balance) {
            return new AccountOperationResponse(false, "Çekmek istediğiniz miktar bakiyenizden daha büyük olamaz.");
        }

        balance -= finalAmount;

        return new AccountOperationResponse(true, isBeforeMaturity ? "Para çekme işlemi vade dolmadan yapıldığı için " + EARLY_WITHDRAW_PENALTY_RATE + "% kesinti ile uygulandı." : "Para çekme işlemi gerçekleşti. Vade sonu gerçekleştiği için faiz miktarı hesabınıza yatırıldı.");

    }
}
