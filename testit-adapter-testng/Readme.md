# Test IT TMS adapter for TestNG
![Test IT](https://raw.githubusercontent.com/testit-tms/adapters-python/master/images/banner.png)

## Getting Started

### Installation

#### Maven users

Add this dependency to your project's POM:

```xml
<dependency>
    <groupId>ru.testit</groupId>
    <artifactId>testit-adapter-testng</artifactId>
    <version>LATEST_VERSION</version>
    <scope>compile</scope>
</dependency>
```

#### Gradle users

Add this dependency to your project's build file:

```groovy
implementation "ru.testit:testit-adapter-testng:LATEST_VERSION"
```

## Usage

#### Maven users

1. Add this dependency to your project's POM:
    ````xml
     <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
        <aspectj.version>1.9.7</aspectj.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.testng</groupId>
            <artifactId>testng</artifactId>
            <version>7.5</version>
        </dependency>
        <dependency>
            <groupId>ru.testit</groupId>
            <artifactId>testit-adapter-testng</artifactId>
            <version>LATEST_VERSION</version>
        </dependency>
           <dependency>
            <groupId>ru.testit</groupId>
            <artifactId>testit-java-commons</artifactId>
            <version>LATEST_VERSION</version>
        </dependency>
        <dependency>
            <groupId>org.aspectj</groupId>
            <artifactId>aspectjweaver</artifactId>
            <version>${aspectj.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.aspectj</groupId>
            <artifactId>aspectjrt</artifactId>
            <version>${aspectj.version}</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>aspectj-maven-plugin</artifactId>
                <version>1.14.0</version>
                <configuration>
                    <complianceLevel>${maven.compiler.source}</complianceLevel>
                    <source>${maven.compiler.source}</source>
                    <target>${maven.compiler.source}</target>
                    <!--Allows the adapter to accept real parameter names-->
                    <compilerArgs>
                        <arg>-parameters</arg>
                    </compilerArgs>
                </configuration>
                <executions>
                    <execution>
                        <phase>process-sources</phase>
                        <goals>
                            <goal>compile</goal>
                            <goal>test-compile</goal>
                        </goals>
                    </execution>
                </executions>
               <dependencies>
                    <dependency>
                        <groupId>org.aspectj</groupId>
                        <artifactId>aspectjtools</artifactId>
                        <version>${aspectj.version}</version>
                    </dependency>
                </dependencies>  
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.0.0-M7</version>
                <configuration>
                    <argLine>-XX:-UseSplitVerifier</argLine>
                    <argLine>-javaagent:${user.home}/.m2/repository/org/aspectj/aspectjweaver/${aspectj.version}/aspectjweaver-${aspectj.version}.jar</argLine>
                </configuration>
            </plugin>
        </plugins>
    </build>
    ````
2. Press "Reload All Maven Projects" button

#### Gradle users

1. Add this dependency to your project's build file:
```groovy
plugins {
   id 'java'
}

configurations {
    aspectConfig
}

sourceCompatibility = 1.8

group 'org.example'
version '1.0-SNAPSHOT'

compileJava.options.encoding = 'utf-8'
tasks.withType(JavaCompile) {
   options.encoding = 'utf-8'
   // Allows the adapter to accept real parameter names
   options.compilerArgs.add("-parameters")
}

repositories {
   mavenCentral()
   mavenLocal()
}

dependencies {
    testImplementation 'org.aspectj:aspectjrt:1.9.7'
    testImplementation "ru.testit:testit-adapter-testng:$LATEST_VERSION"
    testImplementation "ru.testit:testit-java-commons:$LATEST_VERSION"
    testImplementation 'org.testng:testng:7.5'
    aspectConfig "org.aspectj:aspectjweaver:1.9.7"
}

test {
    useTestNG()
    doFirst {
        def weaver = configurations.aspectConfig.find { it.name.contains("aspectjweaver") }
        jvmArgs += "-javaagent:$weaver"
    }
}
```
2. Press "Reload All Gradle Projects" button

### Configuration

#### File

Create **testit.properties** file in the resource directory of the project:
``` 
url={%URL%}
privateToken={%USER_PRIVATE_TOKEN%} 
projectId={%PROJECT_ID%} 
configurationId={%CONFIGURATION_ID%}
TestRunId={%TEST_RUN_ID%}
TestRunName={%TEST_RUN_NAME%}
AdapterMode={%ADAPTER_MODE%}
```
And fill parameters with your configuration, where:  
`URL` - location of the TMS instance  
`USER_PRIVATE_TOKEN` - API secret key  

1. go to the https://{DOMAIN}/user-profile profile  
2. copy the API secret key

`PROJECT_ID` - id of project in TMS instance

1. create a project
2. open DevTools -> network
3. go to the project https://{DOMAIN}/projects/20/tests
4. GET-request project, Preview tab, copy id field  

`CONFIGURATION_ID` - id of configuration in TMS instance  

1. create a project  
2. open DevTools -> network  
3. go to the project https://{DOMAIN}/projects/20/tests  
4. GET-request configurations, Preview tab, copy id field  

`TEST_RUN_ID` - id of the created test-run in TMS instance  

> TEST_RUN_ID is optional. If it's not provided than it creates automatically.

`TEST_RUN_NAME` - name of new test-run

> TEST_RUN_NAME is optional. If it's not provided than it generates automatically.

`ADAPTER_MODE` - mode of adapter. Default value - 0 
> Adapter supports following modes:
> 0 - in this mode adapter filters tests by test run id and configuration id and sends results to test run
> 1 - in this mode adapter sends all results to test run without filtering
> 2 - in this mode adapter creates new test run and sends results to new test run

#### ENV

You can use environment variables (environment variables take precedence over file variables):

`TMS_URL` - location of the TMS instance

`TMS_PRIVATE_TOKEN` - API secret key

`TMS_PROJECT_ID` - id of project in TMS instance

`TMS_CONFIGURATION_ID` - id of configuration in TMS instance

`TMS_TEST_RUN_ID` - id of the created test-run in TMS instance

`TMS_TEST_RUN_NAME` - name of new test-run

`TMS_ADAPTER_MODE` - mode of adapter. Default value - 0

`TMS_CONFIG_FILE` - name of configuration file

> TMS_CONFIG_FILE is optional. If it's not provided than it uses default file name.

#### Command line

You also can CLI variables (CLI variables take precedence over environment variables):

##### Gradle
```
gradle test -DtmsUrl=http://localhost:8080
```

##### Maven
```
maven test -DtmsUrl=http://localhost:8080
```

`tmsUrl` - location of the TMS instance

`tmsPrivateToken` - API secret key

`tmsProjectId` - id of project in TMS instance

`tmsConfigurationId` - id of configuration in TMS instance

`tmsTestRunId` - id of the created test-run in TMS instance

`tmsTestRunName` - name of new test-run

`tmsAdapterMode` - mode of adapter. Default value - 0

`tmsConfigFile` - name of configuration file

> tmsConfigFile is optional. If it's not provided than it uses default file name.

### Annotations

Annotations can be used to specify information about autotest.

Description of Annotations:
- `WorkItemIds` - linking an autotest to a test case
- `DisplayName` - name of the autotest in the Test IT system
- `ExternalID` - ID of the autotest within the project in the Test IT System
- `Title` - title in the autotest card and the step card
- `Description` - description in the autotest card and the step card
- `Labels` - tags in the autotest card
- `Links` - links in the autotest card
- `Step` - the designation of the step

Description of methods:
- `Adapter.addLinks` - links in the autotest result
- `Adapter.addAttachments` - attachments in the autotest result
- `Adapter.addMessage` - message in the autotest result

### Examples

#### Simple test
```java
import ru.testit.annotations.*;
import ru.testit.models.LinkItem;
import org.testng.Assert;
import org.testng.annotations.*;

public class SampleTests {

    @Test
    @ExternalId("Simple_test_1")
    @DisplayName("Simple test 1")
    public void simpleTest1() {
        Assert.assertTrue(true);
    }

    @Step
    @Title("Step 1")
    @Description("Step 1 description")
    private void step1() {
        step2();
        Assert.assertTrue(true);
        Adapter.addMessage("Message");
    }

    @Step
    @Title("Step 2")
    @Description("Step 2 description")
    private void step2() {
        Assert.assertTrue(true);
        Adapter.addAttachment("/Users/user/screen.json");
    }

    @Test
    @ExternalId("Simple_test_2")
    @DisplayName("Simple test 2")
    @WorkItemIds({"12345","54321"})
    @Title("Simple test 2")
    @Description("Simple test 2 description")
    @Links(links = {@Link(url = "www.1.ru", title = "firstLink", description = "firstLinkDesc", type = LinkType.RELATED),
                @Link(url = "www.3.ru", title = "thirdLink", description = "thirdLinkDesc", type = LinkType.ISSUE),
                @Link(url = "www.2.ru", title = "secondLink", description = "secondLinkDesc", type = LinkType.BLOCKED_BY)})
    public void simpleTest2() {
        step1();
        Adapter.addLinks("https://testit.ru/", "Test 1", "Desc 1", LinkType.ISSUE);
        Assert.assertTrue(true);
    }
}
```

#### Parameterized test

```java
package ru.testit.samples;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import ru.testit.annotations.*;
import ru.testit.models.LinkType;

import java.util.stream.Stream;

public class ParameterizedTests {

   @ParameterizedTest
   @ValueSource(shorts = {1, 2, 3})
   @ExternalId("Parameterized_test_with_one_parameter_{number}")
   @DisplayName("Test with number = {number} parameter")
   @WorkItemIds("{number}")
   @Title("Title in the autotest card {number}")
   @Description("Test with BeforeEach, AfterEach and all annotations {number}")
   @Labels({"Tag{number}"})
   void testWithOneParameter(int number) {

   }

   @ParameterizedTest
   @MethodSource("arguments")
   @ExternalId("Parameterized_test_with_multiple_parameters_{number}")
   @DisplayName("Parameterized test with number = {number}, title = {title}, expected = {expected}, url = {url}")
   @Links(links = {
           @Link(url = "https://{url}/module/repository", title = "{title} Repository", description = "Example of repository", type = LinkType.REPOSITORY),
           @Link(url = "https://{url}/module/projects", title = "{title} Projects", type = LinkType.REQUIREMENT),
           @Link(url = "https://{url}/module/", type = LinkType.BLOCKED_BY),
           @Link(url = "https://{url}/module/docs", title = "{title} Documentation", type = LinkType.RELATED),
           @Link(url = "https://{url}/module/JCP-777", title = "{title} JCP-777", type = LinkType.DEFECT),
           @Link(url = "https://{url}/module/issue/5", title = "{title} Issue-5", type = LinkType.ISSUE),
   })
   void testWithMultipleParameters(int number, String title, boolean expected, String url) {
   }

   static Stream<Arguments> arguments() {
      return Stream.of(
              Arguments.of(1, "Test version 1", true, "google.com"),
              Arguments.of(2, "Test version 2", false, "yandex.ru")
      );
   }
}
```

# Contributing

You can help to develop the project. Any contributions are **greatly appreciated**.

* If you have suggestions for adding or removing projects, feel free to [open an issue](https://github.com/testit-tms/adapters-java/issues/new) to discuss it, or directly create a pull request after you edit the *README.md* file with necessary changes.
* Please make sure you check your spelling and grammar.
* Create individual PR for each suggestion.
* Please also read through the [Code Of Conduct](https://github.com/testit-tms/adapters-java/blob/main/CODE_OF_CONDUCT.md) before posting your first idea as well.

# License

Distributed under the Apache-2.0 License. See [LICENSE](https://github.com/testit-tms/adapters-java/blob/main/LICENSE.md) for more information.

