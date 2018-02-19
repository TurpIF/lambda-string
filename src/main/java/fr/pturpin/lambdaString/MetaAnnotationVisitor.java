package fr.pturpin.lambdaString;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static java.util.Objects.requireNonNull;

public class MetaAnnotationVisitor extends AnnotationVisitor {

    private static final String NAME_ANNOTATION_VISITOR = "jdk/internal/org/objectweb/asm/AnnotationVisitor";

    private final MethodVisitor mv;

    public MetaAnnotationVisitor(int api, MethodVisitor mv) {
        super(api);
        this.mv = requireNonNull(mv);
    }

    @Override
    public void visit(String name, Object value) {
        visit(name, () -> mv.visitLdcInsn(value));
    }

    public void visit(String name, Runnable valuePusher) {
        mv.visitInsn(Opcodes.DUP);
        mv.visitLdcInsn(name);
        valuePusher.run();
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                NAME_ANNOTATION_VISITOR,
                "visit",
                "(Ljava/lang/String;Ljava/lang/Object;)V",
                false);
    }

    @Override
    public void visitEnum(String name, String desc, String value) {
        mv.visitInsn(Opcodes.DUP);
        mv.visitLdcInsn(name);
        mv.visitLdcInsn(desc);
        mv.visitLdcInsn(value);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                NAME_ANNOTATION_VISITOR,
                "visitEnum",
                "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V",
                false);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String desc) {
        mv.visitInsn(Opcodes.DUP);
        mv.visitLdcInsn(name);
        mv.visitLdcInsn(desc);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                NAME_ANNOTATION_VISITOR,
                "visitAnnotation",
                "(Ljava/lang/String;Ljava/lang/String;)Ljdk/internal/org/objectweb/asm/AnnotationVisitor;",
                false);
        return this;
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        mv.visitInsn(Opcodes.DUP);
        mv.visitLdcInsn(name);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                NAME_ANNOTATION_VISITOR,
                "visitArray",
                "(Ljava/lang/String;)Ljdk/internal/org/objectweb/asm/AnnotationVisitor;",
                false);
        return this;
    }

    @Override
    public void visitEnd() {
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                NAME_ANNOTATION_VISITOR,
                "visitEnd",
                "()V",
                false);
    }
}
