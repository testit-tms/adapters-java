# Test IT TMS Adapter for JBehave
![Test IT](https://raw.githubusercontent.com/testit-tms/adapters-python/master/images/banner.png)

## Getting Started

### Installation

#### Maven Users

Add this dependency to your project POM:

```xml
<dependency>
    <groupId>ru.testit</groupId>
    <artifactId>testit-adapter-jbehave</artifactId>
    <version>2.3.4</version>
    <scope>compile</scope>
</dependency>
```

#### Gradle Users

Add this dependency to your project build file:

```groovy
implementation "ru.testit:testit-adapter-jbehave:2.3.4"
```

## Usage

#### Maven Users

##### TestNG

1. Add this dependency to your project POM:
    ````xml
     <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <adapter.version>2.3.4</adapter.version>
        <jbehave.version>4.8.3</jbehave.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.testng</groupId>
            <artifactId>testng</artifactId>
            <version>7.5</version>
        </dependency>
        <dependency>
            <groupId>ru.testit</groupId>
            <artifactId>testit-java-commons</artifactId>
            <version>${adapter.version}</version>
        </dependency>
        <dependency>
            <groupId>ru.testit</groupId>
            <artifactId>testit-adapter-jbehave</artifactId>
            <version>${adapter.version}</version>
        </dependency>
        <dependency>
            <groupId>org.jbehave</groupId>
            <artifactId>jbehave-core</artifactId>
            <version>${jbehave.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
    ````
2. Press the **Reload All Maven Projects** button.

##### Junit5

1. Add this dependency to your project POM:
    ````xml
      <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <adapter.version>2.3.4</adapter.version>
        <jbehave.version>4.8.3</jbehave.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-api</artifactId>
            <version>5.8.1</version>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-engine</artifactId>
            <version>5.8.1</version>
        </dependency>
        <dependency>
            <groupId>ru.testit</groupId>
            <artifactId>testit-java-commons</artifactId>
            <version>${adapter.version}</version>
        </dependency>
        <dependency>
            <groupId>ru.testit</groupId>
            <artifactId>testit-adapter-jbehave</artifactId>
            <version>${adapter.version}</version>
        </dependency>
        <dependency>
            <groupId>org.jbehave</groupId>
            <artifactId>jbehave-core</artifactId>
            <version>${jbehave.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
    ````
2. Press the **Reload All Maven Projects** button.

##### Junit4

1. Add this dependency to your project POM:
    ````xml
     <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <adapter.version>2.3.4</adapter.version>
        <jbehave.version>4.8.3</jbehave.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.12</version>
        </dependency>
        <dependency>
            <groupId>org.junit.platform</groupId>
            <artifactId>junit-platform-runner</artifactId>
            <version>1.6.3</version>
        </dependency>
        <dependency>
            <groupId>ru.testit</groupId>
            <artifactId>testit-java-commons</artifactId>
            <version>${adapter.version}</version>
        </dependency>
        <dependency>
            <groupId>ru.testit</groupId>
            <artifactId>testit-adapter-jbehave</artifactId>
            <version>${adapter.version}</version>
        </dependency>
        <dependency>
            <groupId>org.jbehave</groupId>
            <artifactId>jbehave-core</artifactId>
            <version>${jbehave.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
    ````
2. Press the **Reload All Maven Projects** button.

#### Gradle Users

##### TestNG

1. Add this dependency to your project build file:
```groovy
plugins {
    id "java"
}

group "org.example"
version "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

def jbehaveVersion = "4.8.3"
def aspectjVersion = "1.9.22"

dependencies {
    testImplementation "ru.testit:testit-adapter-jbehave:2.3.4"
    testImplementation "ru.testit:testit-java-commons:2.3.4"
    testImplementation "org.testng:testng:7.5"
    testImplementation("org.jbehave:jbehave-core:$jbehaveVersion")
    testImplementation("org.aspectj:aspectjrt:$aspectjVersion")
}

test {
    useTestNG()
    systemProperties(System.getProperties())
}
```
2. Press the **Reload All Gradle Projects** button.

##### Junit5

1. Add this dependency to your project build file:
```groovy
plugins {
    id "java"
}

group "org.example"
version "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

def jbehaveVersion = "4.8.3"
def aspectjVersion = "1.9.22"

dependencies {
    testImplementation "ru.testit:testit-adapter-jbehave:2.3.4"
    testImplementation "ru.testit:testit-java-commons:2.3.4"
    testImplementation("org.jbehave:jbehave-core:$jbehaveVersion")
    testImplementation("org.aspectj:aspectjrt:$aspectjVersion")
    testImplementation "org.junit.jupiter:junit-jupiter-api:5.8.2"
    testRuntimeOnly "org.junit.jupiter:junit-jupiter-engine:5.8.2"
}

test {
    useJUnitPlatform()
    systemProperties(System.getProperties())
}
```
2. Press the **Reload All Gradle Projects** button.

##### Junit4

1. Add this dependency to your project build file:
```groovy
plugins {
    id "java"
}

group "org.example"
version "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

def jbehaveVersion = "4.8.3"
def aspectjVersion = "1.9.22"

dependencies {
    testImplementation "ru.testit:testit-adapter-jbehave:2.3.4"
    testImplementation "ru.testit:testit-java-commons:2.3.4"
    testImplementation("org.jbehave:jbehave-core:$jbehaveVersion")
    testImplementation("org.aspectj:aspectjrt:$aspectjVersion")
    testImplementation "junit:junit:4.12"
}

test {
    useJUnit()
    systemProperties(System.getProperties())
}
```
2. Press the **Reload All Gradle Projects** button.

### Configuration

| Description                                                                                                                                                                                                                                                                                                                                                                            | File property                     | Environment variable                       | System property                      |
|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------|--------------------------------------------|--------------------------------------|
| Location of the TMS instance                                                                                                                                                                                                                                                                                                                                                           | url                               | TMS_URL                                    | tmsUrl                               |
| API secret key [How to getting API secret key?](https://github.com/testit-tms/.github/tree/main/configuration#privatetoken)                                                                                                                                                                                                                                                                                                                                   | privateToken                      | TMS_PRIVATE_TOKEN                          | tmsPrivateToken                      |
| ID of project in TMS instance [How to getting project ID?](https://github.com/testit-tms/.github/tree/main/configuration#projectid)                                                                                                                                                                                                                                                                                                                        | projectId                         | TMS_PROJECT_ID                             | tmsProjectId                         |
| ID of configuration in TMS instance [How to getting configuration ID?](https://github.com/testit-tms/.github/tree/main/configuration#configurationid)                                                                                                                                                                                                                                                                                                            | configurationId                   | TMS_CONFIGURATION_ID                       | tmsConfigurationId                   |
| ID of the created test run in TMS instance.<br/>It's necessary for **adapterMode** 0 or 1                                                                                                                                                                                                                                                                                              | testRunId                         | TMS_TEST_RUN_ID                            | tmsTestRunId                         |
| Parameter for specifying the name of test run in TMS instance (**It's optional**). If it is not provided, it is created automatically                                                                                                                                                                                                                                                  | testRunName                       | TMS_TEST_RUN_NAME                          | tmsTestRunName                       |
| Adapter mode. Default value - 0. The adapter supports following modes:<br/>0 - in this mode, the adapter filters tests by test run ID and configuration ID, and sends the results to the test run<br/>1 - in this mode, the adapter sends all results to the test run without filtering<br/>2 - in this mode, the adapter creates a new test run and sends results to the new test run | adapterMode                       | TMS_ADAPTER_MODE                           | tmsAdapterMode                       |
| It enables/disables certificate validation (**It's optional**). Default value - true                                                                                                                                                                                                                                                                                                   | certValidation                    | TMS_CERT_VALIDATION                        | tmsCertValidation                    |
| It enables/disables TMS integration (**It's optional**). Default value - true                                                                                                                                                                                                                                                                                                          | testIt                            | TMS_TEST_IT                                | tmsTestIt                            |
| Mode of automatic creation test cases (**It's optional**). Default value - false. The adapter supports following modes:<br/>true - in this mode, the adapter will create a test case linked to the created autotest (not to the updated autotest)<br/>false - in this mode, the adapter will not create a test case                                                                    | automaticCreationTestCases        | TMS_AUTOMATIC_CREATION_TEST_CASES          | tmsAutomaticCreationTestCases        |
| Mode of automatic updation links to test cases (**It's optional**). Default value - false. The adapter supports following modes:<br/>true - in this mode, the adapter will update links to test cases<br/>false - in this mode, the adapter will not update link to test cases                                                                                                         | automaticUpdationLinksToTestCases | TMS_AUTOMATIC_UPDATION_LINKS_TO_TEST_CASES | tmsAutomaticUpdationLinksToTestCases |
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
testIt=TEST_IT
```

#### Examples

##### Gradle
```
gradle test -DtmsUrl=http://localhost:8080 -DtmsPrivateToken=Token -DtmsProjectId=f5da5bab-380a-4382-b36f-600083fdd795 -DtmsConfigurationId=3a14fa45-b54e-4859-9998-cc502d4cc8c6
-DtmsAdapterMode=0 -DtmsTestRunId=a17269da-bc65-4671-90dd-d3e3da92af80 -DtmsTestRunName=Regress -DtmsAutomaticCreationTestCases=true -DtmsAutomaticUpdationLinksToTestCases=true 
-DtmsCertValidation=true -DtmsTestIt=true
```

##### Maven
```
maven test -DtmsUrl=http://localhost:8080 -DtmsPrivateToken=Token -DtmsProjectId=f5da5bab-380a-4382-b36f-600083fdd795 -DtmsConfigurationId=3a14fa45-b54e-4859-9998-cc502d4cc8c6
-DtmsAdapterMode=0 -DtmsTestRunId=a17269da-bc65-4671-90dd-d3e3da92af80 -DtmsTestRunName=Regress -DtmsAutomaticCreationTestCases=true -DtmsAutomaticUpdationLinksToTestCases=true 
-DtmsCertValidation=true -DtmsTestIt=true
```

If you want to enable debug mode then see [How to enable debug logging?](https://github.com/testit-tms/adapters-java/tree/main/testit-java-commons)

If you want to add attachment for a failed test then see [How to add an attachment for a failed test?](https://github.com/testit-tms/adapters-java/tree/main/testit-java-commons)

### Meta

Use tags to specify information about autotest.

Description of tags:
- `WorkItemIds` - a method that links autotests with manual tests. Receives the array of manual tests' IDs
- `DisplayName` - internal autotest name (used in Test IT)
- `ExternalId` - unique internal autotest ID (used in Test IT)
- `Title` - autotest name specified in the autotest card. If not specified, the name from the displayName method is used
- `Description` - autotest description specified in the autotest card
- `Labels` - tags listed in the autotest card
- `Links` - links listed in the autotest card

Description of methods:
- `Adapter.addLinks` - add links to the autotest result.
- `Adapter.addAttachments` - add attachments to the autotest result.
- `Adapter.addMessage` - add message to the autotest result.

### Examples

#### Simple test

```java
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

import ru.testit.models.LinkType;
import ru.testit.services.Adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SampleSteps {

    private int a;
    private int b;
    private int c;

    @Given("a is $number")
    public void a_is(int arg1) {
        this.a = arg1;
    }

    @Given("b is $number")
    public void b_is(int arg1) {
        this.b = arg1;
    }

    @When("I add a to b")
    public void i_add_a_to_b() {
        this.c = this.a + this.b;
    }

    @Then("result is $number")
    public void result_is(int arg1) {
        Adapter.addLinks("https://testit.ru/", "Test 1", "Desc 1", LinkType.ISSUE);
        assertEquals(this.c, arg1);
    }

}
```

```story
Scenario: Add a to b
Meta:
@ExternalId failed_with_all_annotations
@DisplayName Failed_test_with_all_annotations
@WorkItemIds 123
@Title Title_in_the_autotest_card
@Description Test_with_all_annotations
@Labels Tag1,Tag2
@Links {"url":"https://dumps.example.com/module/repository","title":"Repository","description":"Example_of_repository","type":"Repository"}

Given a is 5
And b is 10
When I add a to b
Then result is 14
```

#### Parameterized test

```story
Scenario: Scenario with Positive Examples

Given a is <a>
And b is <b>
When I add a to b
Then result is <result>

Examples:
| a | b | result |
| 1 | 3 | 4      |
| 2 | 4 | 6      |
```

# Contributing

You can help to develop the project. Any contributions are **greatly appreciated**.

* If you have suggestions for adding or removing projects, feel free to [open an issue](https://github.com/testit-tms/adapters-java/issues/new) to discuss it, or create a direct pull request after you edit the *README.md* file with necessary changes.
* Make sure to check your spelling and grammar.
* Create individual PR for each suggestion.
* Read the [Code Of Conduct](https://github.com/testit-tms/adapters-java/blob/main/CODE_OF_CONDUCT.md) before posting your first idea as well.

# License

Distributed under the Apache-2.0 License. See [LICENSE](https://github.com/testit-tms/adapters-java/blob/main/LICENSE.md) for more information.
