package fr.pturpin.lambdaString.transform;

import fr.pturpin.lambdaString.asm.InjectingToStringClassVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

import static java.util.Objects.requireNonNull;

public final class InnerClassLambdaMetafactoryTransformer implements ClassFileTransformer {

    private final String toStringStrategyClassName;

    public InnerClassLambdaMetafactoryTransformer(String toStringStrategyClassName) {
        this.toStringStrategyClassName = requireNonNull(toStringStrategyClassName);
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (className.equals("java/lang/invoke/InnerClassLambdaMetafactory")) {
            ClassReader cr = new ClassReader(classfileBuffer);
            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
            cr.accept(new InjectingToStringClassVisitor(cw, toStringStrategyClassName), 0);
            return cw.toByteArray();
        }
        return null;
    }

}
