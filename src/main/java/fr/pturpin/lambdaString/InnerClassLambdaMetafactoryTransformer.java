package fr.pturpin.lambdaString;

import org.objectweb.asm.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import static java.util.Objects.requireNonNull;

public final class InnerClassLambdaMetafactoryTransformer implements ClassFileTransformer {

    private final LambdaToStringStrategy toStringStrategy;

    public InnerClassLambdaMetafactoryTransformer(LambdaToStringStrategy toStringStrategy) {
        this.toStringStrategy = requireNonNull(toStringStrategy);
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (className.equals("java/lang/invoke/InnerClassLambdaMetafactory")) {
            ClassReader cr = new ClassReader(classfileBuffer);
            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
            cr.accept(new ClassVisitor(Opcodes.ASM5, cw) {
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
                    return new InjectingToStringMethodVisitor(mv, toStringStrategy);
                }
            }, 0);
            try {
                new FileOutputStream("D:\\tmp\\tmp.class").write(cw.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return cw.toByteArray();
        }
        return null;
    }

    private static class InjectingToStringMethodVisitor extends MethodVisitor {

        private static final String INNER_CLASS_LAMBDA_METAFACTORY_NAME = "java/lang/invoke/InnerClassLambdaMetafactory";

        private static final String CLASS_WRITER_NAME = "jdk/internal/org/objectweb/asm/ClassWriter";
        private static final String CLASS_WRITER_DESC = "Ljdk/internal/org/objectweb/asm/ClassWriter;";
        private static final String METHOD_VISITOR_DESC = "Ljdk/internal/org/objectweb/asm/MethodVisitor;";
        private static final String CLASS_WRITER_VISIT_METHOD_DESC = Type.getMethodDescriptor(
                Type.getType(METHOD_VISITOR_DESC),
                Type.getType(int.class),
                Type.getType(String.class),
                Type.getType(String.class),
                Type.getType(String.class),
                Type.getType(String[].class));

        private static final String TO_STRING_DESC = Type.getMethodDescriptor(Type.getType(String.class));

        private final LambdaToStringStrategy toStringStrategy;

        InjectingToStringMethodVisitor(MethodVisitor mv, LambdaToStringStrategy toStringStrategy) {
            super(Opcodes.ASM5, mv);
            this.toStringStrategy = requireNonNull(toStringStrategy);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);

            if (CLASS_WRITER_NAME.equals(owner) && "visit".equals(name)) {
                // get cw
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(Opcodes.GETFIELD,
                        INNER_CLASS_LAMBDA_METAFACTORY_NAME,
                        "cw",
                        CLASS_WRITER_DESC);

                // MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "test", "()Ljava/lang/String;", null, null);
                mv.visitInsn(Opcodes.ICONST_1);
                mv.visitLdcInsn("toString");
                mv.visitLdcInsn(TO_STRING_DESC);
                mv.visitInsn(Opcodes.ACONST_NULL);
                mv.visitInsn(Opcodes.ACONST_NULL);
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                        CLASS_WRITER_NAME,
                        "visitMethod",
                        CLASS_WRITER_VISIT_METHOD_DESC,
                        false);

                MetaMethodVisitor metaMv = new MetaMethodVisitor(api, mv);

                metaMv.visitCode();

                metaMv.visitTryCatchBlock(mmv -> {
                    String transformerName = Type.getInternalName(LambdaToStringLinker.class);
                    String lambdaToStringName = "lambdaToString";
                    String lambdaToStringDesc = Type.getMethodDescriptor(Type.getType(String.class), Type.getType(String.class));
                    mmv.visitLdcInsn(toStringStrategy.getClass().getName());
                    mmv.visitMethodInsn(Opcodes.INVOKESTATIC,
                            transformerName,
                            lambdaToStringName,
                            lambdaToStringDesc,
                            false);
                }, mmv -> {
                    mmv.visitInsn(Opcodes.ARETURN);
                }, mmv -> {
                    // Original Object#toString:
                    // return getClass().getName() + "@" + Integer.toHexString(hashCode());

                    mmv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
                    mmv.visitInsn(Opcodes.DUP);
                    mmv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);

                    mmv.visitVarInsn(Opcodes.ALOAD, 0);
                    mmv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                            "java/lang/Object",
                            "getClass",
                            "()Ljava/lang/Class;",
                            false);
                    mmv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                            "java/lang/Class",
                            "getName",
                            "()Ljava/lang/String;",
                            false);
                    mmv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                            "java/lang/StringBuilder",
                            "append",
                            "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
                            false);

                    mmv.visitLdcInsn("@");
                    mmv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                            "java/lang/StringBuilder",
                            "append",
                            "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
                            false);

                    mmv.visitVarInsn(Opcodes.ALOAD, 0);
                    mmv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "hashCode", "()I", false);
                    mmv.visitMethodInsn(Opcodes.INVOKESTATIC,
                            "java/lang/Integer",
                            "toHexString",
                            "(I)Ljava/lang/String;",
                            false);
                    mmv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                            "java/lang/StringBuilder",
                            "append",
                            "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
                            false);

                    mmv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                            "java/lang/StringBuilder",
                            "toString",
                            "()Ljava/lang/String;",
                            false);
                    mmv.visitInsn(Opcodes.ARETURN);
                }, Type.getInternalName(Throwable.class));

                metaMv.visitMaxs(-1, -1); // Maxs computed by ClassWriter.COMPUTE_FRAMES, these arguments ignored
                metaMv.visitEnd();
            }
        }
    }

}
