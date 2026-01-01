package abaciarda.bankingsystem.models;

public class Loan {
    private final int id;
    private final int userId;
    private final double principalAmount;
    private final double interestRate;
    private final double totalDebt;
    private double remainingDebt;
    private final long createdAt;
    private LoanStatus status;

    public Loan(int id, int userId, double principalAmount, double interestRate, double totalDebt, double remainingDebt, long createdAt, LoanStatus status) {
        this.id = id;
        this.userId = userId;
        this.principalAmount = principalAmount;
        this.interestRate = interestRate;
        this.totalDebt = totalDebt;
        this.remainingDebt = remainingDebt;
        this.createdAt = createdAt;
        this.status = status;
    }

    public void pay(double amount) {
        this.remainingDebt -= amount;

        if (remainingDebt <= 0) {
            remainingDebt = 0;
            status = LoanStatus.PAID;
        }
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public double getPrincipalAmount() {
        return principalAmount;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public double getTotalDebt() {
        return totalDebt;
    }

    public double getRemainingDebt() {
        return remainingDebt;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public LoanStatus getStatus() {
        return status;
    }
}
