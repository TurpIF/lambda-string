package fr.pturpin.lambdastring.agent;

import com.ea.agentloader.AgentLoader;

public final class LambdaAgentLoader {

    public static void loadAgent(String agentArgs) {
        AgentLoader.loadAgentClass(LambdaToStringAgent.class.getName(), agentArgs);
    }

}
