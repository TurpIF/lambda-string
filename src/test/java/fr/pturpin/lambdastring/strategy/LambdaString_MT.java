package fr.pturpin.lambdastring.strategy;

import fr.pturpin.lambdastring.LambdaTestHolder;
import fr.pturpin.lambdastring.LambdaTestHolder.Lambda;
import fr.pturpin.lambdastring.agent.LambdaAgentLoader;
import fr.pturpin.lambdastring.transform.LambdaMetaInfo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static fr.pturpin.lambdastring.LambdaTestHolder.defaultToString;
import static org.assertj.core.api.Assertions.assertThat;

class LambdaString_MT {

    private static final String INJECTED_TO_STRING = "toto";

    @BeforeAll
    static void beforeAll() {
        LambdaAgentLoader.loadAgent(TestLambdaToStringStrategy.class.getName());
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
        Lambda lambdaName = () -> {};
        assertThat(lambdaName.toString()).isEqualTo(INJECTED_TO_STRING);
    }

    @Test
    void methodRefFromInstanceMethod() {
        Lambda methodRefName = LambdaTestHolder::body;
        assertThat(methodRefName.toString()).isEqualTo(INJECTED_TO_STRING);
    }

    private static final class TestLambdaToStringStrategy implements LambdaToStringStrategy {
        @Override
        public String createToString(Object lambda, LambdaMetaInfo metaInfo) {
            return INJECTED_TO_STRING;
        }
    }

}
