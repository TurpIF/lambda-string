package fr.pturpin.lambdastring.strategy;

import fr.pturpin.lambdastring.StaticLambdaHolder;
import fr.pturpin.lambdastring.StaticMethodRefHolder;
import fr.pturpin.lambdastring.agent.LambdaAgentLoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultToStringStrategyTest {

    @BeforeAll
    static void beforeAll() {
        LambdaAgentLoader.loadAgent(DefaultToStringStrategy.class.getName());
    }

    @Test
    void testStaticLambda() throws Exception {
        assertThat(StaticLambdaHolder.STATIC_FINAL_LAMBDA.toString()).isEqualTo("StaticLambdaHolder:14");
    }

    @Test
    void testLambdaInStaticInnerClass() throws Exception {
        assertThat(StaticLambdaHolder.LAMBDA_DECLARED_IN_CONSTRUCTOR_OF_STATIC_INNER_CLASS.toString()).isEqualTo(
                "StaticLambdaHolder$StaticInnerLambdaHolder:90");
    }

    @Test
    void testLambdaInInnerClass() throws Exception {
        assertThat(StaticLambdaHolder.LAMBDA_DECLARED_IN_CONSTRUCTOR_OF_INNER_CLASS.toString()).isEqualTo(
                "StaticLambdaHolder$InnerLambdaHolder:103");
    }

    @Test
    void testLambdaFromAnonymous() throws Exception {
        assertThat(StaticLambdaHolder.STATIC_LAMBDA_FROM_ANONYMOUS.toString()).isEqualTo("StaticLambdaHolder$1:59");
    }

    @Test
    void testLambdaFromLambda() throws Exception {
        assertThat(StaticLambdaHolder.STATIC_LAMBDA_FROM_LAMBDA.toString()).isEqualTo("StaticLambdaHolder:69");
    }

    @Test
    void testStaticMethodRef() throws Exception {
        assertThat(StaticMethodRefHolder.STATIC_METHOD_REF.toString()).isEqualTo(
                "StaticMethodRefHolder::staticMethod:12");
    }

    @Test
    void testStaticMethodInStaticInnerClassFromThisStaticInnerClass() throws Exception {
        assertThat(StaticMethodRefHolder.STATIC_METHOD_IN_STATIC_INNER_REF_FROM_STATIC_INNER.toString()).isEqualTo(
                "StaticMethodRefHolder$StaticInnerHolder::staticMethod:33");
    }

    @Test
    void testStaticMethodInStaticInnerClassFromOutsideStaticInnerClass() throws Exception {
        assertThat(StaticMethodRefHolder.STATIC_METHOD_IN_STATIC_INNER.toString()).isEqualTo(
                "StaticMethodRefHolder:116");
    }

    @Test
    void testInstanceMethodInStaticInnerClassFromOutsideStaticInnerClass() throws Exception {
        assertThat(StaticMethodRefHolder.INSTANCE_METHOD_IN_STATIC_INNER.toString()).isEqualTo(
                "StaticMethodRefHolder:119");
    }

    @Test
    void testStaticMethodInInterfaceFromImpl() throws Exception {
        assertThat(StaticMethodRefHolder.STATIC_METHOD_IN_INTERFACE_FROM_IMPL.toString()).isEqualTo(
                "StaticMethodRefHolder$InterfaceInnerHolder::staticMethod:51");
    }

    @Test
    void testStaticMethodInInterface() throws Exception {
        assertThat(StaticMethodRefHolder.STATIC_METHOD_IN_INTERFACE.toString()).isEqualTo(
                "StaticMethodRefHolder$InterfaceInnerHolder::staticMethod:51");
    }

    @Test
    void testOverriddenDefaultMethod() throws Exception {
        assertThat(StaticMethodRefHolder.OVERRIDDEN_DEFAULT_METHOD.toString()).isEqualTo(
                "StaticMethodRefHolder$OverriddenDefaultMethodHolder::defaultMethod:56");
    }

    @Test
    void testDefaultMethodInInterface() throws Exception {
        assertThat(StaticMethodRefHolder.DEFAULT_METHOD_IN_INTERFACE.toString()).isEqualTo(
                "StaticMethodRefHolder$InterfaceInnerHolder::defaultMethod:48");
    }

    @Test
    void testMethodRefWithArgument() throws Exception {
        FooInterface foo = DefaultToStringStrategyTest::foo;
        assertThat(foo.toString()).isEqualTo("DefaultToStringStrategyTest::foo:107");
    }

    private interface FooInterface {
        @SuppressWarnings("unused")
        Object bar(int n, String s);
    }

    // Position sensitive
    @SuppressWarnings("unused")
    private static Object foo(int n, String s) {
        return null;
    }

}
