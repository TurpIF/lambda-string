package fr.pturpin.lambdaString;

import java.lang.instrument.Instrumentation;

public final class LambdaToStringAgent {
  public static void agentmain(String agentArgs, Instrumentation inst) {
    premain(agentArgs, inst);
  }

  public static void premain(String agentArgs, Instrumentation inst) {
    inst.addTransformer(new InnerClassLambdaMetafactoryTransformer(), true);
    try {
      inst.retransformClasses(Class.forName("java.lang.invoke.InnerClassLambdaMetafactory"));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
