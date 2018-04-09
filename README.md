# lambda-string [![build status](https://gitlab.com/TurpIF/lambda-to-string/badges/master/build.svg)](https://gitlab.com/TurpIF/lambda-to-string/commits/master)

Lambda-string (LS) is a helping java agent that inject configurable toString method into lambdas with some useful meta-information.

LS comes with a default toString strategy that print the origin of the lambdas. This feature let you easily track their origin while debugging as shown below :

![With and without the agent](https://gitlab.com/TurpIF/lambda-to-string/raw/master/doc/with-without.gif)

## Usage

The most recent release is [LS 0.1](https://gitlab.com/TurpIF/lambda-to-string/tags/v0.1).

To activate the LS agent in general, please use the following:
- Download the [lambdaString-0.1.jar](https://gitlab.com/TurpIF/lambda-to-string/-/jobs/artifacts/v0.1/raw/target/lambdaString-0.1.jar?job=package%3Ajdk8)
- Add `-javaagent:/path/to/lambdaString-0.1.jar` in your java options


To activate the LS agent using remote debugger, please use the following:
```shell
# Download JAR
wget -O /tmp/lambdaString-0.1.jar "https://gitlab.com/TurpIF/lambda-to-string/-/jobs/artifacts/v0.1/raw/target/lambdaString-0.1.jar?job=package%3Ajdk8"

# Start debug server
cd /your/project
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005 -javaagent:/tmp/lambdaString-0.1.jar my.dummy.Main

# Attach a debugger client with JDB or your preferred IDE
```


To activate the LS agent using IntelliJ, please use the following:
- Download the [lambdaString-0.1.jar](https://gitlab.com/TurpIF/lambda-to-string/-/jobs/artifacts/v0.1/raw/target/lambdaString-0.1.jar?job=package%3Ajdk8)
- Add `-javaagent:/path/to/lambdaString-0.1.jar` in the "VM options" of your debugging configuration

![Intellij hint](https://gitlab.com/TurpIF/lambda-to-string/raw/master/doc/intellij-usage.png)


To activate the LS agent with custom toString strategy, please use the following:
- Download the [lambdaString-0.1.jar](https://gitlab.com/TurpIF/lambda-to-string/-/jobs/artifacts/v0.1/raw/target/lambdaString-0.1.jar?job=package%3Ajdk8)
- Add `-javaagent:/path/to/lambdaString-0.1.jar=my.custom.ToStringStrategy` in your java options


## Why

This question is legitimate because a lambda should be restrained in a small scope.
So, reading code using lambda should be simple and direct.
Although, projects may grow quickly as their technical debt.
This agent may help you in that case : when you lose the control of your lambda, and you can't tell which one is it.

```java
package com.dummy;

public class Main {

  @FunctionalInterface
  interface Foo {
    void foo();
  }

  public static Foo createFoo(int param) {
    switch (param) {
      case 1:
        return () -> { /* do 1 */ };
      case 2:
        return () -> { /* do 2 */ };
    /* ... */
      default:
      return () -> { /* do default */ };
    }
  }

  public static void main(String[] args) {
    Integer param = Integer.valueOf(args[0]);
    Foo foo = createFoo(param);
    /* We have a foo, but which one ? The injected toString will tell you */
  }

}
```

In the above sample, the lambda is chosen from a runtime value.
While debugging, if you put a breakpoint after the generation of the lambda, you can't tell which lambda is returned by the method.
By activating the LS agent, the lambda origin is available through it's toString evaluation.

## Injecting custom toString

The injected toString is configurable by giving a custom strategy class to the agent:

```shell
java -javaagent:./lambdaString-0.1.jar=my.dummy.MyToStringStrategy my.dummy.Main
```

The strategy class should extends the `LambdaToStringStrategy` interface.

TODO Add sample strategy

## Known issues

#### Lambdas loaded during bootstrap are not supported (as `Function.identity()`):

This is because, by default, agents are loaded with the system class loader after the bootstrap one.
So, already visited lambda have already an abstract class representation. Also, this representation is not
retransformable and then not injectable.

#### Attaching the agent during a JVM runtime is not well supported:

It is possible to attach an agent to a running JVM.
But LS does not support injecting toString in lambdas that are already loaded by the JVM.

## Supported JVMs

Here is the list of the tested and working JREs:
- HotSpot JVM 1.8
- HotSpot JVM 9
- HotSpot JVM 10
- IBM J9 VM (JRE 8)

