package fr.pturpin.lambdastring;

import com.ea.agentloader.AgentLoader;
import fr.pturpin.lambdastring.LambdaTestHolder.Lambda;
import fr.pturpin.lambdastring.agent.LambdaToStringAgent;
import fr.pturpin.lambdastring.strategy.LambdaToStringStrategy;
import fr.pturpin.lambdastring.transform.LambdaMetaInfo;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

/**
 * Compare the duration of an injection of a complex string corresponding to the original {@link Object#toString()}
 * implementation.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@Fork(1)
public class ComplexInjectionBenchmark {

    @Setup
    public static void setup() {
        AgentLoader.loadAgentClass(LambdaToStringAgent.class.getName(), ComplexInjectionStrategy.class.getName());
    }

    @State(Scope.Benchmark)
    public static class Data {

        Lambda lambda;
        Lambda methodRef;
        Lambda anonymousClass;
        Lambda staticClass;

        public Data() {
            this.lambda = () -> {};
            this.methodRef = LambdaTestHolder::body;
            this.anonymousClass = newAnonymousLambda();
            this.staticClass = new StaticLambda();
        }
    }

    @Benchmark
    public void staticLambda(Data data, Blackhole bh) {
        bh.consume(data.lambda.toString());
    }

    @Benchmark
    public void staticMethodRef(Data data, Blackhole bh) {
        bh.consume(data.methodRef.toString());
    }

    @Benchmark
    public void staticAnonymousClass(Data data, Blackhole bh) {
        bh.consume(data.anonymousClass.toString());
    }

    @Benchmark
    public void staticStaticClass(Data data, Blackhole bh) {
        bh.consume(data.staticClass.toString());
    }

    @Benchmark
    public void dynamicLambda(Blackhole bh) {
        Lambda lambda = () -> {};
        bh.consume(lambda.toString());
    }

    @Benchmark
    public void dynamicMethodRef(Blackhole bh) {
        Lambda lambda = LambdaTestHolder::body;
        bh.consume(lambda.toString());
    }

    @Benchmark
    public void dynamicAnonymousClass(Blackhole bh) {
        Lambda lambda = newAnonymousLambda();
        bh.consume(lambda.toString());
    }

    @Benchmark
    public void dynamicStaticClass(Blackhole bh) {
        Lambda lambda = new StaticLambda();
        bh.consume(lambda.toString());
    }

    private static Lambda newAnonymousLambda() {
        return new Lambda() {
            @Override
            public void body() {
            }
            @Override
            public String toString() {
                return LambdaTestHolder.defaultToString(this);
            }
        };
    }

    private static final class StaticLambda implements Lambda {
        @Override
        public void body() {
        }

        @Override
        public String toString() {
            return LambdaTestHolder.defaultToString(this);
        }
    }

    @SuppressWarnings("unused")
    private static final class ComplexInjectionStrategy implements LambdaToStringStrategy {
        @Override
        public String createToString(Object lambda, LambdaMetaInfo metaInfo) {
            return LambdaTestHolder.defaultToString(lambda);
        }
    }

}
