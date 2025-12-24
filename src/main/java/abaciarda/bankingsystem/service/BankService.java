package abaciarda.bankingsystem.service;

import abaciarda.bankingsystem.models.*;
import abaciarda.bankingsystem.types.AccountOperationResponse;
import abaciarda.bankingsystem.types.AuthResponse;

import java.sql.SQLException;
import java.util.List;

public class BankService {
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final UserService userService;

    public BankService(AccountService accountService, TransactionService transactionService, UserService userService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.userService = userService;
    }

    public AccountOperationResponse deposit(User user, int accountId, double amount) throws SQLException {
        Account account = accountService.getAccountById(user, accountId);

        if (account == null) {
            return new AccountOperationResponse(false, "Para yatırmak istediğiniz hesap bulunamadı");
        }

        AccountOperationResponse response = account.deposit(amount);

        if (!response.isSuccess()) {
            return response;
        }

        accountService.updateBalance(account);

        transactionService.createTransaction(account, TransactionType.DEPOSIT, amount);

        return response;
    }

    public AccountOperationResponse withdraw(User user, int accountId, double amount) throws SQLException {
        Account account = accountService.getAccountById(user, accountId);

        if (account == null) {
            return new AccountOperationResponse(false, "Para çekmek istediğiniz hesap bulunamadı");
        }

        AccountOperationResponse response = account.withdraw(amount);

        if (!response.isSuccess()) {
            return response;
        }

        accountService.updateBalance(account);

        transactionService.createTransaction(account, TransactionType.WITHDRAW, amount);

        return response;
    }

    public AccountOperationResponse transfer(User user, Account account, String iban, double amount) throws SQLException {
        Account sender = accountService.getAccountById(user, account.getId());
        Account receiver = accountService.getAccountByIban(iban);

        if (sender == null) {
            return new AccountOperationResponse(false, "Hesap bulunamadı.");
        }

        if (receiver == null) {
            return new AccountOperationResponse(false, "Alıcı Hesap bulunamadı.");
        }

        if (!(sender instanceof Transferable transferable)) {
            return new AccountOperationResponse(false, "Bu hesap transfer yapamaz.");
        }

        if (!(receiver instanceof Transferable transferable1)) {
            return new AccountOperationResponse(false, "Alıcı hesap transfer için uygun değil.");
        }

        AccountOperationResponse response = transferable.transfer(receiver, amount);

        if (!response.isSuccess()) {
            return response;
        }

        accountService.updateBalance(sender);
        accountService.updateBalance(receiver);

        transactionService.createTransaction(sender, TransactionType.TRANSFER_OUT, amount);
        transactionService.createTransaction(receiver, TransactionType.TRANSFER_IN, amount);

        return response;
    }

    public List<Transaction> getAccountHistory(User user, Account targetAccount) throws SQLException {
        Account account = accountService.getAccountById(user, targetAccount.getId());

        if (account == null) {
            System.out.println("İşlem geçmişi görüntülemek istediğiniz hesap bulunamadı.");
            return null;
        }

       return transactionService.getTransactions(targetAccount);
    }

    public AccountOperationResponse createAccount(User user, Integer accountTypeId, int maturityDay) throws SQLException {
        if (accountTypeId < 0 || accountTypeId > 1) {
            return new AccountOperationResponse(false, "Geçersiz hesap tipi.");
        }

        AccountType type = null;

        if (accountTypeId == 0) {
            type = AccountType.CHECKING;
        }

        if (accountTypeId == 1) {
            if (maturityDay < 1 || maturityDay > 181) {
                return new AccountOperationResponse(false, "Bankamız için minimum ve maksimum vade günleri 1 veya 181 gün olabilir.");
            }
            type = AccountType.SAVINGS;
        }

        accountService.createAccount(user, type, maturityDay);

        return new AccountOperationResponse(true, "Hesabınız başarıyla oluşturuldu!");
    }

    public AuthResponse<User> authenticateUser(String ssn, String password) throws SQLException {
        return userService.authenticateUser(ssn, password);
    }

    public AuthResponse<User> registerUser(String name, String surname, String ssn, String password) throws SQLException {
        return userService.registerUser(name, surname, ssn, password);
    }

    public Account getAccountById(User user, Integer accountId) throws SQLException {
        if (accountId == null) {
            System.out.println("Hesap ID boş olamaz.");
            return null;
        }

        return accountService.getAccountById(user, accountId);
    }

    public List<Account> listAccounts(User user) throws SQLException {
        return accountService.getAccounts(user);
    }
}
