# Test IT TMS Adapter for TestNG

![Test IT](https://raw.githubusercontent.com/testit-tms/adapters-python/master/images/banner.png)

## Getting Started

### Installation

#### Maven Users

Add this dependency to your project POM:

```xml

<dependency>
    <groupId>ru.testit</groupId>
    <artifactId>testit-adapter-testng</artifactId>
    <version>2.3.4</version>
    <scope>compile</scope>
</dependency>
```

#### Gradle Users

Add this dependency to your project build file:

```groovy
implementation "ru.testit:testit-adapter-testng:2.3.4"
```

## Usage

#### Maven Users

1. Add this dependency to your project POM:
    ````xml
     <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <aspectj.version>1.9.22</aspectj.version>
        <adapter.version>2.3.4</adapter.version>
        <aspectj-maven-plugin.version>1.14</aspectj-maven-plugin.version>
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
            <version>${adapter.version}</version>
        </dependency>
           <dependency>
            <groupId>ru.testit</groupId>
            <artifactId>testit-java-commons</artifactId>
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
                <groupId>dev.aspectj</groupId>
                <artifactId>aspectj-maven-plugin</artifactId>
                <version>${aspectj-maven-plugin.version}</version>
                <configuration>
                    <complianceLevel>${maven.compiler.source}</complianceLevel>
                    <source>${maven.compiler.source}</source>
                    <target>${maven.compiler.target}</target>
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
                <version>3.2.5</version>
                <configuration>
                    <argLine>-noverify</argLine>
                    <argLine>-javaagent:${user.home}/.m2/repository/org/aspectj/aspectjweaver/${aspectj.version}/aspectjweaver-${aspectj.version}.jar</argLine>
                </configuration>
            </plugin>
        </plugins>
    </build>
    ````
2. Press the **Reload All Maven Projects** button.

#### Gradle Users

1. Add this dependency to your project build file:

```groovy
plugins {
    id "java"
}

configurations {
    aspectConfig
}

sourceCompatibility = 1.8

group "org.example"
version "1.0-SNAPSHOT"

compileJava.options.encoding = "utf-8"
tasks.withType(JavaCompile) {
    options.encoding = "utf-8"
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    testImplementation "org.aspectj:aspectjrt:1.9.22"
    testImplementation "ru.testit:testit-adapter-testng:2.3.4"
    testImplementation "ru.testit:testit-java-commons:2.3.4"
    testImplementation "org.testng:testng:7.5"
    aspectConfig "org.aspectj:aspectjweaver:1.9.22"
}

test {
    useTestNG()
    doFirst {
        def weaver = configurations.aspectConfig.find { it.name.contains("aspectjweaver") }
        jvmArgs += "-javaagent:$weaver"
    }
    systemProperties(System.getProperties())
    environment(System.getenv())
}
```

2. Press the **Reload All Gradle Projects** button.

### Configuration

| Description                                                                                                                                                                                                                                                                                                                                                                            | File property                     | Environment variable                       | System property                      |
|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------|--------------------------------------------|--------------------------------------|
| Location of the TMS instance                                                                                                                                                                                                                                                                                                                                                           | url                               | TMS_URL                                    | tmsUrl                               |
| API secret key [How to getting API secret key?](https://github.com/testit-tms/.github/tree/main/configuration#privatetoken)                                                                                                                                                                                                                                                            | privateToken                      | TMS_PRIVATE_TOKEN                          | tmsPrivateToken                      |
| ID of project in TMS instance [How to getting project ID?](https://github.com/testit-tms/.github/tree/main/configuration#projectid)                                                                                                                                                                                                                                                    | projectId                         | TMS_PROJECT_ID                             | tmsProjectId                         |
| ID of configuration in TMS instance [How to getting configuration ID?](https://github.com/testit-tms/.github/tree/main/configuration#configurationid)                                                                                                                                                                                                                                  | configurationId                   | TMS_CONFIGURATION_ID                       | tmsConfigurationId                   |
| ID of the created test run in TMS instance.<br/>It's necessary for **adapterMode** 0 or 1                                                                                                                                                                                                                                                                                              | testRunId                         | TMS_TEST_RUN_ID                            | tmsTestRunId                         |
| Parameter for specifying the name of test run in TMS instance (**It's optional**). If it is not provided, it is created automatically                                                                                                                                                                                                                                                  | testRunName                       | TMS_TEST_RUN_NAME                          | tmsTestRunName                       |
| Adapter mode. Default value - 0. The adapter supports following modes:<br/>0 - in this mode, the adapter filters tests by test run ID and configuration ID, and sends the results to the test run<br/>1 - in this mode, the adapter sends all results to the test run without filtering<br/>2 - in this mode, the adapter creates a new test run and sends results to the new test run | adapterMode                       | TMS_ADAPTER_MODE                           | tmsAdapterMode                       |
| It enables/disables certificate validation (**It's optional**). Default value - true                                                                                                                                                                                                                                                                                                   | certValidation                    | TMS_CERT_VALIDATION                        | tmsCertValidation                    |
| It enables/disables TMS integration (**It's optional**). Default value - true                                                                                                                                                                                                                                                                                                          | testIt                            | TMS_TEST_IT                                | tmsTestIt                            |
| Mode of automatic creation test cases (**It's optional**). Default value - false. The adapter supports following modes:<br/>true - in this mode, the adapter will create a test case linked to the created autotest (not to the updated autotest)<br/>false - in this mode, the adapter will not create a test case                                                                    | automaticCreationTestCases        | TMS_AUTOMATIC_CREATION_TEST_CASES          | tmsAutomaticCreationTestCases        |
| Mode of automatic updation links to test cases (**It's optional**). Default value - false. The adapter supports following modes:<br/>true - in this mode, the adapter will update links to test cases<br/>false - in this mode, the adapter will not update link to test cases                                                                                                         | automaticUpdationLinksToTestCases | TMS_AUTOMATIC_UPDATION_LINKS_TO_TEST_CASES | tmsAutomaticUpdationLinksToTestCases |
| Mode of import type selection when launching autotests (**It's optional**). Default value - true. The adapter supports following modes:<br/>true - in this mode, the adapter will create/update each autotest in real time<br/>false - in this mode, the adapter will create/update multiple autotests                                                                                 | importRealtime                    | TMS_IMPORT_REALTIME                        | tmsImportRealtime                    |
| Name of the configuration file If it is not provided, it is used default file name (**It's optional**)                                                                                                                                                                                                                                                                                 | -                                 | TMS_CONFIG_FILE                            | tmsConfigFile                        |

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
automaticUpdationLinksToTestCases=AUTOMATIC_UPDATION_LINKS_TO_TEST_CASES
certValidation=CERT_VALIDATION
importRealtime=IMPORT_REALTIME
testIt=TEST_IT
```

#### Examples

##### Gradle
```
gradle test -DtmsUrl=http://localhost:8080 -DtmsPrivateToken=Token -DtmsProjectId=f5da5bab-380a-4382-b36f-600083fdd795 -DtmsConfigurationId=3a14fa45-b54e-4859-9998-cc502d4cc8c6
-DtmsAdapterMode=0 -DtmsTestRunId=a17269da-bc65-4671-90dd-d3e3da92af80 -DtmsTestRunName=Regress -DtmsAutomaticCreationTestCases=true -DtmsAutomaticUpdationLinksToTestCases=true 
-DtmsCertValidation=true -DtmsImportRealtime=true -DtmsTestIt=true
```

##### Maven
```
maven test -DtmsUrl=http://localhost:8080 -DtmsPrivateToken=Token -DtmsProjectId=f5da5bab-380a-4382-b36f-600083fdd795 -DtmsConfigurationId=3a14fa45-b54e-4859-9998-cc502d4cc8c6
-DtmsAdapterMode=0 -DtmsTestRunId=a17269da-bc65-4671-90dd-d3e3da92af80 -DtmsTestRunName=Regress -DtmsAutomaticCreationTestCases=true -DtmsAutomaticUpdationLinksToTestCases=true 
-DtmsCertValidation=true -DtmsImportRealtime=true -DtmsTestIt=true
```

If you want to enable debug mode then
see [How to enable debug logging?](https://github.com/testit-tms/adapters-java/tree/main/testit-java-commons)

If you want to add attachment for a failed test then
see [How to add an attachment for a failed test?](https://github.com/testit-tms/adapters-java/tree/main/testit-java-commons)

#### Run with filter
To create filter by autotests you can use the Test IT CLI (use adapterMode 1 for run with filter):

##### Gradle
```
$ export TMS_TOKEN=<YOUR_TOKEN>
$ testit autotests_filter 
  --url https://tms.testit.software \
  --configuration-id 5236eb3f-7c05-46f9-a609-dc0278896464 \
  --testrun-id 6d4ac4b7-dd67-4805-b879-18da0b89d4a8 \
  --framework gradle-testng \
  --output tmp/filter.txt

$ gradle test -DtmsTestRunId=6d4ac4b7-dd67-4805-b879-18da0b89d4a8 -DtmsAdapterMode=1 $(cat tmp/filter.txt)
```

##### Maven
```
$ export TMS_TOKEN=<YOUR_TOKEN>
$ testit autotests_filter 
  --url https://tms.testit.software \
  --configuration-id 5236eb3f-7c05-46f9-a609-dc0278896464 \
  --testrun-id 6d4ac4b7-dd67-4805-b879-18da0b89d4a8 \
  --framework maven-testng \
  --output tmp/filter.txt

$ maven test -DtmsTestRunId=6d4ac4b7-dd67-4805-b879-18da0b89d4a8 -DtmsAdapterMode=1 $(cat tmp/filter.txt)
```

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
- `Step` - the designation of the step.
- `Classname` - name of the classname.
- `Namespace` - name of the package.

Description of methods:

- `Adapter.addLinks` - add links to the autotest result.
- `Adapter.addAttachments` - add attachments to the autotest result.
- `Adapter.addMessage` - add message to the autotest result.

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
    @Title("Step 1 with params: {param1}, {param2}")
    @Description("Step 1 description and params: {param1}, {param2}")
    private void stepWithParams(String param1, int param2) {
        stepWithoutParams();
        Assert.assertTrue(true);
        Adapter.addMessage("Message");
    }

    @Step
    @Title("Step 2")
    @Description("Step 2 description")
    private void stepWithoutParams() {
        Assert.assertTrue(true);
        Adapter.addAttachment("/Users/user/screen.json");
    }

    @Test
    @ExternalId("Simple_test_2")
    @DisplayName("Simple test 2")
    @WorkItemIds({"12345", "54321"})
    @Title("Simple test 2")
    @Description("Simple test 2 description")
    @Links(links = {@Link(url = "www.1.ru", title = "firstLink", description = "firstLinkDesc", type = LinkType.RELATED),
            @Link(url = "www.3.ru", title = "thirdLink", description = "thirdLinkDesc", type = LinkType.ISSUE),
            @Link(url = "www.2.ru", title = "secondLink", description = "secondLinkDesc", type = LinkType.BLOCKED_BY)})
    public void simpleTest2() {
        stepWithParams("password", 456);
        Adapter.addLinks("https://testit.ru/", "Test 1", "Desc 1", LinkType.ISSUE);
        Assert.assertTrue(true);
    }
}
```

#### Parameterized test

```java
package ru.testit.samples;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ru.testit.annotations.*;
import ru.testit.models.LinkType;

public class DataProviderParameterizedTests {

    @DataProvider
    public static Object[][] allParameters() {
        return new Object[][]{
                {"Test version 1", 1, "google.com"},
                {"Test version 2", 2, "yandex.ru"}
        };
    }

    @Test(dataProvider = "allParameters")
    @ExternalId("Parameterized_test_with_data_provider_parameters_{number}")
    @DisplayName("Test with title = {title}, number = {number}, url = {url} parameters")
    @WorkItemIds("{number}")
    @Title("Title in the autotest card {number}")
    @Description("{title}")
    @Labels({"Tag{number}"})
    @Links(links = {
            @Link(url = "https://{url}/module/repository", title = "{title} Repository", description = "Example of repository", type = LinkType.REPOSITORY),
            @Link(url = "https://{url}/module/projects", title = "{title} Projects", type = LinkType.REQUIREMENT),
            @Link(url = "https://{url}/module/", type = LinkType.BLOCKED_BY),
            @Link(url = "https://{url}/module/docs", title = "{title} Documentation", type = LinkType.RELATED),
            @Link(url = "https://{url}/module/JCP-777", title = "{title} JCP-777", type = LinkType.DEFECT),
            @Link(url = "https://{url}/module/issue/5", title = "{title} Issue-5", type = LinkType.ISSUE),
    })
    void testWithDataProviderParameters(String title, int number, String url) {

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
