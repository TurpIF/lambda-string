package fr.pturpin.lambdaString;

import com.ea.agentloader.AgentLoader;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class LambdaToStringAgentTest {

    @Test
    void agentIsLoadableOnlyOnce() throws Exception {
        AgentLoader.loadAgentClass(LambdaToStringAgent.class.getName(), EmptyToStringStrategy.class.getName());

        // Calling again with the same argument is allowed
        AgentLoader.loadAgentClass(LambdaToStringAgent.class.getName(), EmptyToStringStrategy.class.getName());

        // Calling again with an other argument is forbidden
        Assertions.assertThatThrownBy(() -> AgentLoader.loadAgentClass(LambdaToStringAgent.class.getName(),
                EmptyToStringStrategyBis.class.getName()));
    }

    private static final class EmptyToStringStrategy implements LambdaToStringStrategy {
        @Override
        public String createToString(Object lambda) {
            return "";
        }
    }

    private static final class EmptyToStringStrategyBis implements LambdaToStringStrategy {
        @Override
        public String createToString(Object lambda) {
            return "";
        }
    }

}
