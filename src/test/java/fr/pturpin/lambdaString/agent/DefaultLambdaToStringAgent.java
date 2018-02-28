package fr.pturpin.lambdaString.agent;

import com.ea.agentloader.AgentLoader;
import fr.pturpin.lambdaString.StaticLambdaHolder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class DefaultLambdaToStringAgent {

    @Test
    void agentShouldUseDefaultStrategyIfNoneIsGiven() throws Exception {
        assertThatCode(() -> AgentLoader.loadAgentClass(LambdaToStringAgent.class.getName(),
                "")).doesNotThrowAnyException();
        assertThat(StaticLambdaHolder.STATIC_LAMBDA.toString()).startsWith("StaticLambdaHolder");
    }

}
