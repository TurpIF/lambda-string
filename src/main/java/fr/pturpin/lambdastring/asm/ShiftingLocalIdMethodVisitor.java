package fr.pturpin.lambdastring.asm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static java.util.Objects.requireNonNull;

final class ShiftingLocalIdMethodVisitor extends MethodVisitor {

    private final int localShift;

    ShiftingLocalIdMethodVisitor(int api, MethodVisitor mv, int localShift) {
        super(api, requireNonNull(mv));
        this.localShift = localShift;
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        if (opcode == Opcodes.RET || var == 0) {
            super.visitVarInsn(opcode, var);
        } else {
            super.visitVarInsn(opcode, var + localShift);
        }
    }
}
