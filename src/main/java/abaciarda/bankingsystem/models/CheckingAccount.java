package abaciarda.bankingsystem.models;

import abaciarda.bankingsystem.types.AccountOperationResponse;

public class CheckingAccount extends Account implements Transferable{

    public CheckingAccount(int id, int userId, String iban, double balance, AccountType type) {
        super(id, userId, iban, balance, type);
    }

    @Override
    public AccountOperationResponse withdraw(double amount) {
        if (amount < 0) {
            return new AccountOperationResponse(false, "Yatıracağınız para miktarı 0 veya negatif olamaz.");
        }

        if (amount > this.getBalance()) {
            return new AccountOperationResponse(false, "Çekmek istediğiniz para miktarı ana bakiyenizden yüksek olamaz");
        }

        this.decreaseBalance(amount);
        return new AccountOperationResponse(true, "Para çekme işleminiz gerçekleşti. Yeni bakiyeniz: " + this.getBalance());
    }

    @Override
    public AccountOperationResponse transfer(Account targetAccount, double amount) {
        if (targetAccount == null) {
            return new AccountOperationResponse(false, "Hedef hesap bulunamadı.");
        }

        if (amount <= 0) {
            return new AccountOperationResponse(false, "Transfer tutarı 0'dan büyük olmalıdır.");
        }

        if (amount > this.getBalance()) {
            return new AccountOperationResponse(false, "Yetersiz bakiye.");
        }

        decreaseBalance(amount);

        targetAccount.deposit(amount);

        return new AccountOperationResponse(true, amount + " TL transfer edildi. Yeni bakiye: " + getBalance());
    }
}
