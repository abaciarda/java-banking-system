package abaciarda.bankingsystem.models;

import org.junit.jupiter.api.Test;
import java.time.Instant;

import static abaciarda.bankingsystem.models.Bank.GLOBAL_INTEREST_RATE;
import static org.junit.jupiter.api.Assertions.*;

public class LoanTest {

    @Test
    public void testPayPartialDebt() {
        double totalDebt = 5000.0;
        double remainingDebt = 5000.0;
        Loan loan = new Loan(1, 1, 4000.0, GLOBAL_INTEREST_RATE, totalDebt, remainingDebt, Instant.now().toEpochMilli(), LoanStatus.ACTIVE);
        
        loan.pay(1000.0);
        
        assertEquals(4000.0, loan.getRemainingDebt());
        assertEquals(LoanStatus.ACTIVE, loan.getStatus());
    }

    @Test
    public void testPayFullDebtChangesStatus() {
        double totalDebt = 1000.0;
        Loan loan = new Loan(1, 1, 1000.0, GLOBAL_INTEREST_RATE, totalDebt, totalDebt, Instant.now().toEpochMilli(), LoanStatus.ACTIVE);
        
        loan.pay(1000.0);
        
        assertEquals(0.0, loan.getRemainingDebt());
        assertEquals(LoanStatus.PAID, loan.getStatus());
    }
}
