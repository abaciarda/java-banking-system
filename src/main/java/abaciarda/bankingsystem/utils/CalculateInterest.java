package abaciarda.bankingsystem.utils;

import static abaciarda.bankingsystem.models.Bank.GLOBAL_INTEREST_RATE;

public class CalculateInterest {
    public static void calculateInterest(double amount, int maturityDay) {
        if (amount <= 0) {
            System.out.println("Faizini hesaplamak istediğiniz miktar 0'dan büyük olmalı.");
            return;
        }

        if (maturityDay <= 0) {
            System.out.println("Seçilen vade günü 0'dan büyük olmalıdır.");
            return;
        }

        if (maturityDay > 730) {
            System.out.println("Maksimum vade gününü aşamazsınız, Seçebileceğiniz maksimum vade günü 730 gündür.");
            return;
        }

        double interestAmount = amount * GLOBAL_INTEREST_RATE * ((double) maturityDay / 365);
        double finalAmount = amount + interestAmount;

        System.out.println(maturityDay + " günlük vade sonunda kazanacağınız faiz tutarı: " + String.format("%.2f", interestAmount) + " TL");
        System.out.println(maturityDay +" günlük vade sonu bakiyeniz: " + String.format("%.2f", finalAmount) + " TL");

    }
}
