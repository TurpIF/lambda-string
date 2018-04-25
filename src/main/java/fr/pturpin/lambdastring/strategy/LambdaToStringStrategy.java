package fr.pturpin.lambdastring.strategy;

import fr.pturpin.lambdastring.transform.LambdaMetaInfo;

/**
 * Factory class generating toString representation for lambdas.
 *
 * <p>Implementations should provide a default public constructor. One strategy is created per
 * linked lambda. This strategy stay constant during the lifetime of the VM. So implementation may
 * store cached data in the strategy. For shared cached data, implementation should use static
 * variable as usual.
 *
 * <p>For more information about the linkage of a lambda to a strategy, please see {@link
 * fr.pturpin.lambdastring.transform.LambdaToStringLinker}.
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
