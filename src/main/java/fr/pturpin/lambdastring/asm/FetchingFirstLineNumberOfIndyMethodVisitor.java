package fr.pturpin.lambdastring.asm;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.function.IntConsumer;

import static java.util.Objects.requireNonNull;

/**
 * {@link MethodVisitor} trying to find the first <code>LINENUMBER</code>.
 *
 * @see FetchingFirstLineNumberOfIndyClassVisitor
 */
public final class FetchingFirstLineNumberOfIndyMethodVisitor extends MethodVisitor {

    private final IntConsumer onFirstLine;
    private boolean lineFound;

    /**
     * Create new {@link MethodVisitor} trying to find the first <code>LINENUMBER</code>.
     * <p>
     * The visited method may have any access {@link java.lang.reflect.Modifier}, including special ones as
     * {@link java.lang.reflect.Modifier#SYNTHETIC} or {@link java.lang.reflect.Modifier#BRIDGE}.
     * <p>
     * It's guarantee that the consumer will only be triggered at maximum once. But if it isn't, no error is reported.
     * <p>
     * Note that a class may be compiled without debug information, and so does not included any
     * <code>LINENUMBER</code> opcodes. In this case, the consumer will never be triggered.
     *
     * @param onFirstLine consumer to call when finding the first <code>LINENUMBER</code>
     * @throws NullPointerException if the consumer is <code>null</code>
     */
    public FetchingFirstLineNumberOfIndyMethodVisitor(IntConsumer onFirstLine) {
        super(Opcodes.ASM6);
        this.onFirstLine = requireNonNull(onFirstLine);
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        if (!lineFound) {
            onFirstLine.accept(line);
            lineFound = true;
        }
    }

}
