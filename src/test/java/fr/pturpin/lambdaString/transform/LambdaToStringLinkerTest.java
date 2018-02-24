package fr.pturpin.lambdaString.transform;

import com.ea.agentloader.AgentLoader;
import fr.pturpin.lambdaString.LambdaTestHolder;
import fr.pturpin.lambdaString.LambdaTestHolder.Lambda;
import fr.pturpin.lambdaString.agent.LambdaToStringAgent;
import fr.pturpin.lambdaString.strategy.LambdaToStringStrategy;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LambdaToStringLinkerTest {

    @BeforeAll
    static void beforeAll() {
        AgentLoader.loadAgentClass(LambdaToStringAgent.class.getName(), CountingCallsToStringStrategy.class.getName());
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
