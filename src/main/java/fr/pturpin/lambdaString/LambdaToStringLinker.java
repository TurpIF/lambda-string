package fr.pturpin.lambdaString;

import java.lang.invoke.*;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class LambdaToStringLinker {

    private static final Map<String, LambdaToStringStrategy> LINKED_STRATEGY = new ConcurrentHashMap<>();

    /**
     * Generates a new {@link CallSite} from the given {@link LambdaToStringStrategy} class name.
     * <p>
     * The call site represent the {@link LambdaToStringStrategy#createToString(Object, LambdaMetaInfo)} method in a
     * new instance of the given class.
     * <p>
     * For every call, a new call site is generated with a new strategy instance. Also, the call site is
     * {@link ConstantCallSite constant} and has a permanent target. This means that a unique strategy per VM is
     * instantiated per lambda and those strategies are never shared.
     * <p>
     * The given class name should represent a static {@link LambdaToStringStrategy} class name with a default
     * instantiable default constructor. If any error occurs, it's embedded in a {@link LambdaToStringException}.
     *
     * @param strategyClassName Class name of {@link LambdaToStringStrategy} to link with
     * @return the CallSite whose target can be used to create a lambda <code>toString</code>
     * @throws LambdaToStringException if an error occurs while instantiating the new strategy
     */
    public static CallSite link(MethodHandles.Lookup caller,
            String invokedName,
            MethodType invokedType,
            String strategyClassName)
            throws LambdaToStringException {
        LambdaToStringStrategy strategy;
        try {
            strategy = createStrategy(strategyClassName);
        } catch (ReflectiveOperationException e) {
            throw new LambdaToStringException("Exception instantiating toString strategy object", e);
        }
        MethodHandle mh = MethodHandles.constant(LambdaToStringStrategy.class, strategy);
        return new ConstantCallSite(mh);
    }

    static LambdaToStringStrategy linkStrategy(String strategyClassName) throws BootstrapMethodError {
        return LINKED_STRATEGY.computeIfAbsent(strategyClassName, name -> {
            try {
                return createStrategy(name);
            } catch (ReflectiveOperationException e) {
                throw new BootstrapMethodError(e);
            }
        });
    }

    private static LambdaToStringStrategy createStrategy(String strategyClassName) throws
            ReflectiveOperationException {
        ClassLoader classLoader = LambdaToStringLinker.class.getClassLoader();

        Class<?> klass = classLoader.loadClass(strategyClassName);
        if (!LambdaToStringStrategy.class.isAssignableFrom(klass)) {
            throw new ReflectiveOperationException(LambdaToStringStrategy.class + " is not assignable from given class " + klass);
        }
        @SuppressWarnings("unchecked") Class<LambdaToStringStrategy> castKlass = (Class<LambdaToStringStrategy>) klass;

        Constructor<LambdaToStringStrategy> constructor = castKlass.getDeclaredConstructor();
        try {
            constructor.setAccessible(true);
        } catch (SecurityException e) {
            throw new ReflectiveOperationException("No accessible constructor in " + strategyClassName + " class", e);
        }
        return constructor.newInstance();
    }

}
