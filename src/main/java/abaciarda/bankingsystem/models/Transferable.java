package abaciarda.bankingsystem.models;

import abaciarda.bankingsystem.types.AccountOperationResponse;

public interface Transferable {
    AccountOperationResponse transfer(Account targetAccount, double amount);
}
