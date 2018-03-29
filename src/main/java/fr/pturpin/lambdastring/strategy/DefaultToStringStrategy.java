package fr.pturpin.lambdastring.strategy;

import fr.pturpin.lambdastring.transform.LambdaMetaInfo;

import java.util.OptionalInt;

public class DefaultToStringStrategy implements LambdaToStringStrategy {

    @Override
    public String createToString(Object lambda, LambdaMetaInfo metaInfo) throws LambdaToStringException {
        String className = getClassName(metaInfo);
        OptionalInt declarationLine = metaInfo.getDeclarationLine();

        if (!isSynthetic(metaInfo.getModifers())) {
            // Seems like a method reference
            String methodDesc = metaInfo.getMethodDesc();
            String emptyArgsDesc = "()V";
            boolean hideArgs = declarationLine.isPresent() || emptyArgsDesc.equals(methodDesc);
            String toString = className + "::" + metaInfo.getMethodName() + (hideArgs ? "" : methodDesc);

            if (declarationLine.isPresent()) {
                toString = toString + ":" + declarationLine.getAsInt();
            }
            return toString;
        }

        if (declarationLine.isPresent()) {
            return className + ":" + declarationLine.getAsInt();
        }
        return className + "::" + metaInfo.getMethodName() + metaInfo.getMethodDesc();
    }

    private static String getClassName(LambdaMetaInfo metaInfo) {
        String classFullName = metaInfo.getDeclaringClass().getName();
        return classFullName.substring(classFullName.lastIndexOf('.') + 1);
    }

    private static boolean isSynthetic(int modifiers) {
        int ACC_SYNTHETIC = 0x1000; // from JVMS 4.6 (Table 4.6-A)
        return (modifiers & ACC_SYNTHETIC) != 0;
    }
}
