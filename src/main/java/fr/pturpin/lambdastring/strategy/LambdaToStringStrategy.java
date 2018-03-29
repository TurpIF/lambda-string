package fr.pturpin.lambdastring.strategy;

import fr.pturpin.lambdastring.transform.LambdaMetaInfo;

/**
 * Factory class generating toString representation for lambdas.
 */
@FunctionalInterface
public interface LambdaToStringStrategy {

    /**
     * Generate a <code>toString</code> for the given lambda and its {@link LambdaMetaInfo meta information}.
     *
     * The implementation may throw any exception that is encapsulated in an {@link LambdaToStringException}.
     * Then the <code>toString</code> call on a lambda will throw this exception.
     * If an other kind of exception ({@link RuntimeException} included) is thrown, it may be consumed silently.
     *
     * @param lambda lambda instance
     * @param metaInfo lambda meta information
     * @return toString for the given lambda
     * @throws LambdaToStringException if this strategy cannot generate a toString
     */
    String createToString(Object lambda, LambdaMetaInfo metaInfo) throws LambdaToStringException;

}
