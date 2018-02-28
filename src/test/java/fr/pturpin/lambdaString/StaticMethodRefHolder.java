package fr.pturpin.lambdaString;

import fr.pturpin.lambdaString.LambdaTestHolder.Lambda;

/**
 * Contains a lot of different kind of method references.
 * Line number of those declarations should stay constant as some tests use them.
 */
@SuppressWarnings("unused")
public class StaticMethodRefHolder {

    private static void staticMethod() { }

    private static void staticLinedMethod() {
    }

    private static void staticMultilineMethod() {


        int i = 0;
    }

    private void instanceMethod() {

    }

    private static final class StaticInnerHolder {

        private static final Lambda STATIC_METHOD = StaticInnerHolder::staticMethod;
        private static final Lambda OUTSIDE_STATIC_METHOD = StaticMethodRefHolder::staticMethod;

        private static void staticMethod() {
        }

        private void instanceMethod() { }

    }

    private final class InnerHolder {

        private void instanceMethod() { }

    }

    private interface InterfaceInnerHolder {
        default void defaultMethod() {

        }

        static void staticMethod() {
        }
    }

    private static final class OverriddenDefaultMethodHolder implements InterfaceInnerHolder {
        @Override
        public void defaultMethod() {}

        static final Lambda SUPER_STATIC_METHOD = InterfaceInnerHolder::staticMethod;
    }

    private final InnerHolder innerHolder = new InnerHolder();

    public static Lambda STATIC_METHOD_FROM_LAMBDA_ANONYMOUS;

    public static Lambda STATIC_METHOD_FROM_LAMBDA;

    public static Lambda STATIC_METHOD_FROM_METHOD;

    public static Lambda STATIC_METHOD_FROM_OTHER_KIND_OF_ANONYMOUS;

    public static Lambda STATIC_METHOD_FROM_OTHER_KIND_OF_LAMBDA;

    @SuppressWarnings("UnusedAssignment") public static Lambda STATIC_REASSIGNED_METHOD_REF = StaticMethodRefHolder::staticMethod;

    static {
        STATIC_REASSIGNED_METHOD_REF = StaticMethodRefHolder::staticMultilineMethod;

        //noinspection Convert2Lambda,FunctionalExpressionCanBeFolded
        STATIC_METHOD_FROM_LAMBDA_ANONYMOUS = new Lambda() {
            @Override
            public void body() {
            }
        }
                ::body;

        //noinspection FunctionalExpressionCanBeFolded
        STATIC_METHOD_FROM_LAMBDA = StaticLambdaHolder.STATIC_LAMBDA
                ::body;

        //noinspection FunctionalExpressionCanBeFolded
        STATIC_METHOD_FROM_METHOD = ((Lambda) StaticMethodRefHolder::staticMethod)
                ::body;

        //noinspection MethodRefCanBeReplacedWithLambda
        STATIC_METHOD_FROM_OTHER_KIND_OF_LAMBDA = ((Runnable) () -> {
            int i = 0;
        })::run;

        STATIC_METHOD_FROM_OTHER_KIND_OF_ANONYMOUS = (new Runnable() {
            @Override
            public void run() {
                int i = 0;
            }
        })::run;
    }

    public static final Lambda STATIC_METHOD_REF = StaticMethodRefHolder::staticMethod;

    public static final Lambda STATIC_LINED_METHOD = StaticMethodRefHolder
            ::staticLinedMethod;

    public static Lambda STATIC_MULTILINE_METHOD = StaticMethodRefHolder::staticMultilineMethod;

    public static Lambda INSTANCE_METHOD = new StaticMethodRefHolder()::instanceMethod;

    public static Lambda STATIC_METHOD_IN_STATIC_INNER = StaticMethodRefHolder.StaticInnerHolder
            ::staticMethod;

    public static Lambda INSTANCE_METHOD_IN_STATIC_INNER = new StaticMethodRefHolder.StaticInnerHolder()
            ::instanceMethod;

    public static Lambda INSTANCE_METHOD_IN_INSTANCE_INNER = new StaticMethodRefHolder().innerHolder

            ::instanceMethod;

    public static Lambda DEFAULT_METHOD_IN_INTERFACE = new StaticMethodRefHolder.InterfaceInnerHolder(){
    }
    ::defaultMethod;

    public static Lambda STATIC_METHOD_IN_INTERFACE = StaticMethodRefHolder.InterfaceInnerHolder
            ::staticMethod;

    public static Lambda OVERRIDDEN_DEFAULT_METHOD = new StaticMethodRefHolder.OverriddenDefaultMethodHolder()
            ::defaultMethod;

    public static Lambda STATIC_METHOD_IN_STATIC_INNER_REF_FROM_STATIC_INNER = StaticInnerHolder.STATIC_METHOD;

    public static Lambda STATIC_METHOD_FROM_STATIC_INNER = StaticInnerHolder.OUTSIDE_STATIC_METHOD;

    public static Lambda STATIC_METHOD_IN_INTERFACE_FROM_IMPL = OverriddenDefaultMethodHolder.SUPER_STATIC_METHOD;

}
