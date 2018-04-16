package fr.pturpin.lambdastring;

import fr.pturpin.lambdastring.LambdaTestHolder.Lambda;
import fr.pturpin.lambdastring.agent.LambdaAgentLoader;
import fr.pturpin.lambdastring.strategy.LambdaToStringStrategy;
import fr.pturpin.lambdastring.transform.LambdaMetaInfo;
import org.objectweb.asm.Type;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * Measure the time difference between generating a lambda call site through the {@link LambdaMetafactory} with or
 * without agent.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@Fork(1)
public class LambdaCallSiteGenerationComparisonBenchmark {

    @Param({ "true", "false" })
    public boolean isInjected;

    @Setup
    public void setup() {
        if (isInjected) {
            LambdaAgentLoader.loadAgent(NoOpToStringStrategy.class.getName());
        }
    }

    @State(Scope.Benchmark)
    public static class Data {

        final LambdaGeneration lambdaGeneration;

        public Data() {
            try {
                this.lambdaGeneration = createLambdaGenerationData();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private LambdaGeneration createLambdaGenerationData() throws IllegalAccessException, NoSuchMethodException {
            MethodHandles.Lookup caller = MethodHandles.lookup();
            Method lambdaMethod = Lambda.class.getDeclaredMethods()[0];
            MethodType invokedType = MethodType.methodType(lambdaMethod.getDeclaringClass());
            MethodType methodType = MethodType.fromMethodDescriptorString(
                    Type.getMethodDescriptor(lambdaMethod),
                    OriginalToStringInjectionComparisonBenchmark.class.getClassLoader());
            MethodHandle methodHandle = caller.unreflect(LambdaTestHolder.class.getDeclaredMethod("body"));
            return new LambdaGeneration(
                    caller,
                    lambdaMethod.getName(),
                    invokedType,
                    methodType,
                    methodHandle,
                    methodType);
        }
    }

    @Benchmark
    public void generateLambda(Data data, Blackhole bh) throws LambdaConversionException {
        CallSite callSite = LambdaMetafactory.metafactory(
                data.lambdaGeneration.caller,
                data.lambdaGeneration.invokedName,
                data.lambdaGeneration.invokedType,
                data.lambdaGeneration.samMethodType,
                data.lambdaGeneration.implMethod,
                data.lambdaGeneration.instantiatedMethodType);
        bh.consume(callSite);
    }

    /**
     * Holder of arguments of {@link LambdaMetafactory#metafactory(MethodHandles.Lookup, String, MethodType, MethodType, MethodHandle, MethodType)}
     */
    private static final class LambdaGeneration {
        final MethodHandles.Lookup caller;
        final String invokedName;
        final MethodType invokedType;
        final MethodType samMethodType;
        final MethodHandle implMethod;
        final MethodType instantiatedMethodType;

        private LambdaGeneration(
                MethodHandles.Lookup caller,
                String invokedName,
                MethodType invokedType,
                MethodType samMethodType,
                MethodHandle implMethod,
                MethodType instantiatedMethodType) {
            this.caller = caller;
            this.invokedName = invokedName;
            this.invokedType = invokedType;
            this.samMethodType = samMethodType;
            this.implMethod = implMethod;
            this.instantiatedMethodType = instantiatedMethodType;
        }
    }

    @SuppressWarnings("unused")
    private static final class NoOpToStringStrategy implements LambdaToStringStrategy {
        @Override
        public String createToString(Object lambda, LambdaMetaInfo metaInfo) {
            return null;
        }
    }

}
