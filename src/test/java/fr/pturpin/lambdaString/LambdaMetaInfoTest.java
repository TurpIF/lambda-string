package fr.pturpin.lambdaString;

import com.ea.agentloader.AgentLoader;
import fr.pturpin.lambdaString.LambdaStringTest.Lambda;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LambdaMetaInfoTest {

    @BeforeAll
    static void beforeAll() {
        AgentLoader.loadAgentClass(LambdaToStringAgent.class.getName(), LambdaStringTest.TestLambdaToStringStrategy.class.getName());
    }

    @Test
    void lambdaHasMetaInfo() throws Exception {
        Lambda lambda = () -> {};

        LambdaMetaInfo metaInfo = lambda.getClass().getAnnotation(LambdaMetaInfo.class);
        assertThat(metaInfo).isNotNull();
    }

    @Test
    void methodRefHasMetaInfo() throws Exception {
        Lambda methodRef = LambdaStringTest::body;

        LambdaMetaInfo metaInfo = methodRef.getClass().getAnnotation(LambdaMetaInfo.class);
        assertThat(metaInfo).isNotNull();
    }

    @Test
    void lambdaHasDeclaringClass() throws Exception {
        Lambda lambda = () -> {};

        LambdaMetaInfo metaInfo = lambda.getClass().getAnnotation(LambdaMetaInfo.class);
        assertThat(metaInfo.targetClass()).isEqualTo(LambdaMetaInfoTest.class);
    }

    @Test
    void methodRefHasDeclaringClass() throws Exception {
        Lambda methodRef = LambdaStringTest::body;

        LambdaMetaInfo metaInfo = methodRef.getClass().getAnnotation(LambdaMetaInfo.class);
        assertThat(metaInfo.targetClass()).isEqualTo(LambdaMetaInfoTest.class);
    }
    
}
