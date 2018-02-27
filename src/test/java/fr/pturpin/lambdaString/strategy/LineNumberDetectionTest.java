package fr.pturpin.lambdaString.strategy;

import com.ea.agentloader.AgentLoader;
import fr.pturpin.lambdaString.StaticLambdaHolder;
import fr.pturpin.lambdaString.agent.LambdaToStringAgent;
import fr.pturpin.lambdaString.transform.LambdaMetaInfo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

    private static final class OnlyLineToStringStrategy implements LambdaToStringStrategy {
        @Override
        public String createToString(Object lambda, LambdaMetaInfo metaInfo) {
            return String.valueOf(metaInfo.getDeclarationLine().orElse(-1));
        }
    }

}
