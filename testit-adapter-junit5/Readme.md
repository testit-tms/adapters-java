# Test IT TMS adapter for JUnit 5

![Test IT](https://raw.githubusercontent.com/testit-tms/adapters-python/master/images/banner.png)

## Getting Started

### Installation

#### Maven users

Add this dependency to your project POM:

```xml

<dependency>
    <groupId>ru.testit</groupId>
    <artifactId>testit-adapter-junit5</artifactId>
    <version>1.3.5</version>
    <scope>compile</scope>
</dependency>
```

#### Gradle users

Add this dependency to your project build file:

```groovy
implementation "ru.testit:testit-adapter-junit5:1.3.5"
```

## Usage

#### Maven users

1. Add this dependency to your project POM:
    ````xml
     <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
        <aspectj.version>1.9.7</aspectj.version>
        <adapter.version>1.3.5</adapter.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-api</artifactId>
            <version>5.7.0</version>
        </dependency>
        <dependency>
            <groupId>org.junit.platform</groupId>
            <artifactId>junit-platform-launcher</artifactId>
            <version>1.9.0</version>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-engine</artifactId>
            <version>5.7.0</version>
        </dependency>
        <dependency>
            <groupId>ru.testit</groupId>
            <artifactId>testit-java-commons</artifactId>
            <version>${adapter.version}</version>
        </dependency>
        <dependency>
            <groupId>ru.testit</groupId>
            <artifactId>testit-adapter-junit5</artifactId>
            <version>${adapter.version}</version>
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
                    <properties>
                        <configurationParameters>
                            junit.jupiter.extensions.autodetection.enabled = true
                        </configurationParameters>
                    </properties>
                </configuration>
            </plugin>
        </plugins>
    </build>
    ````
2. Press the **Reload All Maven Projects** button.

#### Gradle users

1. Add this dependency to your project build file:

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
    testImplementation "ru.testit:testit-adapter-junit5:1.3.5"
    testImplementation "ru.testit:testit-java-commons:1.3.5"
    testImplementation "org.junit.jupiter:junit-jupiter-api:5.6.0"
    testImplementation "org.junit.jupiter:junit-jupiter-engine:5.6.0"
    testImplementation "org.junit.jupiter:junit-jupiter-params:5.6.0"
    testImplementation "org.junit.platform:junit-platform-launcher:1.9.0"
    aspectConfig "org.aspectj:aspectjweaver:1.9.7"
}

test {
    useJUnitPlatform()
    systemProperty 'junit.jupiter.extensions.autodetection.enabled', true
    doFirst {
        def weaver = configurations.aspectConfig.find { it.name.contains("aspectjweaver") }
        jvmArgs += "-javaagent:$weaver"
    }
    // to enable command line options, specify the option that will be passed like this:
    // systemProperty '<parameter_name>', System.getProperty('<parameter_name>')
    // for example:
    // systemProperty 'tmsTestRunName', System.getProperty('tmsTestRunName')
}
```

2. Press the **Reload All Gradle Projects** button.

### Configuration

| Description                                                                                                                                                                                                                                                                                                                                                                            | Property                   | Environment variable              | CLI argument                  |
|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------|-----------------------------------|-------------------------------|
| Location of the TMS instance                                                                                                                                                                                                                                                                                                                                                           | url                        | TMS_URL                           | tmsUrl                        |
| API secret key [How to getting API secret key?](https://github.com/testit-tms/.github/tree/main/configuration#privatetoken)                                                                                                                                                                                                                                                            | privateToken               | TMS_PRIVATE_TOKEN                 | tmsPrivateToken               |
| ID of project in TMS instance [How to getting project ID?](https://github.com/testit-tms/.github/tree/main/configuration#projectid)                                                                                                                                                                                                                                                    | projectId                  | TMS_PROJECT_ID                    | tmsProjectId                  |
| ID of configuration in TMS instance [How to getting configuration ID?](https://github.com/testit-tms/.github/tree/main/configuration#configurationid)                                                                                                                                                                                                                                  | configurationId            | TMS_CONFIGURATION_ID              | tmsConfigurationId            |
| ID of the created test run in TMS instance.<br/>It's necessary for **adapterMode** 0 or 1                                                                                                                                                                                                                                                                                              | testRunId                  | TMS_TEST_RUN_ID                   | tmsTestRunId                  |
| Parameter for specifying the name of test run in TMS instance (**It's optional**). If it is not provided, it is created automatically                                                                                                                                                                                                                                                  | testRunName                | TMS_TEST_RUN_NAME                 | tmsTestRunName                |
| Adapter mode. Default value - 0. The adapter supports following modes:<br/>0 - in this mode, the adapter filters tests by test run ID and configuration ID, and sends the results to the test run<br/>1 - in this mode, the adapter sends all results to the test run without filtering<br/>2 - in this mode, the adapter creates a new test run and sends results to the new test run | adapterMode                | TMS_ADAPTER_MODE                  | tmsAdapterMode                |
| It enables/disables certificate validation (**It's optional**). Default value - true                                                                                                                                                                                                                                                                                                   | certValidation             | TMS_CERT_VALIDATION               | tmsCertValidation             |
| It enables/disables TMS integration (**It's optional**). Default value - true                                                                                                                                                                                                                                                                                                          | testIt                     | TMS_TEST_IT                       | tmsTestIt                     |
| Mode of automatic creation test cases (**It's optional**). Default value - false. The adapter supports following modes:<br/>true - in this mode, the adapter will create a test case linked to the created autotest (not to the updated autotest)<br/>false - in this mode, the adapter will not create a test case                                                                    | automaticCreationTestCases | TMS_AUTOMATIC_CREATION_TEST_CASES | tmsAutomaticCreationTestCases |
| Name of the configuration file If it is not provided, it is used default file name (**It's optional**)                                                                                                                                                                                                                                                                                 | -                          | TMS_CONFIG_FILE                   | tmsConfigFile                 |

#### File

Create **testit.properties** file in the resource directory of the project:
``` 
url=URL
privateToken=USER_PRIVATE_TOKEN
projectId=PROJECT_ID
configurationId=CONFIGURATION_ID
testRunId=TEST_RUN_ID
testRunName=TEST_RUN_NAME
adapterMode=ADAPTER_MODE
automaticCreationTestCases=AUTOMATIC_CREATION_TEST_CASES
certValidation=CERT_VALIDATION
testIt=TEST_IT
```

#### Examples

##### Gradle
```
gradle test -DtmsUrl=http://localhost:8080 -DtmsPrivateToken=Token -DtmsProjectId=f5da5bab-380a-4382-b36f-600083fdd795 -DtmsConfigurationId=3a14fa45-b54e-4859-9998-cc502d4cc8c6
-DtmsAdapterMode=0 -DtmsTestRunId=a17269da-bc65-4671-90dd-d3e3da92af80 -DtmsTestRunName=Regress -DtmsAutomaticCreationTestCases=true -DtmsCertValidation=true -DtestIt=true
```

##### Maven
```
maven test -DtmsUrl=http://localhost:8080 -DtmsPrivateToken=Token -DtmsProjectId=f5da5bab-380a-4382-b36f-600083fdd795 -DtmsConfigurationId=3a14fa45-b54e-4859-9998-cc502d4cc8c6
-DtmsAdapterMode=0 -DtmsTestRunId=a17269da-bc65-4671-90dd-d3e3da92af80 -DtmsTestRunName=Regress -DtmsAutomaticCreationTestCases=true -DtmsCertValidation=true -DtmsTestIt=true
```

If you want to enable debug mode then
see [How to enable debug logging?](https://github.com/testit-tms/adapters-java/tree/main/testit-java-commons)

If you want to add attachment for a failed test then
see [How to add an attachment for a failed test?](https://github.com/testit-tms/adapters-java/tree/main/testit-java-commons)

### Annotations

Use annotations to specify information about autotest.

Description of annotations:

- `WorkItemIds` - a method that links autotests with manual tests. Receives the array of manual tests' IDs
- `DisplayName` - internal autotest name (used in Test IT)
- `ExternalId` - unique internal autotest ID (used in Test IT)
- `Title` - autotest name specified in the autotest card. If not specified, the name from the displayName method is used
- `Description` - autotest description specified in the autotest card
- `Labels` - tags listed in the autotest card
- `Links` - links listed in the autotest card
- `Step` - the designation of the step
- `Classname` - name of the classname
- `Namespace` - name of the package

Description of methods:

- `Adapter.addLinks` - add links to the autotest result.
- `Adapter.addAttachments` - add attachments to the autotest result.
- `Adapter.addMessage` - add message to the autotest result.

### Examples

#### Simple test

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import ru.testit.annotations.*;
import ru.testit.models.LinkItem;
import ru.testit.tms.client.TMSClient;

public class SimpleTest {

    @Test
    @ExternalId("Simple_test_1")
    @DisplayName("Simple test 1")
    public void simpleTest1() {
        Assertions.assertTrue(true);
    }

    @Test
    @ExternalId("Simple_test_2")
    @WorkItemIds({"12345", "54321"})
    @DisplayName("Simple test 2")
    @Title("test №2")
    @Description("Description")
    @Links(links = {@Link(url = "www.1.ru", title = "firstLink", description = "firstLinkDesc", type = LinkType.RELATED),
            @Link(url = "www.3.ru", title = "thirdLink", description = "thirdLinkDesc", type = LinkType.ISSUE),
            @Link(url = "www.2.ru", title = "secondLink", description = "secondLinkDesc", type = LinkType.BLOCKED_BY)})
    public void itsTrueReallyTrue() {
        stepWithParams("password", 456);
        Adapter.addLinks("https://testit.ru/", "Test 1", "Desc 1", LinkType.ISSUE);
        Assertions.assertTrue(true);
    }

    @Step
    @Title("Step 1 with params: {param1}, {param2}")
    @Description("Step 1 description and params: {param1}, {param2}")
    private void stepWithParams(String param1, int param2) {
        stepWithoutParams();
        Assertions.assertTrue(true);
        Adapter.addMessage("Message");
    }

    @Step
    @Title("Step 2")
    @Description("Step 2 description")
    private void stepWithoutParams() {
        Assertions.assertTrue(true);
        Adapter.addAttachment("/Users/user/screen.json");
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

* If you have suggestions for adding or removing projects, feel free
  to [open an issue](https://github.com/testit-tms/adapters-java/issues/new) to discuss it, or create a direct pull
  request after you edit the *README.md* file with necessary changes.
* Make sure to check your spelling and grammar.
* Create individual PR for each suggestion.
* Read the [Code Of Conduct](https://github.com/testit-tms/adapters-java/blob/main/CODE_OF_CONDUCT.md) before posting
  your first idea as well.

# License

Distributed under the Apache-2.0 License.
See [LICENSE](https://github.com/testit-tms/adapters-java/blob/main/LICENSE.md) for more information.
