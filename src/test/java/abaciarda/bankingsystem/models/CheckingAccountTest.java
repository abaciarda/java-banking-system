package abaciarda.bankingsystem.models;

import abaciarda.bankingsystem.types.AccountOperationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CheckingAccountTest {

    private CheckingAccount checkingAccount;

    @BeforeEach
    public void setUp() {
        checkingAccount = new CheckingAccount(1, 1, "IBAN1", 1000.0, AccountType.CHECKING);
    }

    @Test
    public void testWithdrawSuccess() {
        AccountOperationResponse response = checkingAccount.withdraw(500.0);
        assertTrue(response.isSuccess());
        assertEquals(500.0, checkingAccount.getBalance());
    }

    @Test
    public void testWithdrawInsufficientBalance() {
        AccountOperationResponse response = checkingAccount.withdraw(1500.0);
        assertFalse(response.isSuccess());
        assertEquals(1000.0, checkingAccount.getBalance());
    }

    @Test
    public void testWithdrawNegativeAmount() {
        AccountOperationResponse response = checkingAccount.withdraw(-100.0);
        assertFalse(response.isSuccess());
        assertEquals(1000.0, checkingAccount.getBalance());
    }

     @Test
    public void testTransferSuccess() {
        CheckingAccount targetAccount = new CheckingAccount(2, 2, "IBAN2", 500.0, AccountType.CHECKING);
        
        AccountOperationResponse response = checkingAccount.transfer(targetAccount, 200.0);
        
        assertTrue(response.isSuccess());
        assertEquals(800.0, checkingAccount.getBalance());
        assertEquals(700.0, targetAccount.getBalance());
    }

    @Test
    public void testTransferInsufficientBalance() {
        CheckingAccount targetAccount = new CheckingAccount(2, 2, "IBAN2", 500.0, AccountType.CHECKING);
        
        AccountOperationResponse response = checkingAccount.transfer(targetAccount, 1200.0);
        
        assertFalse(response.isSuccess());
        assertEquals(1000.0, checkingAccount.getBalance());
        assertEquals(500.0, targetAccount.getBalance());
    }

    @Test
    public void testTransferNullTarget() {
        AccountOperationResponse response = checkingAccount.transfer(null, 200.0);
        assertFalse(response.isSuccess());
        assertEquals(1000.0, checkingAccount.getBalance());
    }
}
