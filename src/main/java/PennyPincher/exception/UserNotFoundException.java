package PennyPincher.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
