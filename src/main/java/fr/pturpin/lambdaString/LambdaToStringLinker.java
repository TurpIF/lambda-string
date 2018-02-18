package fr.pturpin.lambdaString;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class LambdaToStringLinker {

    private static final Map<String, LambdaToStringStrategy> LINKED_STRATEGY = new ConcurrentHashMap<>();

    /**
     * Method injected in lambda as a <code>toString</code>
     */
    public static String lambdaToString(String strategyClassName) throws BootstrapMethodError {
        return linkStrategy(strategyClassName).createToString();
    }

    static LambdaToStringStrategy linkStrategy(String strategyClassName) throws BootstrapMethodError {
        return LINKED_STRATEGY.computeIfAbsent(strategyClassName, name -> {
            try {
                return createStrategy(name);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static LambdaToStringStrategy createStrategy(String strategyClassName) throws
            ReflectiveOperationException {
        ClassLoader classLoader = InnerClassLambdaMetafactoryTransformer.class.getClassLoader();
        Class<LambdaToStringStrategy> klass = (Class) classLoader.loadClass(strategyClassName);
        Constructor<LambdaToStringStrategy> constructor = klass.getDeclaredConstructor();
        try {
            constructor.setAccessible(true);
        } catch (SecurityException e) {
            throw new ReflectiveOperationException("No accessible constructor in " + strategyClassName + " class", e);
        }
        return constructor.newInstance();
    }

}
