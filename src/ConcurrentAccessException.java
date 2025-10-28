package src;
//Custom class us to throw and error if there are 2 or more thread access the same deck
public class ConcurrentAccessException extends RuntimeException{
    public ConcurrentAccessException(String message){
        super(message);
    }
}