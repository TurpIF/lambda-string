package fr.pturpin.lambdaString.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static java.util.Objects.requireNonNull;

public final class InjectingToStringClassVisitor extends ClassVisitor {

    private static final String SPIN_INNER_CLASS_METHOD = "spinInnerClass";

    private final String toStringStrategyClassName;

    public InjectingToStringClassVisitor(ClassVisitor cw, String toStringStrategyClassName) {
        super(Opcodes.ASM5, cw);
        this.toStringStrategyClassName = requireNonNull(toStringStrategyClassName);
    }

    @Override
    public MethodVisitor visitMethod(
            int access,
            String name,
            String desc,
            String signature,
            String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access,
                name,
                desc,
                signature,
                exceptions);

        if (SPIN_INNER_CLASS_METHOD.equals(name)) {
            return new InjectingToStringMethodVisitor(mv, toStringStrategyClassName);
        }
        return mv;
    }
}
