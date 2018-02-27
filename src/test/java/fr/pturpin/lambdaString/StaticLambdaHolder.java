package fr.pturpin.lambdaString;

import fr.pturpin.lambdaString.LambdaTestHolder.Lambda;

import java.util.function.Supplier;

/**
 * Contains a lot of different kind of lambdas. Line number of those declarations should stay constant as some tests
 * use them.
 */
@SuppressWarnings("unused")
public class StaticLambdaHolder {

    public static final Lambda STATIC_FINAL_LAMBDA = () -> {};

    public static final Lambda STATIC_FINAL_MULTILINE_LAMBDA = () -> {
        int i = 0;
        int j = 0;
    };

    public static final Lambda STATIC_FINAL_LAMBDA_IN_STATIC_BLOCK;

    public static Lambda STATIC_LAMBDA = () -> {};

    @SuppressWarnings("UnusedAssignment") public static Lambda STATIC_REASSIGNED_LAMBDA = () -> {};

    public static Lambda STATIC_LAMBDA_FROM_ANONYMOUS;

    public static Lambda STATIC_LAMBDA_FROM_LAMBDA;

    public static Lambda LAMBDA_FROM_STATIC_INNER_CLASS = StaticInnerLambdaHolder.INNER_STATIC_FINAL;

    private static final StaticInnerLambdaHolder STATIC_INNER_LAMBDA_HOLDER =  new StaticInnerLambdaHolder(() -> {
        int i = 0;
    });

    private static final InnerLambdaHolder INNER_LAMBDA_HOLDER =  new StaticLambdaHolder().innerLambdaHolder;

    public static Lambda LAMBDA_DECLARED_IN_CONSTRUCTOR_OF_STATIC_INNER_CLASS = STATIC_INNER_LAMBDA_HOLDER.lambdaDeclaredInConstructor;

    public static Lambda LAMBDA_GIVEN_TO_CONSTRUCTOR_OF_STATIC_INNER_CLASS = STATIC_INNER_LAMBDA_HOLDER.lambdaGivenToConstructor;

    public static Lambda LAMBDA_DECLARED_IN_CONSTRUCTOR_OF_INNER_CLASS = INNER_LAMBDA_HOLDER.lambdaDeclaredInConstructor;

    public static Lambda LAMBDA_GIVEN_TO_CONSTRUCTOR_OF_INNER_CLASS = INNER_LAMBDA_HOLDER.lambdaGivenToConstructor;

    static {
        STATIC_FINAL_LAMBDA_IN_STATIC_BLOCK = () -> {
            int i = 0;
        };

        STATIC_REASSIGNED_LAMBDA = () -> {};

        //noinspection Convert2Lambda
        STATIC_LAMBDA_FROM_ANONYMOUS = new Supplier<Lambda>() {
            @Override
            public Lambda get() {
                return () -> {
                    int i = 0;
                    int j = 0;
                };
            }
        }.get();

        //noinspection TrivialFunctionalExpressionUsage
        STATIC_LAMBDA_FROM_LAMBDA = ((Supplier<Lambda>) () -> {
            //noinspection CodeBlock2Expr
            return () -> {
                int i = 0;
            };
        }).get();
    }

    private final InnerLambdaHolder innerLambdaHolder = new InnerLambdaHolder(() -> {
        // Nothing
        // Still nothing
        int i = 0;
    });

    private static final class StaticInnerLambdaHolder {

        static final Lambda INNER_STATIC_FINAL = () -> {};

        final Lambda lambdaDeclaredInConstructor;
        final Lambda lambdaGivenToConstructor;

        private StaticInnerLambdaHolder(Lambda lambdaGivenToConstructor) {
            this.lambdaGivenToConstructor = lambdaGivenToConstructor;
            this.lambdaDeclaredInConstructor = () -> {
                int i = 0;
            };
        }
    }

    private final class InnerLambdaHolder {

        final Lambda lambdaDeclaredInConstructor;
        final Lambda lambdaGivenToConstructor;

        private InnerLambdaHolder(Lambda lambdaGivenToConstructor) {
            this.lambdaGivenToConstructor = lambdaGivenToConstructor;
            this.lambdaDeclaredInConstructor = () -> {
                int i = 0;
            };

        }
    }

}
