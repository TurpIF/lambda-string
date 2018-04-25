# lambda-string [![build status](https://gitlab.com/TurpIF/lambda-to-string/badges/master/build.svg)](https://gitlab.com/TurpIF/lambda-to-string/commits/master) [![coverage report](https://gitlab.com/TurpIF/lambda-to-string/badges/master/coverage.svg)](https://turpif.gitlab.io/lambda-to-string/coverage)

Lambda-string (LS) is a helping java agent that inject configurable toString method into lambdas with some useful meta-information.

LS comes with a default toString strategy that print the origin of the lambdas. This feature let you easily track their origin while debugging as shown below :

![With and without the agent](https://gitlab.com/TurpIF/lambda-to-string/raw/master/doc/with-without.gif)

## Usage

The most recent release is [LS 0.1](https://gitlab.com/TurpIF/lambda-to-string/tags/v0.1).

To activate the LS agent in general, please use the following:
- Download the [lambdaString-0.1.jar](https://gitlab.com/TurpIF/lambda-to-string/-/jobs/artifacts/v0.1/raw/target/lambdaString-0.1.jar?job=package%3Ajdk8)
- Add `-javaagent:/path/to/lambdaString-0.1.jar` in your java options


To activate the LS agent using remote debugger, please use the following:
```bash
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


## Benchmark

This agent should be used during a debugging session with human interaction,
so a little performance overhead is still acceptable as long as it is not perceptible by a human.
Although, here is some benchmarks to give you some ideas of the potential impacts :

- [HotSpot JVM 8](https://turpif.gitlab.io/lambda-to-string/benchmark/jre8/)
- [HotSpot JVM 9](https://turpif.gitlab.io/lambda-to-string/benchmark/jre9/)
- [HotSpot JVM 10](https://turpif.gitlab.io/lambda-to-string/benchmark/jre10/)
- [IBM J9 VM (JRE 8)](https://turpif.gitlab.io/lambda-to-string/benchmark/jre8-ibm/)

To reduce the impacts, a particular attention is done when transforming the lambda runtime representations.
Also, the majority of the computation is done when `toString` is effectively called.
This avoids performance cost until a true human interaction.

The benchmarks are done with JMH on a Gitlab shared agent. So the absolute metrics may be wrong,
but the important part is the relative comparison with and without the agent.

### Creation of lambdas

The `LambdaCallSiteGenerationComparisonBenchmark` benchmark compares the time
the JRE spend generating a class instance (and its constant call site) from a lambda. This is done only once per lambda.
Currently, on the HostSpot JVM 8, the JRE takes roughly **50ns** to generate a lambda call site without the agent
and **100ns** with. So yes, there is an overhead, but your JRE can still generate **10 000 000** lambdas per seconds
(and if you have so many lambdas in your code base, I guess that one seconds is not that much compared to the others
kinds of issues you may have).

### Cost of strategy call

The `OriginalToStringInjectionComparisonBenchmark` benchmark compares the time the JRE spend returning the
`Object#toString()` of a lambda. Without agent, the original `toString` is simply called.
With the agent, a `LambdaToStringStrategy` is setup to return the same. Roughly, both versions take the same time.
So, there isn't any impact when injecting a strategy that reproduces the same output than a real `toString`.

### Cost of the debugging strategy

The `DefaultToStringStrategyComparisonBenchmark` benchmark compares the time spent by the JRE to
return the original `toString` compared to returning a useful debugging `toString` as shown above.
The debugging strategy exceed few milliseconds. Although, this stays imperceptible for a human


## Customizing injected toString

It is possible to give a custom `toString` strategy to the agent. For this, you should first create
a new implementation of the `LambdaToStringStrategy` interface.

Then the agent setup should give the custom strategy class as below :

```bash
java -javaagent:./lambdaString-0.1.jar=my.dummy.MyToStringStrategy my.dummy.Main
```

Here is a sample of custom strategy returning a constant toString :

```java
public final class MyToStringStrategy implements LambdaToStringStrategy {
    @Override
    public String createToString(Object lambda, LambdaMetaInfo metaInfo) {
        return "Hello world";
    }
}
```


## Contributing

When contributing to this repository, please first discuss the change you wish to make via issue, email, or any other
method with the owners of this repository before making a change.

### Prerequisites

Installing a development environment needs few requirements:
- [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- [Maven](https://maven.apache.org/)
- [GitLab Runner](https://docs.gitlab.com/runner/) (for debugging)
- [Docker](https://www.docker.com/) (for debugging)

### Installation

```bash
git clone https://gitlab.com/TurpIF/lambda-to-string lambda-to-string
cd lambda-to-string
mvn verify
```

Testing the agent on different JRE is eased with the GitLab Runner and Docker.
Also, few scripts are available to simulate the gitlab CI pipeline through a shared directory amongst the dockers.
Available testing JRE tasks are those matching the `test-jre*` pattern in the GitLab CI file.

```bash
git clone https://gitlab.com/TurpIF/lambda-to-string lambda-to-string
cd lambda-to-string

# GitLab Runner does not allow running pipeline with shared artifact, so the build-jre8 task should be called
# once before running any testing task.
# Task outputs are shared in the /tmp/output directory of the host. 

# Build the agent
./src/test/shell/build-jdk8.sh

# Test the agent with JRE 9
./src/test/shell/test-jre.sh test-jre9

# Test with a debugging server listening the port 9000
./src/test/shell/test-jre.sh test-jre10 9000
```

Calling those script may fail if your use is not allowed to use the docker socket.
You should either make this available to your user
(see [docker docs](https://docs.docker.com/install/linux/linux-postinstall/#manage-docker-as-a-non-root-user)),
or run the script with `sudo`.

Running the benchmarks locally is equivalent to execute the tests.
Available benchmarking JRE tasks are those matching the `benchmark-jre*` pattern in the GitLab CI file.

```bash
git clone https://gitlab.com/TurpIF/lambda-to-string lambda-to-string
cd lambda-to-string

# Build the agent and the benchmark JAR
./src/test/shell/build-jdk8.sh
./src/test/shell/test-jre.sh build-benchmark

# Benchmark the agent with JRE 10
./src/test/shell/test-jre.sh benchmark-jre10
```


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

