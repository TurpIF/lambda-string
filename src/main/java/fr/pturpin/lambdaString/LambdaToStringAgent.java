package fr.pturpin.lambdaString;

import java.lang.instrument.Instrumentation;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Agent transforming the <code>InnerClassLambdaMetafactory</code> class to inject a custom
 * {@link Object#toString()} to all created lambdas.
 * <p>
 * The injected <code>toString</code> is represented by a {@link LambdaToStringStrategy} class whose name is given as
 * an agent parameters. The class should have a default constructor. If the constructor is not visible, the agent try
 * to {@link java.lang.reflect.Constructor#setAccessible(boolean) set it accessible} but may failed because of a
 * {@link SecurityManager}.
 * </p><p>
 * If no class parameter is given, this agent does nothing and returns silently.
 * </p><p>
 * If this agent is dynamically set up during runtime, already created lambdas are not modified and keep their
 * default <code>toString</code>.
 * </p><p>
 * Lambda in classes loaded during bootstrap class loader are not supported except if the given
 * {@link LambdaToStringStrategy} class is included in the bootstrap classpath via
 * <code>-Xbootclasspath/p:&lt;path/to/agent/jar&gt;</code>.<br>
 * If this property is not set, then the explicit {@link Object#toString()} implementation is injected in all lambdas
 * <code>toString</code> from bootstrap. This explicit implementation is:<br>
 * <code>return getClass().getName() + "@" Integer.toHexString(hashCode());</code>
 * </p><p>
 * This agent is runnable only once in the same JVM. A {@link IllegalStateException} is thrown in case of multiple run.
 * </p>
 */
public final class LambdaToStringAgent {

    private static final AtomicReference<String> initializedArgs = new AtomicReference<>(null);

    public static void agentmain(String agentArgs, Instrumentation inst) {
        premain(agentArgs, inst);
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        if (agentArgs == null || agentArgs.isEmpty()) {
            return;
        }

        if (!initializedArgs.compareAndSet(null, agentArgs)) {
            String args = initializedArgs.get();
            if (args.equals(agentArgs)) {
                // Already initialized with same args
                return;
            }
            throw new IllegalStateException("This agent is runnable only once but was already ran with " + args + " as argument.");
        }

        inst.addTransformer(new InnerClassLambdaMetafactoryTransformer(agentArgs), true);
        try {
            inst.retransformClasses(Class.forName("java.lang.invoke.InnerClassLambdaMetafactory"));
            // Impossible to retransform the already created lambda classes.
            // On JDK8: there is a bug in HotSpot implementation: https://bugs.openjdk.java.net/browse/JDK-8145964
            // On JDK9: anonymous classes (and so lambda classes) are not Instrumentation.isModifiableClass
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
