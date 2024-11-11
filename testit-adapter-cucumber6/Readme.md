# Test IT TMS Adapter for Cucumber6
![Test IT](https://raw.githubusercontent.com/testit-tms/adapters-python/master/images/banner.png)

## Getting Started

### Installation

#### Maven Users

Add this dependency to your project POM:

```xml
<dependency>
    <groupId>ru.testit</groupId>
    <artifactId>testit-adapter-cucumber6</artifactId>
    <version>2.3.4</version>
    <scope>compile</scope>
</dependency>
```

#### Gradle Users

Add this dependency to your project build file:

```groovy
implementation "ru.testit:testit-adapter-cucumber6:2.3.4"
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
        <gherkin.version>18.0.0</gherkin.version>
        <cucumber.version>6.11.0</cucumber.version>
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
            <artifactId>testit-adapter-cucumber6</artifactId>
            <version>${adapter.version}</version>
        </dependency>
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>gherkin</artifactId>
            <version>${gherkin.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>cucumber-core</artifactId>
            <version>${cucumber.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>cucumber-java</artifactId>
            <version>${cucumber.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>cucumber-testng</artifactId>
            <version>${cucumber.version}</version>
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
        <gherkin.version>18.0.0</gherkin.version>
        <cucumber.version>6.11.0</cucumber.version>
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
            <artifactId>testit-adapter-cucumber5</artifactId>
            <version>${adapter.version}</version>
        </dependency>
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>cucumber-java</artifactId>
            <version>${cucumber.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>cucumber-junit</artifactId>
            <version>${cucumber.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>cucumber-junit-platform-engine</artifactId>
            <version>${cucumber.version}</version>
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
        <gherkin.version>18.0.0</gherkin.version>
        <cucumber.version>6.11.0</cucumber.version>
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
            <artifactId>testit-adapter-cucumber5</artifactId>
            <version>${adapter.version}</version>
        </dependency>
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>gherkin</artifactId>
            <version>${gherkin.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>cucumber-core</artifactId>
            <version>${cucumber.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>cucumber-java</artifactId>
            <version>${cucumber.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>cucumber-junit</artifactId>
            <version>${cucumber.version}</version>
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

def cucumberVersion = "6.11.0"
def cucumberGherkinVersion = "18.0.0"

dependencies {
    testImplementation "ru.testit:testit-adapter-cucumber6:2.3.4"
    testImplementation "ru.testit:testit-java-commons:2.3.4"
    testImplementation "org.testng:testng:7.5"
    testImplementation("io.cucumber:gherkin:$cucumberGherkinVersion")
    testImplementation("io.cucumber:cucumber-core:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-java:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-testng:$cucumberVersion")
}

test {
    useTestNG()
    systemProperties(System.getProperties())
    environment(System.getenv())
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

def cucumberVersion = "6.11.0"
def cucumberGherkinVersion = "18.0.0"

dependencies {
    testImplementation "ru.testit:testit-adapter-cucumber6:2.3.4"
    testImplementation "ru.testit:testit-java-commons:2.3.4"
    testImplementation("io.cucumber:gherkin:$cucumberGherkinVersion")
    testImplementation("io.cucumber:cucumber-core:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-java:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-junit:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-junit-platform-engine:$cucumberVersion")
    testImplementation "org.junit.jupiter:junit-jupiter-api:5.8.1"
    testRuntimeOnly "org.junit.jupiter:junit-jupiter-engine:5.8.1"
}

test {
    useJUnitPlatform()
    systemProperties(System.getProperties())
    environment(System.getenv())
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

def cucumberVersion = "6.11.0"
def cucumberGherkinVersion = "18.0.0"
def junit4Version = "4.12"

dependencies {
    testImplementation "ru.testit:testit-adapter-cucumber6:2.3.4"
    testImplementation "ru.testit:testit-java-commons:2.3.4"
    testImplementation("io.cucumber:gherkin:$cucumberGherkinVersion")
    testImplementation("io.cucumber:cucumber-core:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-java:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-junit:$cucumberVersion")
    testImplementation "junit:junit:$junit4Version"
}

test {
    useJUnit()
    systemProperties(System.getProperties())
    environment(System.getenv())
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

If you want to enable debug mode then see [How to enable debug logging?](https://github.com/testit-tms/adapters-java/tree/main/testit-java-commons)

If you want to add attachment for a failed test then see [How to add an attachment for a failed test?](https://github.com/testit-tms/adapters-java/tree/main/testit-java-commons)

### Tags

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
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;
import ru.testit.models.LinkType;
import ru.testit.services.Adapter;


public class SampleTests {
    @Given("I authorize on the portal")
    public void authorization() {
        Assert.assertTrue(setLogin("User_1"));
        Assert.assertTrue(setPassword("Pass123"));
    }

    private boolean setLogin(String login) {
        return login.equals("User_1");
    }

    private boolean setPassword(String password) {
        return password.equals("Pass123");
    }

    @When("I create a project")
    public void createProject() {
        Assert.assertTrue(true);
    }

    @And("I open the project")
    public void enterProject() {
        Assert.assertTrue(true);
    }

    @And("I create a section")
    public void createSection() {
        Assert.assertTrue(true);
    }

    @And("I create a section - failed")
    public void createFailedSection() {
        Assert.fail();
    }

    @Then("I create a test case")
    public void createTestCase() {
        Adapter.addLinks("https://testit.ru/", "Test 1", "Desc 1", LinkType.ISSUE);
        Assert.assertTrue(true);
    }

    @Then("I check something")
    public void requiredAnnotationsTest() {
        Assert.assertTrue(true);
    }

    @Then("I check something - failed")
    public void requiredAnnotationsFailedTest() {
        Assert.fail();
    }
}
```

```gherkin
Feature: Sample

  Background:
    Given I authorize on the portal

  @ExternalId=with_all_annotations
  @DisplayName=Test_with_all_annotations
  @WorkItemIds=123
  @Title=Title_in_the_autotest_card
  @Description=Test_with_all_annotations
  @Labels=Tag1,Tag2
  @Links={"url":"https://dumps.example.com/module/repository","title":"Repository","description":"Example_of_repository","type":"Repository"}
  Scenario: Create new project, section and test case
    When I create a project
    And I open the project
    And I create a section
    Then I create a test case
```

#### Parameterized test

```java
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

public class ParameterizedTest {
    private int result;

    @When("Summing {int}+{int}")
    public void sum(int left, int right){
        this.result = left + right;
    }

    @Then("Result is {int}")
    public void result(int result){
        Assert.assertEquals(this.result, result);
    }
}
```

```gherkin
Feature: Rule
  Tests that use Rule

  @DisplayName=sum:{left}+{right}={result}
  @ExternalId={result}
  Scenario Outline: Summing
    When Summing <left>+<right>
    Then Result is <result>

    Examples:
      | left | right | result |
      | 1    | 1     | 3      |
      | 9    | 9     | 18     |
```

# Contributing

You can help to develop the project. Any contributions are **greatly appreciated**.

* If you have suggestions for adding or removing projects, feel free to [open an issue](https://github.com/testit-tms/adapters-java/issues/new) to discuss it, or create a direct pull request after you edit the *README.md* file with necessary changes.
* Make sure to check your spelling and grammar.
* Create individual PR for each suggestion.
* Read the [Code Of Conduct](https://github.com/testit-tms/adapters-java/blob/main/CODE_OF_CONDUCT.md) before posting your first idea as well.

# License

Distributed under the Apache-2.0 License. See [LICENSE](https://github.com/testit-tms/adapters-java/blob/main/LICENSE.md) for more information.
