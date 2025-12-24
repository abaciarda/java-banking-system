package abaciarda.bankingsystem.models;

public enum AccountType {
    CHECKING("Vadesiz Hesap"),
    SAVINGS("Tasarruf Hesabı");

    private final String label;

    AccountType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
