package fr.pturpin.lambdaString;

import com.ea.agentloader.AgentLoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

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
        STATIC_METHOD_REF_BEFORE_AGENT = LambdaStringTest::body;
        AgentLoader.loadAgentClass(LambdaToStringAgent.class.getName(), "");
        STATIC_LAMBDA_AFTER_AGENT = () -> {};
        STATIC_METHOD_REF_AFTER_AGENT = LambdaStringTest::body;
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
        Lambda methodRefName = LambdaStringTest::body;
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

    /**
     * Returns the original {@link Object#toString()} of the given object.
     */
    private static String defaultToString(Object object) {
        return object.getClass().getName() + "@" + Integer.toHexString(object.hashCode());
    }


    /**
     * Dummy method whose reference match the {@link Lambda} interface
     */
    private static void body() {
        // nothing
    }

    /**
     * Dummy functional interface to work on in tests
     */
    @FunctionalInterface
    private interface Lambda {
        void body();
    }

}
