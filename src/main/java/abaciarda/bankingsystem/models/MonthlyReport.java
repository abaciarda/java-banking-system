package abaciarda.bankingsystem.models;

public class MonthlyReport {
    private double totalDeposit;
    private double totalWithdraw;
    private double totalTransferIn;
    private double totalTransferOut;
    private double netChange;

    public MonthlyReport(double totalDeposit, double totalWithdraw, double totalTransferIn, double totalTransferOut, double netChange) {
        this.totalDeposit = totalDeposit;
        this.totalWithdraw = totalWithdraw;
        this.totalTransferIn = totalTransferIn;
        this.totalTransferOut = totalTransferOut;
        this.netChange = netChange;
    }

    public double getTotalDeposit() {
        return totalDeposit;
    }

    public double getTotalWithdraw() {
        return totalWithdraw;
    }

    public double getTotalTransferIn() {
        return totalTransferIn;
    }

    public double getTotalTransferOut() {
        return totalTransferOut;
    }

    public double getNetChange() {
        return netChange;
    }
}
