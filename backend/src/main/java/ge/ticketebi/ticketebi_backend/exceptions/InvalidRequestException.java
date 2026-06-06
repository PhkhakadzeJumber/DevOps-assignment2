package ge.ticketebi.ticketebi_backend.exceptions;

public class InvalidRequestException extends RuntimeException{
    public InvalidRequestException(String message) {
        super(message);
    }
}
