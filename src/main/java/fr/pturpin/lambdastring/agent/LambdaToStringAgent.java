package fr.pturpin.lambdastring.agent;

import fr.pturpin.lambdastring.strategy.DefaultToStringStrategy;
import fr.pturpin.lambdastring.strategy.LambdaToStringStrategy;
import fr.pturpin.lambdastring.transform.InnerClassLambdaMetafactoryTransformer;
import fr.pturpin.lambdastring.transform.LambdaToStringLinker;
import fr.pturpin.lambdastring.transform.LambdaToStringLinkerException;

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
 * <p>
 * If no class parameter is given, this agent use the {@link DefaultToStringStrategy} strategy.
 * <p>
 * If an error occurs because the given strategy class name is invalid, a {@link RuntimeException} containing the
 * {@link LambdaToStringLinkerException} cause is thrown while loading this agent.
 * <p>
 * If this agent is dynamically set up during runtime, already created lambdas are not modified and keep their
 * default <code>toString</code>.
 * <p>
 * Lambda in classes loaded during bootstrap class loader are not supported except if the given
 * {@link LambdaToStringStrategy} class is included in the bootstrap classpath via
 * <code>-Xbootclasspath/p:&lt;path/to/agent/jar&gt;</code>.<br>
 * If this property is not set, then the explicit {@link Object#toString()} implementation is injected in all lambdas
 * <code>toString</code> from bootstrap. This explicit implementation is:<br>
 * <code>return getClass().getName() + "@" Integer.toHexString(hashCode());</code>
 * <p>
 * This agent is runnable only once in the same JVM. A {@link IllegalStateException} is thrown in case of multiple run.
 */
public final class LambdaToStringAgent {

    private static final AtomicReference<String> initializedArgs = new AtomicReference<>(null);

    public static void agentmain(String agentArgs, Instrumentation inst) {
        premain(agentArgs, inst);
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        if (agentArgs == null || agentArgs.isEmpty()) {
            agentArgs = DefaultToStringStrategy.class.getName();
        }

        try {
            // Check validity
            LambdaToStringLinker.createStrategy(agentArgs);
        } catch (LambdaToStringLinkerException e) {
            throw new RuntimeException(e);
        }

        if (!initializedArgs.compareAndSet(null, agentArgs)) {
            String args = initializedArgs.get();
            if (args.equals(agentArgs)) {
                // Already initialized with same args
                return;
            }
            throw new IllegalStateException("This agent is runnable only once but was already ran with " + args + " as argument.");
        }

        Class<?> metaFactoryClass;
        try {
            // Make sure it's already loaded, so Instrumentation#retransformClasses does not throw a ClassCircularityError
            metaFactoryClass = Class.forName("java.lang.invoke.InnerClassLambdaMetafactory");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Lambda meta factory not found", e);
        }

        inst.addTransformer(new InnerClassLambdaMetafactoryTransformer(agentArgs), true);
        try {
            inst.retransformClasses(metaFactoryClass);
            // Impossible to retransform the already created lambda classes.
            // On JDK8: there is a bug in HotSpot implementation: https://bugs.openjdk.java.net/browse/JDK-8145964
            // On JDK9: anonymous classes (and so lambda classes) are not Instrumentation.isModifiableClass
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
