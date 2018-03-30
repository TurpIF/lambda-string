package fr.pturpin.lambdastring.agent;

import fr.pturpin.lambdastring.LambdaTestHolder;
import fr.pturpin.lambdastring.LambdaTestHolder.Lambda;
import fr.pturpin.lambdastring.strategy.LambdaToStringStrategy;
import fr.pturpin.lambdastring.transform.LambdaMetaInfo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static fr.pturpin.lambdastring.LambdaTestHolder.defaultToString;
import static org.assertj.core.api.Assertions.assertThat;

class LambdaToStringAgentIT {

    private static final String INJECTED_TO_STRING = "injected";

    @BeforeAll
    static void beforeAll() {
        LambdaAgentLoader.loadAgent(ITLambdaToStringStrategy.class.getName());
    }

    @Test
    void classLoadedFromBootstrapClassLoaderAreNotSupported() throws Exception {
        assertThat(Function.class.getClassLoader())
                .as("%s is loaded by the bootstrap class loader", Function.class)
                .isNull();

        String expected = defaultToString(Function.identity());
        assertThat(Function.identity().toString()).isEqualTo(expected);
    }

    @Test
    void lambdaFromInstanceMethod() {
        Lambda lambda = () -> {};
        assertThat(lambda.toString()).isEqualTo(INJECTED_TO_STRING);
    }

    @Test
    void methodRefFromInstanceMethod() {
        Lambda methodRef = LambdaTestHolder::body;
        assertThat(methodRef.toString()).isEqualTo(INJECTED_TO_STRING);
    }

    static final class ITLambdaToStringStrategy implements LambdaToStringStrategy {
        @Override
        public String createToString(Object lambda, LambdaMetaInfo metaInfo) {
            return INJECTED_TO_STRING;
        }
    }
}
