package fr.pturpin.lambdastring.transform;

import fr.pturpin.lambdastring.strategy.LambdaToStringStrategy;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

class InnerClassLambdaMetafactoryTransformer_UT {

    @Test
    void testInnerMetafactoryTransformation() throws Exception {
        InnerClassLambdaMetafactoryTransformer transformer = new InnerClassLambdaMetafactoryTransformer(
                DummyStrategy.class.getName(), CheckClassAdapter::new);
        ClassLoader classLoader = getClass().getClassLoader();
        String metafactoryName = "java/lang/invoke/InnerClassLambdaMetafactory";
        Class<?> metafactory = classLoader.loadClass(metafactoryName.replace('/', '.'));
        InputStream is = classLoader.getResourceAsStream(metafactoryName + ".class");
        byte[] metafactoryBuffer = readAll(is);
        byte[] transformed = transformer.transform(classLoader, metafactoryName, metafactory, null, metafactoryBuffer);
        Assertions.assertThat(transformed).isNotNull();
    }

    private static byte[] readAll(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            baos.write(data, 0, nRead);
        }
        baos.flush();
        return baos.toByteArray();
    }

    private static final class DummyStrategy implements LambdaToStringStrategy {
        @Override
        public String createToString(Object lambda, LambdaMetaInfo metaInfo) {
            return null;
        }
    }
}
