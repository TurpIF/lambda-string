package fr.pturpin.lambdastring.agent;

import fr.pturpin.lambdastring.strategy.LambdaToStringStrategy;
import fr.pturpin.lambdastring.transform.LambdaMetaInfo;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.jar.JarFile;

class LambdaToStringAgentTest {

    @Test
    void agentShouldThrowIfItsInvalid() throws Exception {
        Assertions.assertThatThrownBy(() -> loadAgent("unexisting agent class"));
    }

    @Test
    void agentIsLoadableOnlyOnce() throws Exception {
        loadAgent(EmptyToStringStrategy.class.getName());

        // Calling again with the same argument is allowed
        loadAgent(EmptyToStringStrategy.class.getName());

        // Calling again with an other argument is forbidden
        Assertions.assertThatThrownBy(() -> loadAgent(EmptyToStringStrategyBis.class.getName()));
    }

    private static void loadAgent(String agentArgs) {
        LambdaToStringAgent.premain(agentArgs, new DummyInstrumentation());
    }

    private static final class EmptyToStringStrategy implements LambdaToStringStrategy {
        @Override
        public String createToString(Object lambda, LambdaMetaInfo metaInfo) {
            return "";
        }
    }

    private static final class EmptyToStringStrategyBis implements LambdaToStringStrategy {
        @Override
        public String createToString(Object lambda, LambdaMetaInfo metaInfo) {
            return "";
        }
    }

    private static final class DummyInstrumentation implements Instrumentation {

        @Override
        public void addTransformer(ClassFileTransformer classFileTransformer, boolean b) {

        }

        @Override
        public void addTransformer(ClassFileTransformer classFileTransformer) {

        }

        @Override
        public boolean removeTransformer(ClassFileTransformer classFileTransformer) {
            return false;
        }

        @Override
        public boolean isRetransformClassesSupported() {
            return false;
        }

        @Override
        public void retransformClasses(Class<?>... classes) throws UnmodifiableClassException {

        }

        @Override
        public boolean isRedefineClassesSupported() {
            return false;
        }

        @Override
        public void redefineClasses(ClassDefinition... classDefinitions) throws ClassNotFoundException, UnmodifiableClassException {

        }

        @Override
        public boolean isModifiableClass(Class<?> aClass) {
            return false;
        }

        @Override
        public Class[] getAllLoadedClasses() {
            return new Class[0];
        }

        @Override
        public Class[] getInitiatedClasses(ClassLoader classLoader) {
            return new Class[0];
        }

        @Override
        public long getObjectSize(Object o) {
            return 0;
        }

        @Override
        public void appendToBootstrapClassLoaderSearch(JarFile jarFile) {

        }

        @Override
        public void appendToSystemClassLoaderSearch(JarFile jarFile) {

        }

        @Override
        public boolean isNativeMethodPrefixSupported() {
            return false;
        }

        @Override
        public void setNativeMethodPrefix(ClassFileTransformer classFileTransformer, String s) {

        }
    }

}
