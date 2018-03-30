package fr.pturpin.lambdastring.agent;

import com.ea.agentloader.AgentLoader;
import fr.pturpin.lambdastring.strategy.DefaultToStringStrategy;
import fr.pturpin.lambdastring.strategy.LambdaToStringException;
import fr.pturpin.lambdastring.strategy.LambdaToStringStrategy;
import fr.pturpin.lambdastring.transform.LambdaMetaInfo;
import fr.pturpin.lambdastring.transform.LambdaToStringLinker;
import fr.pturpin.lambdastring.transform.LambdaToStringLinkerException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class LambdaAgentLoader {

    private static final String LOAD_FOR_IT_PROP = "fr.pturpin.lambdastring.test.agent.loader.for-integration";
    private static final boolean LOAD_FOR_IT = Boolean.getBoolean(LOAD_FOR_IT_PROP);

    /**
     * Load agent for unit test or for integration tests.
     * <p>
     * For unit tests, the agent is loaded at runtime and no <code>-javaagent</code> is expected in the invoking
     * command line. For JDK >= 9, the <code>-Djdk.attach.allowAttachSelf=true</code> flag should be set.
     * <p>
     * For integration tests, the unique loaded agent is the {@link DynamicLambdaToStringStrategy} one but each
     * invocation to this method update the delegate of this dynamic strategy. It is expected to set
     * <code>-javaagent:&lt;path-to-agent-jar&gt;=fr.pturpin.lambdastring.agent.LambdaAgentLoader$DynamicLambdaToStringStrategy
     * -Dfr.pturpin.lambdastring.test.agent.loader.for-integration=true</code>
     * in the command line. Also, to detect that the test is ran as an integration test, the {@link #LOAD_FOR_IT_PROP}
     * flag should also be set to <code>true</code>.
     *
     * @param agentArgs agent args corresponding to the {@link LambdaToStringStrategy} class name
     */
    public static void loadAgent(String agentArgs) {
        if (LOAD_FOR_IT) {
            loadAgentForIT(agentArgs);
        } else {
            loadAgentForUT(agentArgs);
        }
    }

    private static void loadAgentForUT(String agentArgs) {
        AgentLoader.loadAgentClass(LambdaToStringAgent.class.getName(), agentArgs);
    }

    private static void loadAgentForIT(String agentArgs) {
        Supplier<LambdaToStringStrategy> newDelegateFactory;
        if (agentArgs == null || agentArgs.isEmpty()) {
            newDelegateFactory = DefaultToStringStrategy::new;
        } else {
            newDelegateFactory = () -> {
                try {
                    return LambdaToStringLinker.createStrategy(agentArgs);
                } catch (LambdaToStringLinkerException e) {
                    throw new RuntimeException("Could not load agent for IT", e);
                }
            };
        }
        DynamicLambdaToStringStrategy.setDelegate(newDelegateFactory);
    }

    private static final class DynamicLambdaToStringStrategy implements LambdaToStringStrategy {

        private static Supplier<LambdaToStringStrategy> delegateFactory = null;
        private static Map<Object, LambdaToStringStrategy> delegateByLambda = new HashMap<>();

        DynamicLambdaToStringStrategy() {
        }

        static void setDelegate(Supplier<LambdaToStringStrategy> newDelegateFactory) {
            delegateFactory = newDelegateFactory;
            delegateByLambda.clear();
        }

        @Override
        public String createToString(Object lambda, LambdaMetaInfo metaInfo) throws LambdaToStringException {
            if (delegateFactory == null) {
                throw new LambdaToStringException("Delegate strategy is not set");
            }
            LambdaToStringStrategy strategy = delegateByLambda.computeIfAbsent(lambda, l -> delegateFactory.get());
            return strategy.createToString(lambda, metaInfo);
        }
    }

}
