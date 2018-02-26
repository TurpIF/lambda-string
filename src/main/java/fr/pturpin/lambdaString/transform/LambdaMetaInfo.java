package fr.pturpin.lambdaString.transform;

import org.objectweb.asm.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.OptionalInt;
import java.util.function.IntConsumer;

import static java.util.Objects.requireNonNull;

public final class LambdaMetaInfo {

    private final Class<?> targetClass;
    private final int referenceKind;
    private final String declaringClassName;
    private final String methodName;
    private final String methodDesc;

    public LambdaMetaInfo(Class<?> targetClass, int referenceKind, String declaringClassName, String methodName, String methodDesc) {
        this.targetClass = requireNonNull(targetClass);
        this.referenceKind = referenceKind;
        this.declaringClassName = requireNonNull(declaringClassName);
        this.methodName = requireNonNull(methodName);
        this.methodDesc = requireNonNull(methodDesc);
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public int getReferenceKind() {
        return referenceKind;
    }

    public String getDeclaringClassName() {
        return declaringClassName;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getMethodDesc() {
        return methodDesc;
    }

    public OptionalInt getDeclarationLine() {
        int line = 0;
        try {
            line = computeDeclarationLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return line == -1 ? OptionalInt.empty() : OptionalInt.of(line);
    }

    private int computeDeclarationLine() throws IOException {
        int[] line = new int[]{ -1 };
        InputStream classStream = getClass().getClassLoader().getResourceAsStream(declaringClassName + ".class");
        ClassReader cr = new ClassReader(classStream);
        FetchingLineNumberOfIndyClassVisitor visitor = new FetchingLineNumberOfIndyClassVisitor(
                Opcodes.ASM5,
                methodName,
                methodDesc,
                foundLine -> line[0] = foundLine);
        cr.accept(visitor, 0);
        return line[0];
    }

    private static final class FetchingLineNumberOfIndyClassVisitor extends ClassVisitor {

        private final String methodName;
        private final String methodDesc;
        private final IntConsumer onLine;

        FetchingLineNumberOfIndyClassVisitor(int api, String methodName, String methodDesc, IntConsumer onLine) {
            super(api);
            this.methodName = requireNonNull(methodName);
            this.methodDesc = requireNonNull(methodDesc);
            this.onLine = requireNonNull(onLine);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (methodName.equals(name) && methodDesc.equals(desc)) {
                return new FetchingLineNumberOfIndyMethodVisitor(api, onLine);
            }
            return null;
        }
    }

    private static final class FetchingLineNumberOfIndyMethodVisitor extends MethodVisitor {

        private final IntConsumer onLine;
        private boolean lineFound;

        FetchingLineNumberOfIndyMethodVisitor(int api, IntConsumer onLine) {
            super(api);
            this.onLine = requireNonNull(onLine);
        }

        @Override
        public void visitLineNumber(int line, Label start) {
            if (!lineFound) {
                onLine.accept(line);
                lineFound = true;
            }
        }

    }

}
