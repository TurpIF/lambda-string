package fr.pturpin.lambdastring.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.invoke.MethodType;
import java.util.function.IntConsumer;

import static java.util.Objects.requireNonNull;

/**
 * {@link ClassVisitor} trying to find the first <code>LINENUMBER</code> used in a particular method.
 *
 * @see FetchingFirstLineNumberOfIndyMethodVisitor
 */
public final class FetchingFirstLineNumberOfIndyClassVisitor extends ClassVisitor {

    private final String methodName;
    private final String methodDesc;
    private final IntConsumer onFirstLine;

    /**
     * Create new {@link ClassVisitor} trying to find the first <code>LINENUMBER</code> used in a particular method
     * specified by the name name and the given descriptor.
     * <p>
     * The descriptor should match the format given by {@link MethodType#toMethodDescriptorString()}.
     * The described method may have any access {@link java.lang.reflect.Modifier}, including special ones as
     * {@link java.lang.reflect.Modifier#SYNTHETIC} or {@link java.lang.reflect.Modifier#BRIDGE}.
     * No error is thrown is no method matching the given name and descriptor is found.
     * <p>
     * It's guarantee that the consumer will only be triggered at maximum once.
     * <p>
     * Note that a class may be compiled without debug information, and so does not included any
     * <code>LINENUMBER</code> opcodes. In this case, the consumer will never be triggered.
     *
     * @param methodName  name of method to find
     * @param methodDesc  descriptor of method to find
     * @param onFirstLine consumer to call when finding the first <code>LINENUMBER</code>
     * @throws NullPointerException if any argument is <code>null</code>
     */
    public FetchingFirstLineNumberOfIndyClassVisitor(String methodName, String methodDesc, IntConsumer onFirstLine) {
        super(Opcodes.ASM6);
        this.methodName = requireNonNull(methodName);
        this.methodDesc = requireNonNull(methodDesc);
        this.onFirstLine = requireNonNull(onFirstLine);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (methodName.equals(name) && methodDesc.equals(desc)) {
            return new FetchingFirstLineNumberOfIndyMethodVisitor(onFirstLine);
        }
        return null;
    }
}
