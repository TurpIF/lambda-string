package fr.pturpin.lambdastring.agent;

import fr.pturpin.lambdastring.strategy.LambdaToStringStrategy;
import fr.pturpin.lambdastring.transform.LambdaMetaInfo;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

class LambdaToStringAgent_UT {

    @Test
    void agentShouldThrowIfItsInvalid() {
        Assertions.assertThatThrownBy(() -> loadAgent("unexisting agent class")).isNotNull();
    }

    @Test
    void agentIsLoadableOnlyOnce() {
        loadAgent(EmptyToStringStrategy.class.getName());

        // Calling again with the same argument is allowed
        loadAgent(EmptyToStringStrategy.class.getName());

        // Calling again with an other argument is forbidden
        Assertions.assertThatThrownBy(() -> loadAgent(EmptyToStringStrategyBis.class.getName())).isNotNull();
    }

    private static void loadAgent(String agentArgs) {
        LambdaToStringAgent.premain(agentArgs, dummyInstrumentation());
    }

    private static final class EmptyToStringStrategy implements LambdaToStringStrategy {
        @Override
        public String createToString(Object lambda, LambdaMetaInfo metaInfo) {
            return "";
        }
    }

    private static final class EmptyToStringStrategyBis implements LambdaToStringStrategy {
        @Override
        public String createToString(Object lambda, LambdaMetaInfo metaInfo) {
            return "";
        }
    }

    private static Instrumentation dummyInstrumentation() {
        // Use a proxy because the JDK9 add a new method in the Instrumentation interface.
        Object proxy = Proxy.newProxyInstance(
            LambdaToStringAgent_UT.class.getClassLoader(),
            new Class[]{Instrumentation.class},
            new NoOpHandler());
        return (Instrumentation) proxy;
    }

    private static final class NoOpHandler implements InvocationHandler {
        public Object invoke(Object proxy, Method method, Object[] args) {
            if (boolean.class.equals(method.getReturnType())) {
                return false;
            }
            return null;
        }
    }

}
