package fr.pturpin.lambdastring.strategy;

import fr.pturpin.lambdastring.LambdaTestHolder;
import fr.pturpin.lambdastring.agent.LambdaAgentLoader;
import fr.pturpin.lambdastring.transform.LambdaMetaInfo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static fr.pturpin.lambdastring.LambdaTestHolder.defaultToString;
import static org.assertj.core.api.Assertions.assertThat;

class BeforeAndAfterAgentLambdaString_UT {

    private static final String INJECTED_TO_STRING = "toto";

    private static LambdaTestHolder.Lambda STATIC_LAMBDA_BEFORE_AGENT;
    private static LambdaTestHolder.Lambda STATIC_LAMBDA_AFTER_AGENT;
    private static LambdaTestHolder.Lambda STATIC_METHOD_REF_BEFORE_AGENT;
    private static LambdaTestHolder.Lambda STATIC_METHOD_REF_AFTER_AGENT;

    @BeforeAll
    static void beforeAll() {
        STATIC_LAMBDA_BEFORE_AGENT = () -> {};
        STATIC_METHOD_REF_BEFORE_AGENT = LambdaTestHolder::body;
        LambdaAgentLoader.loadAgent(TestLambdaToStringStrategy.class.getName());
        STATIC_LAMBDA_AFTER_AGENT = () -> {};
        STATIC_METHOD_REF_AFTER_AGENT = LambdaTestHolder::body;
    }

    @Test
    void staticLambdaBeforeAgentAreNotSupported() throws Exception {
        String expected = defaultToString(STATIC_LAMBDA_BEFORE_AGENT);
        assertThat(STATIC_LAMBDA_BEFORE_AGENT.toString()).isEqualTo(expected);
    }

    @Test
    void methodRefLambdaBeforeAgentAreNotSupported() throws Exception {
        String expected = defaultToString(STATIC_METHOD_REF_BEFORE_AGENT);
        assertThat(STATIC_METHOD_REF_BEFORE_AGENT.toString()).isEqualTo(expected);
    }

    @Test
    void staticLambdaAfterAgent() throws Exception {
        assertThat(STATIC_LAMBDA_AFTER_AGENT.toString()).isEqualTo(INJECTED_TO_STRING);
    }

    @Test
    void methodRefLambdaAfterAgent() throws Exception {
        assertThat(STATIC_METHOD_REF_AFTER_AGENT.toString()).isEqualTo(INJECTED_TO_STRING);
    }

    static final class TestLambdaToStringStrategy implements LambdaToStringStrategy {
        @Override
        public String createToString(Object lambda, LambdaMetaInfo metaInfo) {
            return INJECTED_TO_STRING;
        }
    }
}
