package src;
public class ConcurrentAccessException extends RuntimeException{
    public ConcurrentAccessException(String message){
        super(message);
    }
}