package abaciarda.bankingsystem.utils;

import java.time.LocalDate;

public class IbanGenerator {
    public static String generateIban(String name) {
        String countryCode = "TR";

        String year = String.valueOf(LocalDate.now().getYear());

        int randomNumber = (int) (Math.random() * 100) + 1;

        String bankCode = "1905";

        return countryCode + year + bankCode + randomNumber + name;
    }
}
