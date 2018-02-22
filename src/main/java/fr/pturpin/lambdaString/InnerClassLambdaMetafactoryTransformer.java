package fr.pturpin.lambdaString;

import org.objectweb.asm.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.invoke.MethodType;
import java.security.ProtectionDomain;

import static java.util.Objects.requireNonNull;

public final class InnerClassLambdaMetafactoryTransformer implements ClassFileTransformer {

    private final String toStringStrategyClassName;

    InnerClassLambdaMetafactoryTransformer(String toStringStrategyClassName) {
        this.toStringStrategyClassName = requireNonNull(toStringStrategyClassName);
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (className.equals(InjectingToStringMethodVisitor.INNER_CLASS_LAMBDA_METAFACTORY_NAME)) {
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
                    return new InjectingToStringMethodVisitor(mv, toStringStrategyClassName);
                }
            }, 0);
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
        private static final String LAMBDA_META_INFO_NAME = Type.getInternalName(LambdaMetaInfo.class);

        private final String toStringStrategyClassName;

        InjectingToStringMethodVisitor(MethodVisitor mv, String toStringStrategyClassName) {
            super(Opcodes.ASM5, mv);
            this.toStringStrategyClassName = requireNonNull(toStringStrategyClassName);
            LambdaToStringLinker.linkStrategy(toStringStrategyClassName); // Check validity
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

                visitToString();
            }
        }

        private void visitToString() {
            // MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "toString", "()Ljava/lang/String;", null, null);
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

            MetaMethodVisitor mmv = new MetaMethodVisitor(api, mv);

            mmv.visitCode();

            // NoClassDefFoundError is thrown if lambda does not have any visibility on this agent classes.
            // This happens when lambda is loaded by the bootstrap class loader but not this agent classes.
            mmv.visitTryCatchBlock(() -> {
                visitExternalToString(mmv);
            }, () -> {
                mmv.visitInsn(Opcodes.ARETURN);
            }, () -> {
                visitDefaultToString(mmv);
                mmv.visitInsn(Opcodes.ARETURN);
            }, Type.getInternalName(NoClassDefFoundError.class));

            mmv.visitMaxs(-1, -1); // Maxs computed by ClassWriter.COMPUTE_FRAMES, these arguments ignored
            mmv.visitEnd();
        }

        /**
         * Push, in the stack of the lambda, the call to the external <code>toString</code> method used as new
         * lambda <code>toString</code>.
         * <p>
         * The call is represented by this snippet:<br />
         * <code>LambdaToStringLinker.lambdaToString(
         *     toStringStrategyClassName,
         *     this,
         *     new LambdaMetaInfo(targetClass));</code>
         *
         * @param mmv meta method visitor of the generated lambda
         */
        private void visitExternalToString(MetaMethodVisitor mmv) {
            mmv.visitLdcInsn(toStringStrategyClassName);
            mmv.visitVarInsn(Opcodes.ALOAD, 0);

            mmv.visitTypeInsn(Opcodes.NEW, LAMBDA_META_INFO_NAME);
            mmv.visitInsn(Opcodes.DUP);

            mmv.visitLdcInsn(() -> {
                // targetClass
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(Opcodes.GETFIELD,
                        INNER_CLASS_LAMBDA_METAFACTORY_NAME,
                        "targetClass",
                        "Ljava/lang/Class;");
                mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                        "jdk/internal/org/objectweb/asm/Type",
                        "getType",
                        "(Ljava/lang/Class;)Ljdk/internal/org/objectweb/asm/Type;",
                        false);
            });

            mmv.visitIntInsn(Opcodes.SIPUSH, () -> {
                // implInfo.getReferenceKind()
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(Opcodes.GETFIELD,
                        INNER_CLASS_LAMBDA_METAFACTORY_NAME,
                        "implInfo",
                        "Ljava/lang/invoke/MethodHandleInfo;");
                mv.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                        "java/lang/invoke/MethodHandleInfo",
                        "getReferenceKind",
                        "()I",
                        true);
            });

            mmv.visitLdcInsn(() -> {
                // implInfo.getDeclaringClass().getName().replace('.', '/')
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(Opcodes.GETFIELD,
                        INNER_CLASS_LAMBDA_METAFACTORY_NAME,
                        "implInfo",
                        "Ljava/lang/invoke/MethodHandleInfo;");
                mv.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                        "java/lang/invoke/MethodHandleInfo",
                        "getDeclaringClass",
                        "()Ljava/lang/Class;",
                        true);
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                        "java/lang/Class",
                        "getName",
                        "()Ljava/lang/String;",
                        false);
                mv.visitIntInsn(Opcodes.BIPUSH, '.');
                mv.visitInsn(Opcodes.I2C);
                mv.visitIntInsn(Opcodes.BIPUSH, '/');
                mv.visitInsn(Opcodes.I2C);
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                        "java/lang/String",
                        "replace",
                        "(CC)Ljava/lang/String;",
                        false);
            });

            mmv.visitLdcInsn(() -> {
                // implInfo.getName()
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(Opcodes.GETFIELD,
                        INNER_CLASS_LAMBDA_METAFACTORY_NAME,
                        "implInfo",
                        "Ljava/lang/invoke/MethodHandleInfo;");
                mv.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                        "java/lang/invoke/MethodHandleInfo",
                        "getName",
                        "()Ljava/lang/String;",
                        true);
            });

            mmv.visitLdcInsn(() -> {
                // implInfo.getMethodType().toMethodDescriptorString();
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(Opcodes.GETFIELD,
                        INNER_CLASS_LAMBDA_METAFACTORY_NAME,
                        "implInfo",
                        "Ljava/lang/invoke/MethodHandleInfo;");
                mv.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                        "java/lang/invoke/MethodHandleInfo",
                        "getMethodType",
                        "()Ljava/lang/invoke/MethodType;",
                        true);
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                        "java/lang/invoke/MethodType",
                        "toMethodDescriptorString",
                        "()Ljava/lang/String;",
                        false);
            });

            mmv.visitMethodInsn(Opcodes.INVOKESPECIAL,
                    LAMBDA_META_INFO_NAME,
                    "<init>",
                    MethodType.methodType(void.class, Class.class, int.class, String.class, String.class, String.class)
                            .toMethodDescriptorString(),
                    false);

            String transformerName = Type.getInternalName(LambdaToStringLinker.class);
            String lambdaToStringName = "lambdaToString";

            String lambdaToStringDesc = MethodType.methodType(String.class,
                    String.class,
                    Object.class,
                    LambdaMetaInfo.class)
                    .toMethodDescriptorString();
            mmv.visitMethodInsn(Opcodes.INVOKESTATIC,
                    transformerName,
                    lambdaToStringName,
                    lambdaToStringDesc,
                    false);
        }

        /**
         * Push the original {@link Object#toString()} implementation in the stack of the lambda
         * <p>
         * The original implementation is represented by this snippet :<br />
         * <code>getClass().getName() + "@" + Integer.toHexString(hashCode())</code>
         *
         * @param mmv meta method visitor of the generated lambda
         */
        private void visitDefaultToString(MetaMethodVisitor mmv) {
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
        }

    }

}
