package fr.pturpin.lambdaString.transform;

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

}
