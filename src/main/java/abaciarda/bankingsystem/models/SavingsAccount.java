package abaciarda.bankingsystem.models;

import abaciarda.bankingsystem.types.AccountOperationResponse;

import java.time.Instant;

import static abaciarda.bankingsystem.models.Bank.GLOBAL_EARLY_WITHDRAW_PENALTY_RATE;

public class SavingsAccount extends Account {
    private final double interestRate;
    private final long maturityDate;

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

        boolean isBeforeMaturity = Instant.now().toEpochMilli() < maturityDate;

        double finalAmount = amount;
        double interest = 0.0;

        if (isBeforeMaturity) {
            double penalty = amount * (GLOBAL_EARLY_WITHDRAW_PENALTY_RATE / 100);
            finalAmount += penalty;
        } else {
            interest = this.getBalance() * (interestRate / 100);
        }

        double availableBalance = this.getBalance() + interest;

        if (finalAmount > availableBalance) {
            if (isBeforeMaturity) {
                return new AccountOperationResponse(false, "Yetersiz bakiye. Erken çekim nedeniyle %"+ GLOBAL_EARLY_WITHDRAW_PENALTY_RATE + " ceza uygulanır. Gerekli toplam tutar: " + finalAmount);
            } else {
                return new AccountOperationResponse(false, "Yetersiz bakiye. Faiz dahil çekilebilir tutar: " + availableBalance);
            }
        }


        if (!isBeforeMaturity) {
            increaseBalance(interest);
        }

        decreaseBalance(finalAmount);

        return new AccountOperationResponse(true, isBeforeMaturity ? "Para çekme işlemi vade dolmadan yapıldığı için " + GLOBAL_EARLY_WITHDRAW_PENALTY_RATE + "% kesinti ile uygulandı." : "Para çekme işlemi gerçekleşti. Vade sonu gerçekleştiği için faiz miktarı hesabınıza yatırıldı.");
    }

}
