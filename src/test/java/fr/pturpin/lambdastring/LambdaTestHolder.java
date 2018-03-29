package fr.pturpin.lambdastring;

public class LambdaTestHolder {

    /**
     * Returns the original {@link Object#toString()} of the given object.
     */
    public static String defaultToString(Object object) {
        return object.getClass().getName() + "@" + Integer.toHexString(object.hashCode());
    }


    /**
     * Dummy method whose reference match the {@link Lambda} interface
     */
    public static void body() {
        // nothing
    }

    /**
     * Dummy functional interface to work on in tests
     */
    @FunctionalInterface
    public interface Lambda {
        @SuppressWarnings("unused")
        void body();
    }

}
