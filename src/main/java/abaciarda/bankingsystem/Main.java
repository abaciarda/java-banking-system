package abaciarda.bankingsystem;

import abaciarda.bankingsystem.config.DBConnection;
import abaciarda.bankingsystem.models.Account;
import abaciarda.bankingsystem.models.MonthlyReport;
import abaciarda.bankingsystem.models.User;
import abaciarda.bankingsystem.service.AccountService;
import abaciarda.bankingsystem.service.BankService;
import abaciarda.bankingsystem.service.TransactionService;
import abaciarda.bankingsystem.service.UserService;
import abaciarda.bankingsystem.types.AccountOperationResponse;
import abaciarda.bankingsystem.types.AuthResponse;
import abaciarda.bankingsystem.utils.CH;
import abaciarda.bankingsystem.utils.CalculateInterest;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static Connection connection;
    private static BankService bankService;

    public static Scanner sc = new Scanner(System.in);

    public static String RED = "\u001B[31m";
    public static String GREEN = "\u001B[32m";
    public static String RESET = "\u001B[0m";

    public static void main(String[] args) {
        connection = DBConnection.getConnection();
        UserService userService = new UserService(connection);
        AccountService accountService = new AccountService(connection);
        TransactionService transactionService = new TransactionService(connection);
        bankService = new BankService(accountService, transactionService, userService);

        boolean running = true;

        System.out.println(RED + """
               ██╗ █████╗ ██╗   ██╗ █████╗     ██████╗  █████╗ ███╗   ██╗██╗  ██╗
               ██║██╔══██╗██║   ██║██╔══██╗    ██╔══██╗██╔══██╗████╗  ██║██║ ██╔╝
               ██║███████║██║   ██║███████║    ██████╔╝███████║██╔██╗ ██║█████╔╝ 
          ██   ██║██╔══██║╚██╗ ██╔╝██╔══██║    ██╔══██╗██╔══██║██║╚██╗██║██╔═██╗ 
          ╚█████╔╝██║  ██║ ╚████╔╝ ██║  ██║    ██████╔╝██║  ██║██║ ╚████║██║  ██╗
           ╚════╝ ╚═╝  ╚═╝  ╚═══╝  ╚═╝  ╚═╝    ╚═════╝ ╚═╝  ╚═╝╚═╝  ╚═══╝╚═╝  ╚═╝
        """ + RESET);

        while(running) {
            CH.printTitle("Ana Menü");
            System.out.println(RED + "Merhaba! Java Bank'a hoş geldiniz!" + RESET);
            System.out.println("Aşağıdaki menüden yapmak istediğiniz işlemi seçiniz:");
            System.out.println("1. Giriş Yap");
            System.out.println("2. Kayıt Ol");
            System.out.println("3. Çıkış Yap");

            int choice = readInt("Seçiminiz: ");
            CH.singleSpace();

            switch (choice) {
                case 1:
                    handleLogin();
                    break;
                case 2:
                    handleRegister();
                    break;
                case 3:
                    CH.info("İyi günler dileriz...");
                    running = false;
                    break;
                default:
                    CH.error("Geçersiz seçim, tekrar deneyin.");
            }
        }
    }

    private static void handleLogin() {
        CH.printTitle("Kullanıcı Girişi");
        System.out.println("TC Kimlik No Giriniz:");
        String ssn = sc.nextLine();
        System.out.println("Şifrenizi giriniz: ");
        String password = sc.nextLine();

        try {
            AuthResponse<User> response = bankService.authenticateUser(ssn, password);

            if (response.isSuccess()) {
                CH.success("Giriş başarılı! Hoşgeldin " + response.getData().getName());

                CH.multiSpace();
                showUserDashboard(response.getData());
            } else {
                System.out.println("Hata: " + response.getMessage());
                CH.multiSpace();
            }
        } catch (SQLException e) {
            CH.error("Sistem hatası: " + e.getMessage());
            CH.multiSpace();
        }
    }

    private static void handleRegister() {
        CH.printTitle("Yeni Kullanıcı Kaydı");
        System.out.print("Ad: ");
        String name = sc.nextLine();
        System.out.print("Soyad: ");
        String surname = sc.nextLine();
        System.out.print("TC Kimlik No (SSN): ");
        String ssn = sc.nextLine();
        System.out.print("Şifre: ");
        String password = sc.nextLine();

        try {
            AuthResponse<User> response = bankService.registerUser(name, surname, ssn, password);
            CH.info(response.getMessage());
        } catch (SQLException e) {
            CH.error("Kayıt sırasında hata: " + e.getMessage());
        }
    }

    private static void showUserDashboard(User user) {
        boolean authenticated = true;

        while (authenticated) {
            System.out.println("Kullanıcı: " + user.getName() + " " + user.getSurname());
            CH.printDivider();
            System.out.println("1. Hesaplarımı Listele");
            System.out.println("2. Yeni Hesap Aç");
            System.out.println("3. Para Yatır");
            System.out.println("4. Para Çek");
            System.out.println("5. Para Transferi (Havale)");
            System.out.println("6. İşlem Geçmişi");
            System.out.println("7. Faiz Hesaplama");
            System.out.println("8. Aylık Rapor");
            System.out.println("9. Çıkış Yap (Oturumu Kapat)");
            System.out.print("İşlem Seçiniz: ");

            int choice = readInt("İşlem Seçiniz");
            CH.multiSpace();

            try {
                switch (choice) {
                    case 1:
                        listUserAccounts(user);
                        break;
                    case 2:
                        createNewAccount(user);
                        break;
                    case 3:
                        handleDeposit(user);
                        break;
                    case 4:
                        handleWithdraw(user);
                        break;
                    case 5:
                        handleTransfer(user);
                        break;
                    case 6:
                        showAccountHistory(user);
                        break;
                    case 7:
                        handleCalculateInterest(user);
                        break;
                    case 8:
                        showMonthlyReport(user);
                        break;
                    case 9:
                        authenticated = false;
                        break;
                    default:
                        CH.error("Geçersiz işlem.");
                }
            } catch (SQLException e) {
                CH.error("İşlem sırasında veritabanı hatası: " + e.getMessage());
            }
        }
    }

    private static void listUserAccounts(User user) throws SQLException {
        CH.printTitle("Hesaplarınız");
        List<Account> accounts = bankService.listAccounts(user);

        if (accounts.isEmpty()) {
            CH.info("Size ait bir hesap bulunamadı.");
            CH.multiSpace();
        } else {
            System.out.println("Aktif Hesaplarınız: ");

            System.out.printf("%-5s %-20s %-15s %-10s\n", "ID", "IBAN", "Bakiye", "Tip");
            CH.printDivider();

            for (Account acc : accounts) {
                System.out.printf("%-5d %-20s %-15.2f %-10s\n",
                    acc.getId(),
                    acc.getIban(),
                    acc.getBalance(),
                    acc.getType().getLabel());
            }
            CH.printDivider();
            CH.multiSpace();
        }
    }

    private static void createNewAccount(User user) throws SQLException {
        CH.printTitle("Yeni Hesap Aç");
        System.out.println("0 - Vadesiz Hesap");
        System.out.println("1 - Tasarruf Hesap");
        int typeId = readInt("Seçiminiz: ");

        int maturity = 0;
        if (typeId == 1) {
            maturity = readInt("Vade günü (1-181 gün): ");
        }

        AccountOperationResponse response = bankService.createAccount(user, typeId, maturity);
        CH.info(response.isSuccess() ? "Başarılı: " + response.getMessage() : "Hata: " + response.getMessage());
        CH.printDivider();
        CH.multiSpace();
    }

    private static void handleDeposit(User user) throws SQLException {
        listUserAccounts(user);
        int accountId = readInt("Para yatırmak istediğiniz hesabın ID'sini giriniz: ");
        double amount = readDouble("Yatırmak istediğiniz miktarı giriniz: ");

        AccountOperationResponse response = bankService.deposit(user, accountId, amount);
        CH.info(response.getMessage());
        CH.printDivider();
        CH.multiSpace();
    }

    private static void handleWithdraw(User user) throws SQLException {
        listUserAccounts(user);
        int accountId = readInt("Para çekmek istediğiniz hesabın ID'sini giriniz: ");
        double amount = readDouble("Çekmek istediğiniz miktarı giriniz: ");

        AccountOperationResponse response = bankService.withdraw(user, accountId, amount);
        CH.info(response.getMessage());
        CH.multiSpace();
    }

    private static void handleTransfer(User user) throws SQLException {
        listUserAccounts(user);
        CH.printDivider();
        int senderAccountId = readInt("Gönderim yapacak hesabın ID'sini giriniz: ");

        Account senderAccount = bankService.getAccountById(user, senderAccountId);

        System.out.println("Alıcı IBAN giriniz:");
        String targetIban = sc.nextLine();
        double amount = readDouble("Gönderilecek miktarı giriniz:");

        AccountOperationResponse response = bankService.transfer(user, senderAccount, targetIban, amount);
        System.out.println(response.getMessage());
        CH.printDivider();
        CH.multiSpace();
    }

    private static void showAccountHistory(User user) throws SQLException {
        CH.printTitle("Hesap İşlem Geçmişi");

        listUserAccounts(user);
        int accountId = readInt("İşlem geçmişi görüntülemek istediğiniz hesabın ID'sini giriniz:");

        Account account = bankService.getAccountById(user, accountId);

        if (account == null) {
            CH.error("Hesap bulunamadı.");
            return;
        }

        List<abaciarda.bankingsystem.models.Transaction> transactions =
                bankService.getAccountHistory(user, account);

        if (transactions == null || transactions.isEmpty()) {
            CH.info("Bu hesaba ait işlem bulunmamaktadır.");
            CH.multiSpace();
            return;
        }

        System.out.printf(
                "%-5s %-15s %-12s %-15s %-20s%n",
                "ID", "TÜR", "TUTAR", "BAKİYE", "TARİH"
        );
        CH.printDivider();

        for (var t : transactions) {
            System.out.printf(
                    "%-5d %-15s %-12.2f %-15.2f %-20s%n",
                    t.getId(),
                    t.getType(),
                    t.getAmount(),
                    t.getBalanceAfter(),
                    CH.formatDate(t.getCreatedAt())
            );
        }

        CH.printDivider();
        CH.multiSpace();
    }

    private static void showMonthlyReport(User user) throws SQLException {
        CH.printTitle("Aylık Rapor");

        MonthlyReport report = bankService.getMonthlyReport(user);

        System.out.println("======================================");
        System.out.println("        SON 30 GÜNLÜK HESAP RAPORU      ");
        System.out.println("======================================");

        System.out.printf("Toplam Para Yatırma   : %.2f TL%n", report.getTotalDeposit());
        System.out.printf("Toplam Para Çekme     : %.2f TL%n", report.getTotalWithdraw());
        System.out.printf("Toplam Gelen Transfer : %.2f TL%n", report.getTotalTransferIn());
        System.out.printf("Toplam Giden Transfer : %.2f TL%n", report.getTotalTransferOut());

        CH.printDivider();

        System.out.printf("Net Değişim           : %.2f TL%n", report.getNetChange());

        if (report.getNetChange() > 0) {
            System.out.println("Durum                 : KÂR");
        } else if (report.getNetChange() < 0) {
            System.out.println("Durum                 : ZARAR");
        } else {
            System.out.println("Durum                 : DEĞİŞİM YOK");
        }

        System.out.println("======================================");

        CH.printDivider();
        CH.multiSpace();
    }

    public static void handleCalculateInterest(User user) {
        CH.printTitle("Faiz Hesaplama");

        double amount = readDouble("Anapara miktarını giriniz");
        int maturityDay = readInt("Vade süresini giriniz (1-730 Gün)");

        CalculateInterest.calculateInterest(amount, maturityDay);

        CH.printDivider();
        CH.multiSpace();
    }

    public static int readInt(String question) {
        System.out.println(question);
        while (true) {
            try {
                String input = sc.nextLine().trim();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                CH.error("Hatalı giriş! Lütfen geçerli bir tam sayı giriniz.");
            }
        }
    }

    private static double readDouble(String question) {
        System.out.println(question);
        while (true) {
            try {
                String input = sc.nextLine().trim();
                input = input.replace(",", ".");
                double value = Double.parseDouble(input);
                if (value < 0) {
                    CH.error("Miktar negatif olamaz!");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                CH.error("Hatalı giriş! Lütfen geçerli bir para miktarı giriniz (Örn: 100.50).");
            }
        }
    }


}