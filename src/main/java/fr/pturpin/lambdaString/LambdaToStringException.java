package fr.pturpin.lambdaString;

/**
 * Exception representing an error while computing the
 * {@link LambdaToStringStrategy#createToString(Object, LambdaMetaInfo) toString} of a lambda.
 */
public class LambdaToStringException extends Exception {

    public LambdaToStringException() {
        super();
    }

    public LambdaToStringException(String msg) {
        super(msg);
    }

    public LambdaToStringException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public LambdaToStringException(Throwable cause) {
        super(cause);
    }

}
