package abaciarda.bankingsystem.models;

import abaciarda.bankingsystem.types.AccountOperationResponse;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import static org.junit.jupiter.api.Assertions.*;

public class SavingsAccountTest {

    @Test
    public void testWithdrawBeforeMaturityPenaltyApplied() {
        long futureMaturity = Instant.now().plus(30, ChronoUnit.DAYS).toEpochMilli();
        double balance = 1000.0;
        double interestRate = (Bank.GLOBAL_INTEREST_RATE / 365.0) * 30;
        
        SavingsAccount savingsAccount = new SavingsAccount(1, 1, "IBAN1", balance, AccountType.SAVINGS, interestRate, futureMaturity);
        
        double withdrawAmount = 100.0;

        double penaltyRate = Bank.GLOBAL_EARLY_WITHDRAW_PENALTY_RATE;
        double expectedPenalty = withdrawAmount * penaltyRate;
        double expectedFinalAmount = withdrawAmount + expectedPenalty;
        
        AccountOperationResponse response = savingsAccount.withdraw(withdrawAmount);
        
        assertTrue(response.isSuccess());
        assertEquals(balance - expectedFinalAmount, savingsAccount.getBalance(), 0.001);
    }

    @Test
    public void testWithdrawBeforeMaturityInsufficientFundsDueToPenalty() {
        long futureMaturity = Instant.now().plus(30, ChronoUnit.DAYS).toEpochMilli();
        double balance = 100.0;
        SavingsAccount savingsAccount = new SavingsAccount(1, 1, "IBAN1", balance, AccountType.SAVINGS, Bank.GLOBAL_INTEREST_RATE, futureMaturity);
        
        double withdrawAmount = 100.0;
        
        AccountOperationResponse response = savingsAccount.withdraw(withdrawAmount);
        
        assertFalse(response.isSuccess());
        assertEquals(100.0, savingsAccount.getBalance());
        assertTrue(response.getMessage().contains("Yetersiz bakiye"));
    }

    @Test
    public void testWithdrawAfterMaturityInterestApplied() {
        long pastMaturity = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
        double balance = 1000.0;
        double interestRate = Bank.GLOBAL_INTEREST_RATE; 
        SavingsAccount savingsAccount = new SavingsAccount(1, 1, "IBAN1", balance, AccountType.SAVINGS, interestRate, pastMaturity);
        
        double withdrawAmount = 500.0;
        double expectedInterest = balance * interestRate;
        double expectedBalance = (balance + expectedInterest) - withdrawAmount;

        AccountOperationResponse response = savingsAccount.withdraw(withdrawAmount);
        
        assertTrue(response.isSuccess());
        assertEquals(expectedBalance, savingsAccount.getBalance(), 0.001);
        assertTrue(response.getMessage().contains("faiz miktarı hesabınıza yatırıldı"));
    }
}
