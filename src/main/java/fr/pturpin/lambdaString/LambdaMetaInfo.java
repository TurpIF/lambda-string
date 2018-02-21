package fr.pturpin.lambdaString;

import static java.util.Objects.requireNonNull;

public final class LambdaMetaInfo {

    private final Class<?> targetClass;

    public LambdaMetaInfo(Class<?> targetClass) {
        this.targetClass = requireNonNull(targetClass);
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

}
