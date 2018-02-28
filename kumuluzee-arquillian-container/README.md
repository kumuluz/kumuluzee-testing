# KumuluzEE Arquillian Container Adapter
[![Build Status](https://img.shields.io/travis/kumuluz/kumuluzee-testing/master.svg?style=flat)](https://travis-ci.org/kumuluz/kumuluzee-testing)

> Arquillian container adapter for the KumuluzEE microservice framework

KumuluzEE Arquillian Container Adapter is an adapter for the [Arquillian](http://arquillian.org/) integration testing framework,
which enables the use of KumuluzEE container in integration tests.

KumuluzEE Arquillian Container Adapter starts the KumuluzEE server before running the tests, which enables the tests to interact with
the environment closely resembling the one in production.

The extension supports KumuluzEE version 2.6.0 or higher.

## Usage

You can use the KumuluzEE Arquillian Container Adapter by adding the following dependency:
```xml
<dependency>
    <groupId>com.kumuluz.ee.testing</groupId>
    <artifactId>kumuluzee-arquillian-container</artifactId>
    <version>${kumuluzee-arquillian-container.version}</version>
    <scope>test</scope>
</dependency>
```

To use the Arquillian framework, add the Arquillian BOM to the dependency management:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.jboss.arquillian</groupId>
            <artifactId>arquillian-bom</artifactId>
            <version>${arquillian.version}</version>
            <scope>import</scope>
            <type>pom</type>
        </dependency>
    </dependencies>
</dependencyManagement>
```

If tests are using the jUnit framework, add the following dependencies:
```xml
<dependency>
    <groupId>org.jboss.arquillian.junit</groupId>
    <artifactId>arquillian-junit-container</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <version>${junit.version}</version>
    <scope>test</scope>
</dependency>
```

This document will use the jUnit framework as an example.

### Writing tests 

Writing tests for KumuluzEE is no different than writing tests for any other container. KumuluzEE Arquillian Container
Adapter also supports CDI and Arquillian Resource injection out-of-the-box.

Example of a simple test:

```java
@Dependent
public class SimpleBean {

    public String sayHello() {
        return "bean-hello";
    }
}
```

```java
@RunWith(Arquillian.class)
public class InjectTest {

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClass(SimpleBean.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private SimpleBean bean;

    @Test
    public void injectTest() {
        Assert.assertNotNull(bean);
        Assert.assertEquals("bean-hello", bean.sayHello());
    }
}
```

The deployment can be either `JavaArchive` or `WebArchive`. In both cases, the helper methods provided by Arquillian
should be used when constructing the deployment (e.g. `JavaArchive#addAsManifestResource`, `WebArchive#addAsLibrary`,
etc.). The KumuluzEE Adapter will take care to convert and export the deployment in a format, recognised by the
KumuluzEE Server.

More examples like the one above can be found in the [test](TODO) folder of this project.

### Configuring Arquillian Container Adapter

KumuluzEE Arquillian Container Adapter uses common sense default values and should work with no additional configuration for most
use-cases. In order to configure the container, add the `arquillian.xml` file to the root of the classpath.

The following attributes are supported:

- `deleteTemporaryFiles` - If `true` cleans up created temporary files. Setting this option to `false` is useful when
  debugging, since the exported deployment is preserved. Default value: `true`.
- `containerStartTimeoutMs` - Time in milliseconds in which the container is expected to start. After this time has
  elapsed, the container is considered dead and the test fails with `DeploymentException`.
  Default value: `60000` (60 seconds).
- `kumuluzVersion` - The version of the KumuluzEE core framework, which is added to every deployment.
  Default value: The latest version of KumuluzEE.
- `includeRequiredLibraries` - The dependencies, that are automatically included in all deployments. The values can be
  any of the following:
  
  - `false` - No additional dependencies are included. Note that in order for KumuluzEE Server to start successfully,
  at least `kumuluzee-core` and servlet dependencies are required, in this case they must be provided by the user.
  - `default` - The `kumuluzee-core`, `kumuluzee-servlet-jetty` and `kumuluzee-cdi-weld` are included. This is the
  default value.
  - `MicroProfile-1.0` - The contents of MicroProfile 1.0 is included (JAX-RS 2.0, CDI 1.2, and JSON-P 1.0).
  - `MicroProfile-1.1` - The contents of MicroProfile 1.1 is included (Everything in MicroProfile 1.0 +
  MicroProfile Config 1.0).
  - `MicroProfile-1.2` - The contents of MicroProfile 1.2 is included (Everything in MicroProfile 1.0 +
  MicroProfile Config 1.1, Fault Tolerance 1.0, JWT Propagation 1.0, Health Metrics 1.0 and Health Check 1.0).

Example of the `arquillian.xml` file:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<arquillian xmlns="http://jboss.org/schema/arquillian"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://jboss.org/schema/arquillian http://www.jboss.org/schema/arquillian/arquillian_1_0.xsd">

    <container qualifier="KumuluzEE" default="true">
        <configuration>
            <property name="deleteTemporaryFiles">false</property>
            <property name="containerStartTimeoutMs">60000</property>
            <property name="kumuluzVersion">2.6.0</property>
            <property name="includeRequiredLibraries">MicroProfile-1.0</property>
        </configuration>
    </container>

</arquillian>
```

### Adding dependencies to deployments

The following dependencies are added to each deployment automatically:

- `kumuluzee-core`
- `kumuluzee-servlet-jetty`
- `kumuluzee-cdi-weld`

The version of the above mentioned dependencies is determined with the configuration property `kumuluzVersion`.

In order to correctly resolve additional Maven dependencies, use the `MavenDependencyAppender` interface.
For example, to include `kumuluzee-metrics` in each deployment use the following appender:

```java
public class DependencyAppender implements MavenDependencyAppender {

    @Override
    public List<String> addLibraries() {
        return Collections.singletonList("com.kumuluz.ee.metrics:kumuluzee-metrics-core:1.0.0");
    }
}
```

All appenders must be registered in a service file named
`com.kumuluz.ee.arquillian.container.spi.MavenDependencyAppender`.

The method `addLibraries()` must return a list of Strings, each one specifying one dependency in the
format: `<groupId>:<artifactId>:<version>`. When including a KumuluzEE component, the version part can be omitted and
the version specified with the `kumuluzVersion` property will be used (latest version by default). For example:

```java
public class RequiredDependencyAppender implements MavenDependencyAppender {

    @Override
    public List<String> addLibraries() {
        return Collections.singletonList("com.kumuluz.ee:kumuluzee-jax-rs-jersey:");
    }
}
```

## Changelog

Recent changes can be viewed on Github on the [Releases Page](https://github.com/kumuluz/kumuluzee-testing/releases)

## Contribute

See the [contributing docs](https://github.com/kumuluz/kumuluzee-testing/blob/master/CONTRIBUTING.md)

When submitting an issue, please follow the 
[guidelines](https://github.com/kumuluz/kumuluzee-testing/blob/master/CONTRIBUTING.md#bugs).

When submitting a bugfix, write a test that exposes the bug and fails before applying your fix. Submit the test alongside the fix.

When submitting a new feature, add tests that cover the feature.

## License

MIT