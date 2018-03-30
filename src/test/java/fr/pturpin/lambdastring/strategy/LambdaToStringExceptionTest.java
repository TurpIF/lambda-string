package fr.pturpin.lambdastring.strategy;

import fr.pturpin.lambdastring.LambdaTestHolder.Lambda;
import fr.pturpin.lambdastring.agent.LambdaAgentLoader;
import fr.pturpin.lambdastring.transform.LambdaMetaInfo;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class LambdaToStringExceptionTest {

    @BeforeAll
    static void beforeAll() {
        LambdaAgentLoader.loadAgent(ThrowingLambdaToStringStrategy.class.getName());
    }

    @Test
    void exceptionThrownInToStringShouldNotBeIgnored() throws Exception {
        Lambda lambda = () -> {};

        Assertions.assertThatThrownBy(lambda::toString)
                .isInstanceOf(LambdaToStringException.class)
                .hasCause(ThrowingLambdaToStringStrategy.EXCEPTION);
    }

    private static final class ThrowingLambdaToStringStrategy implements LambdaToStringStrategy {

        private static final Exception EXCEPTION = new Exception();

        @Override
        public String createToString(Object lambda, LambdaMetaInfo metaInfo) throws LambdaToStringException {
            throw new LambdaToStringException(EXCEPTION);
        }
    }
}
