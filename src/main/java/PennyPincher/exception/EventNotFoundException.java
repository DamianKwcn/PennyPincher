package PennyPincher.exception;

public class EventNotFoundException extends RuntimeException{

    public EventNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
