package fr.pturpin.lambdastring.agent;

import fr.pturpin.lambdastring.StaticLambdaHolder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class DefaultLambdaToStringAgent_MT {

    @Test
    void agentShouldUseDefaultStrategyIfNoneIsGiven() throws Exception {
        assertThatCode(() -> LambdaAgentLoader.loadAgent("")).doesNotThrowAnyException();
        assertThat(StaticLambdaHolder.STATIC_LAMBDA.toString()).startsWith("StaticLambdaHolder");
    }

}
