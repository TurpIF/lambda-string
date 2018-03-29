package fr.pturpin.lambdastring.transform;

import fr.pturpin.lambdastring.strategy.LambdaToStringStrategy;

/**
 * Exception representing an error while instantiating a new {@link LambdaToStringStrategy}.
 */
public final class LambdaToStringLinkerException extends Exception {

    /**
     * Create a default {@link LambdaToStringLinkerException}.
     */
    LambdaToStringLinkerException() {
        super();
    }

    /**
     * Create a {@link LambdaToStringLinkerException} with a message.
     *
     * @param msg the detail message
     */
    LambdaToStringLinkerException(String msg) {
        super(msg);
    }

    /**
     * Create a {@link LambdaToStringLinkerException} with a message and a cause.
     *
     * @param msg   the detail message
     * @param cause the cause
     */
    LambdaToStringLinkerException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Create a {@link LambdaToStringLinkerException} with a cause.
     *
     * @param cause the cause
     */
    LambdaToStringLinkerException(Throwable cause) {
        super(cause);
    }

}
