package ge.ticketebi.ticketebi_backend.exceptions;

public class UnauthorizedActionException extends RuntimeException{
    public UnauthorizedActionException(String message){
        super(message);
    }
}
