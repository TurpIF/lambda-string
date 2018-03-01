# lambda-to-string [![build status](https://gitlab.com/TurpIF/lambda-to-string/badges/master/build.svg)](https://gitlab.com/TurpIF/lambda-to-string/commits/master)

Small library to inject custom toString method on all lambda for debugging purpose as below:

```java
public class MyClass {
    public void foo() {
        Predicate predicate = this::myCondition;
        Predicate lambda = () -> true;

        // the 12 and 4 is the line number of the myCondition method
        assert "MyClass::myCondition:12".equals(predicate.toString());
        assert "MyClass:4".equals(predicate.toString());
    }

    public boolean myCondition() {
        return true;
    }
}
```

## Usage

The injection is made by activating this agent : [lambdaString-0.1.jar](https://gitlab.com/TurpIF/lambda-to-string/uploads/6a07a595f55004cb70bb6aa876fb0d6c/lambdaString-0.1.jar)
```shell
# Using the default toString strategy
java -javaagent:path/to/lambdaString-0.1.jar

# Or with custom strategy
java -javaagent:path/to/lambdaString-0.1.jar=my.custom.ToStringStrategy
```

The default strategy will show useful debugging info as above. But you may also write your own strategy by extending
the `LambdaToStringStrategy` interface.


## Issues

#### Lambdas loaded during bootstrap are not supported (as `Function.identity()`):

This is because, by default, agents are loaded with the system class loader after the bootstrap one.
So, already visited lambda have already an abstract class representation. Also, this representation is not
retransformable and then not injectable.

## Supported JVM

This agent is currently only tested on the HotSpot JVM version 1.8 and 9.
