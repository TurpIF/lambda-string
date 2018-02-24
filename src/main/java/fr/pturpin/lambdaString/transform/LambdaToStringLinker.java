package fr.pturpin.lambdaString.transform;

import fr.pturpin.lambdaString.strategy.LambdaToStringStrategy;

import java.lang.invoke.*;
import java.lang.reflect.Constructor;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

public final class LambdaToStringLinker {

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
     * instantiable default constructor. If any error occurs, it's embedded in a {@link LambdaToStringLinkerException}.
     *
     * @param strategyClassName Class name of {@link LambdaToStringStrategy} to link with
     * @return the CallSite whose target can be used to create a lambda <code>toString</code>
     * @throws LambdaToStringLinkerException if an error occurs while instantiating the new strategy
     */
    public static CallSite link(MethodHandles.Lookup caller,
            String invokedName,
            MethodType invokedType,
            String strategyClassName)
            throws LambdaToStringLinkerException {
        LambdaToStringStrategy strategy = createStrategy(strategyClassName);
        MethodHandle mh = MethodHandles.constant(LambdaToStringStrategy.class, strategy);
        return new ConstantCallSite(mh);
    }

    public static LambdaToStringStrategy createStrategy(String strategyClassName) throws
            LambdaToStringLinkerException {
        ClassLoader classLoader = LambdaToStringLinker.class.getClassLoader();

        Class<?> klass;
        try {
            klass = classLoader.loadClass(strategyClassName);
        } catch (ClassNotFoundException e) {
            throw new LambdaToStringLinkerException(e);
        }

        if (!LambdaToStringStrategy.class.isAssignableFrom(klass)) {
            throw new LambdaToStringLinkerException(LambdaToStringStrategy.class + " is not assignable from given class " + klass);
        }
        @SuppressWarnings("unchecked") Class<LambdaToStringStrategy> castKlass = (Class<LambdaToStringStrategy>) klass;

        Constructor<LambdaToStringStrategy> constructor;
        try {
            constructor = AccessController.doPrivileged((PrivilegedExceptionAction<Constructor<LambdaToStringStrategy>>) () -> {
                Constructor<LambdaToStringStrategy> ctor = castKlass.getDeclaredConstructor();
                ctor.setAccessible(true);
                return ctor;
            });
        } catch (PrivilegedActionException e) {
            throw new LambdaToStringLinkerException("No accessible constructor in " + strategyClassName + " class", e);
        }

        try {
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new LambdaToStringLinkerException("Exception instantiating strategy object", e);
        }
    }

}
