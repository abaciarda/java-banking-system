package abaciarda.bankingsystem.service;

import abaciarda.bankingsystem.models.*;
import abaciarda.bankingsystem.types.AccountOperationResponse;
import abaciarda.bankingsystem.types.AuthResponse;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

public class BankService {
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final UserService userService;
    private final LoanService loanService;

    public BankService(AccountService accountService, TransactionService transactionService, UserService userService, LoanService loanService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.userService = userService;
        this.loanService = loanService;
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
        if (account == null) {
            return new AccountOperationResponse(false, "Gönderici hesap boş olamaz. Lütfen seçtiğiniz hesaba tekrar göz atın.");
        }

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

    public MonthlyReport getMonthlyReport(User user) throws SQLException {
        List<Account> userAccounts = accountService.getAccounts(user);

        double totalDeposit = 0;
        double totalWithdraw = 0;
        double totalTransferIn = 0;
        double totalTransferOut = 0;

        long thirtyDayUnixTMSTMP = 30L * 24 * 60 * 60 * 1000;
        long startDate = Instant.now().toEpochMilli() - thirtyDayUnixTMSTMP;


        for (Account account : userAccounts) {
            List<Transaction> transactions = transactionService.getTransactions(account);

            for(Transaction transaction : transactions) {
                if (transaction.getCreatedAt() < startDate) {
                    continue;
                }

                switch (transaction.getType()) {
                    case DEPOSIT:
                        totalDeposit += transaction.getAmount();
                        break;

                    case WITHDRAW:
                        totalWithdraw += transaction.getAmount();
                        break;

                    case TRANSFER_IN:
                        totalTransferIn += transaction.getAmount();
                        break;

                    case TRANSFER_OUT:
                        totalTransferOut += transaction.getAmount();
                        break;
                }
            }
        }

        double netVal = (totalDeposit + totalTransferIn) - (totalWithdraw + totalTransferOut);

        return new MonthlyReport(totalDeposit, totalWithdraw, totalTransferIn, totalTransferOut, netVal);
    }

    public AccountOperationResponse requestLoan(User user, int accountId, double amount) throws SQLException {
        Account account = accountService.getAccountById(user, accountId);

        if (account == null || account.getType() != AccountType.CHECKING) {
            return new AccountOperationResponse(false, "Kredi yalnızca vadesiz hesaba tanımlanabilir.");
        }

        double interestRate = Bank.GLOBAL_INTEREST_RATE;

        Loan loan = loanService.createLoan(user, amount, interestRate);

        account.deposit(amount);
        accountService.updateBalance(account);

        transactionService.createTransaction(account, TransactionType.LOAN_IN, amount);

        return new AccountOperationResponse(true, String.format("Kredi başarıyla tanımlandı. Toplam borcunuz: %.2f", loan.getTotalDebt()));
    }

    public AccountOperationResponse payLoan(User user, int accountId, Loan loan, double amount) throws SQLException{
        Account account = accountService.getAccountById(user, accountId);

        if (account == null) {
            return new AccountOperationResponse(false, "Hesap bulunamadı.");
        }

        if (account.getType() != AccountType.CHECKING) {
            return new AccountOperationResponse(false, "Kredi ödemeleri yalnızca vadesiz hesapla yapılabilir.");
        }

        if (amount <= 0) {
            return new AccountOperationResponse(false, "Kredi ödemesi gerçekleştirmek için minimum ödemeni gereken miktar 1 TL'dir.");
        }

        if (amount - loan.getRemainingDebt() > 0.01) {
            return new AccountOperationResponse(false, String.format("Ödeme tutarı kalan borçtan fazla olamaz. Kalan borç: %.2f TL", loan.getRemainingDebt()));
        }

        AccountOperationResponse withdraw = account.withdraw(amount);
        if (!withdraw.isSuccess()) {
            return withdraw;
        }

        loan.pay(amount);

        accountService.updateBalance(account);
        loanService.updateLoan(loan);

        transactionService.createTransaction(account, TransactionType.LOAN_PAYMENT, amount);

        return new AccountOperationResponse(true, "Kredi ödemesi başarıyla alındı.");
    }

    public List<Loan> getActiveLoans(User user) throws SQLException {
        return loanService.getActiveLoans(user);
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
