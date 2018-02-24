package fr.pturpin.lambdaString;

import fr.pturpin.lambdaString.LambdaStringTest.Lambda;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static fr.pturpin.lambdaString.LambdaStringTest.defaultToString;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * IT of the {@link LambdaToStringAgent} agent.
 * This test should be called with the agent set and with {@link ITLambdaToStringStrategy} class name as parameter :<br>
 * <code>-javaagent:&lt;path-to-agent-jar&gt;=fr.pturpin.lambdaString.LambdaToStringAgentIT$ITLambdaToStringStrategy</code>
 */
class LambdaToStringAgentIT {

    private static final String INJECTED_TO_STRING = "injected";

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
        Lambda methodRef = LambdaStringTest::body;
        assertThat(methodRef.toString()).isEqualTo(INJECTED_TO_STRING);
    }

    static final class ITLambdaToStringStrategy implements LambdaToStringStrategy {
        @Override
        public String createToString(Object lambda, LambdaMetaInfo metaInfo) {
            return INJECTED_TO_STRING;
        }
    }
}
