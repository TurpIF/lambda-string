package fr.pturpin.lambdastring.transform;

import fr.pturpin.lambdastring.LambdaTestHolder;
import fr.pturpin.lambdastring.LambdaTestHolder.Lambda;
import fr.pturpin.lambdastring.agent.LambdaAgentLoader;
import fr.pturpin.lambdastring.strategy.LambdaToStringStrategy;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LambdaToStringLinker_MT {

    @BeforeAll
    static void beforeAll() {
        LambdaAgentLoader.loadAgent(CountingCallsToStringStrategy.class.getName());
    }

    @Test
    void lambdaStrategyShouldNotBeShared() throws Exception {
        Lambda lambda1 = () -> {};
        Lambda lambda2 = () -> {};

        assertThat(lambda1.toString()).isEqualTo("0");
        assertThat(lambda2.toString()).isEqualTo("0");
    }

    @Test
    void lambdaStrategyShouldBePermanent() throws Exception {
        Lambda lambda = () -> {};

        assertThat(lambda.toString()).isEqualTo("0");
        assertThat(lambda.toString()).isEqualTo("1");
    }

    @Test
    void methodRefStrategyShouldNotBeShared() throws Exception {
        Lambda methodRef1 = LambdaTestHolder::body;
        Lambda methodRef2 = LambdaTestHolder::body;

        assertThat(methodRef1.toString()).isEqualTo("0");
        assertThat(methodRef2.toString()).isEqualTo("0");
    }

    @Test
    void methodRefStrategyShouldBePermanent() throws Exception {
        Lambda methodRef = LambdaTestHolder::body;

        assertThat(methodRef.toString()).isEqualTo("0");
        assertThat(methodRef.toString()).isEqualTo("1");
    }

    private static final class CountingCallsToStringStrategy implements LambdaToStringStrategy {
        private int count = 0;

        @Override
        public String createToString(Object lambda, LambdaMetaInfo metaInfo) {
            return String.valueOf(count++);
        }
    }
}
