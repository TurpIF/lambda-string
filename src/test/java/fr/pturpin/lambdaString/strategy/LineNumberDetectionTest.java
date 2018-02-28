package fr.pturpin.lambdaString.strategy;

import com.ea.agentloader.AgentLoader;
import fr.pturpin.lambdaString.StaticLambdaHolder;
import fr.pturpin.lambdaString.StaticMethodRefHolder;
import fr.pturpin.lambdaString.agent.LambdaToStringAgent;
import fr.pturpin.lambdaString.transform.LambdaMetaInfo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class LineNumberDetectionTest {

    @BeforeAll
    static void beforeAll() {
        AgentLoader.loadAgentClass(LambdaToStringAgent.class.getName(), OnlyLineToStringStrategy.class.getName());
    }

    @Test
    void testStaticFinalLambda() throws Exception {
        assertThat(StaticLambdaHolder.STATIC_FINAL_LAMBDA.toString()).isEqualTo("14");
    }

    @Test
    void testStaticFinalMultilineLambda() throws Exception {
        assertThat(StaticLambdaHolder.STATIC_FINAL_MULTILINE_LAMBDA.toString()).isEqualTo("17");
    }

    @Test
    void testStaticFinalLambdaInStaticBlock() throws Exception {
        assertThat(StaticLambdaHolder.STATIC_FINAL_LAMBDA_IN_STATIC_BLOCK.toString()).isEqualTo("49");
    }

    @Test
    void testStaticLambda() throws Exception {
        assertThat(StaticLambdaHolder.STATIC_LAMBDA.toString()).isEqualTo("23");
    }

    @Test
    void testStaticReassignedLambda() throws Exception {
        assertThat(StaticLambdaHolder.STATIC_REASSIGNED_LAMBDA.toString()).isEqualTo("52");
    }

    @Test
    void testStaticLambdaFromAnonymous() throws Exception {
        assertThat(StaticLambdaHolder.STATIC_LAMBDA_FROM_ANONYMOUS.toString()).isEqualTo("59");
    }

    @Test
    void testStaticLambdaFromLambda() throws Exception {
        assertThat(StaticLambdaHolder.STATIC_LAMBDA_FROM_LAMBDA.toString()).isEqualTo("69");
    }

    @Test
    void testLambdaFromStaticInnerClass() throws Exception {
        assertThat(StaticLambdaHolder.LAMBDA_FROM_STATIC_INNER_CLASS.toString()).isEqualTo("82");
    }

    @Test
    void testLambdaDeclaredInConstructorOfStaticInnerClass() throws Exception {
        assertThat(StaticLambdaHolder.LAMBDA_DECLARED_IN_CONSTRUCTOR_OF_STATIC_INNER_CLASS.toString()).isEqualTo("90");
    }

    @Test
    void testLambdaGivenToConstructorOfStaticInnerClass() throws Exception {
        assertThat(StaticLambdaHolder.LAMBDA_GIVEN_TO_CONSTRUCTOR_OF_STATIC_INNER_CLASS.toString()).isEqualTo("34");
    }

    @Test
    void testLambdaDeclaredInConstructorOfInnerClass() throws Exception {
        assertThat(StaticLambdaHolder.LAMBDA_DECLARED_IN_CONSTRUCTOR_OF_INNER_CLASS.toString()).isEqualTo("103");
    }

    @Test
    void testLambdaGivenToConstructorOfInnerClass() throws Exception {
        assertThat(StaticLambdaHolder.LAMBDA_GIVEN_TO_CONSTRUCTOR_OF_INNER_CLASS.toString()).isEqualTo("77");
    }

    @Test
    void testStaticMethodFromLambdaAnonymous() throws Exception {
        assertThat(StaticMethodRefHolder.STATIC_METHOD_FROM_LAMBDA_ANONYMOUS.toString()).isEqualTo("82");
    }

    @Test
    void testStaticMethodFromLambda() throws Exception {
        // Unsupported
        assertThat(StaticMethodRefHolder.STATIC_METHOD_FROM_LAMBDA.toString()).isEqualTo("-1");
    }

    @Test
    void testStaticMethodFromMethod() throws Exception {
        // Unsupported
        assertThat(StaticMethodRefHolder.STATIC_METHOD_FROM_METHOD.toString()).isEqualTo("-1");
    }

    @Test
    void testStaticMethodFromOtherKindOfAnonymous() throws Exception {
        // Unsupported
        assertThat(StaticMethodRefHolder.STATIC_METHOD_FROM_OTHER_KIND_OF_ANONYMOUS.toString()).isEqualTo("102");
    }

    @Test
    void testStaticMethodFromOtherKindOfLambda() throws Exception {
        // Unsupported
        assertThat(StaticMethodRefHolder.STATIC_METHOD_FROM_OTHER_KIND_OF_LAMBDA.toString()).isEqualTo("-1");
    }

    @Test
    void testStaticReassignedMethodRef() throws Exception {
        assertThat(StaticMethodRefHolder.STATIC_REASSIGNED_METHOD_REF.toString()).isEqualTo("20");
    }

    @Test
    void testStaticMethodRef() throws Exception {
        assertThat(StaticMethodRefHolder.STATIC_METHOD_REF.toString()).isEqualTo("12");
    }

    @Test
    void testStaticLinedMethod() throws Exception {
        assertThat(StaticMethodRefHolder.STATIC_LINED_METHOD.toString()).isEqualTo("15");
    }

    @Test
    void testStaticMultilineMethod() throws Exception {
        assertThat(StaticMethodRefHolder.STATIC_MULTILINE_METHOD.toString()).isEqualTo("20");
    }

    @Test
    void testInstanceMethod() throws Exception {
        assertThat(StaticMethodRefHolder.INSTANCE_METHOD.toString()).isEqualTo("25");
    }

    @Test
    void testStaticMethodInStaticInner() throws Exception {
        assertThat(StaticMethodRefHolder.STATIC_METHOD_IN_STATIC_INNER.toString()).isEqualTo("116");
    }

    @Test
    void testInstanceMethodInStaticInner() throws Exception {
        assertThat(StaticMethodRefHolder.INSTANCE_METHOD_IN_STATIC_INNER.toString()).isEqualTo("119");
    }

    @Test
    void testInstanceMethodInInstanceInner() throws Exception {
        assertThat(StaticMethodRefHolder.INSTANCE_METHOD_IN_INSTANCE_INNER.toString()).isEqualTo("122");
    }

    @Test
    void testDefaultMethodInInterface() throws Exception {
        assertThat(StaticMethodRefHolder.DEFAULT_METHOD_IN_INTERFACE.toString()).isEqualTo("48");
    }

    @Test
    void testStaticMethodInInterface() throws Exception {
        assertThat(StaticMethodRefHolder.STATIC_METHOD_IN_INTERFACE.toString()).isEqualTo("51");
    }

    @Test
    void testOverriddenDefaultMethod() throws Exception {
        assertThat(StaticMethodRefHolder.OVERRIDDEN_DEFAULT_METHOD.toString()).isEqualTo("56");
    }

    @Test
    void testStaticMethodInStaticInnerRefFromStaticInner() throws Exception {
        assertThat(StaticMethodRefHolder.STATIC_METHOD_IN_STATIC_INNER_REF_FROM_STATIC_INNER.toString()).isEqualTo("33");
    }

    @Test
    void testStaticMethodFromStaticInner() throws Exception {
        assertThat(StaticMethodRefHolder.STATIC_METHOD_FROM_STATIC_INNER.toString()).isEqualTo("30");
    }

    @Test
    void testStaticMethodInInterfaceFromImpl() throws Exception {
        assertThat(StaticMethodRefHolder.STATIC_METHOD_IN_INTERFACE_FROM_IMPL.toString()).isEqualTo("51");
    }

    @SuppressWarnings({"Convert2MethodRef", "ResultOfMethodCallIgnored"})
    @Test
    void testLineDeclarationOnMethodRefOnBootstrapClasses() throws Exception {
        ClassLoader stringClassLoader = String.class.getClassLoader();
        assertThat(stringClassLoader).as("String.class is not loaded in bootstrap class loader").isNull();

        String dummy = "dummy";
        Predicate lambda = dummy::equals;
        assertThatCode(lambda::toString).doesNotThrowAnyException();
        assertThatCode(() -> lambda.toString()).doesNotThrowAnyException();
    }

    private static final class OnlyLineToStringStrategy implements LambdaToStringStrategy {
        @Override
        public String createToString(Object lambda, LambdaMetaInfo metaInfo) throws LambdaToStringException {
            return String.valueOf(metaInfo.getDeclarationLine().orElse(-1));
        }
    }

}
