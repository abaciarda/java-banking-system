package abaciarda.bankingsystem.types;

public class AccountOperationResponse {
    private final boolean success;
    private final String message;

    public AccountOperationResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
