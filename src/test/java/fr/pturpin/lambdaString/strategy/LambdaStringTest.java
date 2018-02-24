package fr.pturpin.lambdaString.strategy;

import com.ea.agentloader.AgentLoader;
import fr.pturpin.lambdaString.LambdaTestHolder;
import fr.pturpin.lambdaString.LambdaTestHolder.Lambda;
import fr.pturpin.lambdaString.agent.LambdaToStringAgent;
import fr.pturpin.lambdaString.transform.LambdaMetaInfo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static fr.pturpin.lambdaString.LambdaTestHolder.defaultToString;
import static org.assertj.core.api.Assertions.assertThat;

class LambdaStringTest {

    private static final String INJECTED_TO_STRING = "toto";

    private static Lambda STATIC_LAMBDA_BEFORE_AGENT;
    private static Lambda STATIC_LAMBDA_AFTER_AGENT;
    private static Lambda STATIC_METHOD_REF_BEFORE_AGENT;
    private static Lambda STATIC_METHOD_REF_AFTER_AGENT;

    @BeforeAll
    static void beforeAll() {
        STATIC_LAMBDA_BEFORE_AGENT = () -> {};
        STATIC_METHOD_REF_BEFORE_AGENT = LambdaTestHolder::body;
        AgentLoader.loadAgentClass(LambdaToStringAgent.class.getName(), TestLambdaToStringStrategy.class.getName());
        STATIC_LAMBDA_AFTER_AGENT = () -> {};
        STATIC_METHOD_REF_AFTER_AGENT = LambdaTestHolder::body;
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
