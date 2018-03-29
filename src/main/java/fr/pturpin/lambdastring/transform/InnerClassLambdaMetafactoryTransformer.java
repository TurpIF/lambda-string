package fr.pturpin.lambdastring.transform;

import fr.pturpin.lambdastring.asm.InjectingToStringClassVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.function.UnaryOperator;

import static java.util.Objects.requireNonNull;

public final class InnerClassLambdaMetafactoryTransformer implements ClassFileTransformer {

    private final String toStringStrategyClassName;
    private final UnaryOperator<ClassVisitor> classVisitorDecorator;

    public InnerClassLambdaMetafactoryTransformer(String toStringStrategyClassName) {
        this(toStringStrategyClassName, UnaryOperator.identity());
    }

    InnerClassLambdaMetafactoryTransformer(
            String toStringStrategyClassName,
            UnaryOperator<ClassVisitor> classVisitorDecorator) {
        this.toStringStrategyClassName = requireNonNull(toStringStrategyClassName);
        this.classVisitorDecorator = requireNonNull(classVisitorDecorator);
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (className.equals("java/lang/invoke/InnerClassLambdaMetafactory")) {
            ClassReader cr = new ClassReader(classfileBuffer);
            ClassWriter cw = new ClassWriter(cr, 0);
            cr.accept(new InjectingToStringClassVisitor(classVisitorDecorator.apply(cw), toStringStrategyClassName), 0);
            return cw.toByteArray();
        }
        return null;
    }

}
