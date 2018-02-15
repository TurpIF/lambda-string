package fr.pturpin.lambdaString;

import com.ea.agentloader.AgentLoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

class LambdaStringTest {

  private static final String INJECTED_TO_STRING = "toto";

  @BeforeAll
  static void beforeAll() {
    AgentLoader.loadAgentClass(LambdaToStringAgent.class.getName(), "");
  }

  @Test
  void lambdaFromInstanceMethod() {
    Predicate<Object> lambdaName = e -> true;

    assertThat(lambdaName.toString()).isEqualTo(INJECTED_TO_STRING);
  }

  @Test
  void methodRefFromInstanceMethod() {
    Predicate<Object> methodRefName = "test"::equals;

    assertThat(methodRefName.toString()).isEqualTo(INJECTED_TO_STRING);
  }

}
