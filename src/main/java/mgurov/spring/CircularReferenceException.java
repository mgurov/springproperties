package mgurov.spring;

public class CircularReferenceException extends RuntimeException {
    public CircularReferenceException(String keyRefererence) {
        super(keyRefererence + " has already been met before");
    }
}
