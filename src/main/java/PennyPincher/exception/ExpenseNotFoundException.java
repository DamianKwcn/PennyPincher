package PennyPincher.exception;

public class ExpenseNotFoundException extends RuntimeException {

    public ExpenseNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}