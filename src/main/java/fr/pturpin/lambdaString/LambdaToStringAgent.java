package fr.pturpin.lambdaString;

import java.lang.instrument.Instrumentation;

/**
 * Agent transforming the {@link java.lang.invoke.InnerClassLambdaMetafactory} class to inject a custom
 * {@link Object#toString()} to all created lambdas.
 * <p>
 * If this agent is dynamically set up during runtime, already created lambdas are not modified and keep their
 * default <code>toString</code>.
 * </p><p>
 * Lambda in classes loaded during bootstrap class loader are not supported except if this agent classes are included
 * in the bootstrap classpath via <code>-Xbootclasspath/p:&lt;path/to/agent/jar&gt;</code>.<br />
 * If this property is not set, then the explicit {@link Object#toString()} implementation is injected in all lambdas
 * <code>toString</code> from bootstrap. This explicit implementation is:<br />
 * <code>return getClass().getName() + "@" Integer.toHexString(hashCode());</code>
 * </p>
 */
public final class LambdaToStringAgent {
    public static void agentmain(String agentArgs, Instrumentation inst) {
        premain(agentArgs, inst);
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new InnerClassLambdaMetafactoryTransformer(), true);
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
