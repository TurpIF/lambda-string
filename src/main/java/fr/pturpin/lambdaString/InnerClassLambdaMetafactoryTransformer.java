package fr.pturpin.lambdaString;

import org.objectweb.asm.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public final class InnerClassLambdaMetafactoryTransformer implements ClassFileTransformer {

  @Override
  public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
      ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
    if (className.equals("java/lang/invoke/InnerClassLambdaMetafactory")) {
      ClassReader cr = new ClassReader(classfileBuffer);
      ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
      cr.accept(new ClassVisitor(Opcodes.ASM5, cw) {
        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
          return new InjectingToStringMethodVisitor(super.visitMethod(access, name, desc, signature, exceptions));
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

    InjectingToStringMethodVisitor(MethodVisitor mv) {
      super(Opcodes.ASM5, mv);
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

        metaMv.visitLdcInsn("toto");
        metaMv.visitInsn(Opcodes.ARETURN);

        metaMv.visitMaxs(-1, -1); // Maxs computed by ClassWriter.COMPUTE_FRAMES, these arguments ignored
        metaMv.visitEnd();
      }
    }
  }
}
