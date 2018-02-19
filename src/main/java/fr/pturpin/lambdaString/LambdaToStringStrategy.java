package fr.pturpin.lambdaString;

/**
 * Factory class generating toString representation for lambdas.
 */
@FunctionalInterface
public interface LambdaToStringStrategy {
    String createToString(Object lambda);
}
