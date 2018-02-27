package fr.pturpin.lambdaString.asm;

import fr.pturpin.lambdaString.strategy.LambdaToStringStrategy;
import fr.pturpin.lambdaString.transform.LambdaMetaInfo;
import fr.pturpin.lambdaString.transform.LambdaToStringLinker;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.management.ManagementFactory;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class InjectingToStringMethodVisitor extends MethodVisitor {

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

    private final MethodVisitor mv;
    private final String toStringStrategyClassName;

    InjectingToStringMethodVisitor(MethodVisitor mv, String toStringStrategyClassName) {
        super(Opcodes.ASM5, mv);
        this.mv = new ShiftingLocalIdMethodVisitor(Opcodes.ASM5, mv, 9);
        this.toStringStrategyClassName = requireNonNull(toStringStrategyClassName);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitMaxs(10, 13);
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

        mmv.visitTryCatchBlock(() -> visitExternalToString(mmv),
                () -> mmv.visitInsn(Opcodes.ARETURN),
                getCatchBlocks(mmv));

        mmv.visitMaxs(-1, -1); // Maxs computed by ClassWriter.COMPUTE_FRAMES, these arguments ignored
        mmv.visitEnd();
    }

    /**
     * Returns the appropriate catch blocks to handle potential visibility errors, such as
     * {@link NoClassDefFoundError}, that may happen while getting the injected <code>toString</code>.
     * <p>
     * Those are specific to the JRE version:
     * <ul>
     * <li>JRE8: {@link NoClassDefFoundError} in a {@link BootstrapMethodError} is thrown if lambda does not have any
     * visibility on this agent classes</li>
     * <li>JRE9: {@link NoClassDefFoundError} is thrown if lambda does not have any visibility on this agent classes.
     * This is currently only observed when agent is setup with <code>-javagent</code></li>
     * </ul>
     * <p>
     * This happens when lambda is loaded by the bootstrap class loader but not this agent classes.
     * See <code>classLoadedFromBootstrapClassLoaderAreNotSupported</code> test in <code>LambdaToStringAgentIT</code>
     * and <code>LambdaStringTest</code> to reproduce this.
     *
     * @param mmv the meta method visitor to write code in lambda
     * @return map of catch blocks by their supported {@link Throwable} class name
     */
    private Map<String, Runnable> getCatchBlocks(MetaMethodVisitor mmv) {
        return detectJreVersion().map(version -> {
            if (version == SupportedJreVersion.JRE_8) {
                return getJdk8CatchBlocks(mmv);
            } else if (version == SupportedJreVersion.JRE_9) {
                return getJdk9CatchBlocks(mmv);
            }
            return null;
        }).orElseGet(HashMap::new);
    }

    private Map<String, Runnable> getJdk8CatchBlocks(MetaMethodVisitor mmv) {
        Map<String, Runnable> multiCatchBlocks = new HashMap<>();
        multiCatchBlocks.put(Type.getInternalName(BootstrapMethodError.class), () -> {
            // if (!(e.getCause() instanceof NoClassDefFoundError)) throw e;
            Runnable newIf = () -> {
                mmv.newLabel(); // ifInstanceOf
                mv.visitVarInsn(Opcodes.ASTORE, 2);
            };
            Runnable pushIf = () -> mv.visitVarInsn(Opcodes.ALOAD, 2);
            Runnable newElse = () -> {
                mmv.newLabel(); // ifInstanceOf
                mv.visitVarInsn(Opcodes.ASTORE, 3);
            };
            Runnable pushElse = () -> mv.visitVarInsn(Opcodes.ALOAD, 3);

            mmv.visitVarInsn(Opcodes.ASTORE, 1);
            newIf.run();
            newElse.run();

            mmv.visitVarInsn(Opcodes.ALOAD, 1);
            mmv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "java/lang/Throwable",
                    "getCause",
                    "()Ljava/lang/Throwable;",
                    false);
            mmv.visitTypeInsn(Opcodes.INSTANCEOF, "java/lang/NoClassDefFoundError");
            mmv.visitJumpInsn(Opcodes.IFNE, pushElse);

            // If instanceof
            mmv.visitLabel(pushIf);
            mmv.visitVarInsn(Opcodes.ALOAD, 1);
            mmv.visitInsn(Opcodes.ATHROW);

            // Else
            mmv.visitLabel(pushElse);
            mmv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            visitDefaultToString(mmv);
            mmv.visitInsn(Opcodes.ARETURN);
        });
        return multiCatchBlocks;
    }

    private Map<String, Runnable> getJdk9CatchBlocks(MetaMethodVisitor mmv) {
        Map<String, Runnable> multiCatchBlocks = new HashMap<>();
        multiCatchBlocks.put(Type.getInternalName(NoClassDefFoundError.class), () -> {
            visitDefaultToString(mmv);
            mmv.visitInsn(Opcodes.ARETURN);
        });
        return multiCatchBlocks;
    }

    /**
     * Push, in the stack of the lambda, the call to the external <code>toString</code> method used as new
     * lambda <code>toString</code>.
     * <p>
     * The call is represented by this snippet:<br>
     * <code>LambdaToStringLinker.lambdaToString(
     *     toStringStrategyClassName,
     *     this,
     *     new LambdaMetaInfo(targetClass));</code>
     *
     * @param mmv meta method visitor of the generated lambda
     */
    private void visitExternalToString(MetaMethodVisitor mmv) {
        // Use a invokedynamic with constant call site to cache the initialization setup and keep permanent
        // strategy instance by lambda
        mmv.visitInvokeDynamicInsn("createToString",
                Type.getMethodDescriptor(Type.getType(LambdaToStringStrategy.class)),
                () -> {
                    // Handle to LambdaToStringLinker#link
                    // The Handle may not be given through a simpleLDC to the InnerClassLambdaMetafactory because it
                    // doesn't know the LambdaToStringLinker class
                    mv.visitTypeInsn(Opcodes.NEW, "jdk/internal/org/objectweb/asm/Handle");
                    mv.visitInsn(Opcodes.DUP);
                    mv.visitIntInsn(Opcodes.BIPUSH, Opcodes.H_INVOKESTATIC);
                    mv.visitLdcInsn(Type.getInternalName(LambdaToStringLinker.class));
                    mv.visitLdcInsn("link");
                    mv.visitLdcInsn(MethodType.methodType(CallSite.class,
                            MethodHandles.Lookup.class,
                            String.class,
                            MethodType.class,
                            String.class).toMethodDescriptorString());
                    mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
                            "jdk/internal/org/objectweb/asm/Handle",
                            "<init>",
                            MethodType.methodType(void.class, int.class, String.class, String.class, String.class)
                                    .toMethodDescriptorString(),
                            false);
                },
                toStringStrategyClassName);

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
            // implInfo.getDeclaringClass()
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
            mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                    "jdk/internal/org/objectweb/asm/Type",
                    "getType",
                    "(Ljava/lang/Class;)Ljdk/internal/org/objectweb/asm/Type;",
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
                MethodType.methodType(void.class, Class.class, int.class, Class.class, String.class, String.class)
                        .toMethodDescriptorString(),
                false);

        mmv.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                Type.getInternalName(LambdaToStringStrategy.class),
                "createToString",
                MethodType.methodType(String.class, Object.class, LambdaMetaInfo.class).toMethodDescriptorString(),
                true);
    }

    /**
     * Push the original {@link Object#toString()} implementation in the stack of the lambda
     * <p>
     * The original implementation is represented by this snippet :<br>
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

    private enum SupportedJreVersion { JRE_8, JRE_9 }

    private static Optional<SupportedJreVersion> detectJreVersion() {
        // For Java 9 and further, this should not change : https://bugs.openjdk.java.net/browse/JDK-8149519
        PrivilegedAction<String> action = ManagementFactory.getRuntimeMXBean()::getSpecVersion;
        String specVersion = AccessController.doPrivileged(action);
        if ("1.8".equals(specVersion)) {
            return Optional.of(SupportedJreVersion.JRE_8);
        } else if ("9".equals(specVersion)) {
            return Optional.of(SupportedJreVersion.JRE_9);
        }
        return Optional.empty();
    }

}
