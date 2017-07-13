package nl.rubensten.texifyidea;

/**
 * Exception that is thrown by problems within TeXiFy-IDEA.
 *
 * @author Ruben Schellekens
 */
public class TeXception extends RuntimeException {

    public TeXception() {
        super();
    }

    public TeXception(String message) {
        super(message);
    }

    public TeXception(String message, Throwable cause) {
        super(message, cause);
    }

    public TeXception(Throwable cause) {
        super(cause);
    }

}