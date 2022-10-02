# Test IT TMS Adapter for Cucumber5
![Test IT](https://raw.githubusercontent.com/testit-tms/adapters-python/master/images/banner.png)

## Getting Started

### Installation

#### Maven Users

Add this dependency to your project POM:

```xml
<dependency>
    <groupId>ru.testit</groupId>
    <artifactId>testit-adapter-cucumber5</artifactId>
    <version>2.1.0</version>
    <scope>compile</scope>
</dependency>
```

#### Gradle Users

Add this dependency to your project build file:

```groovy
implementation "ru.testit:testit-adapter-cucumber5:2.1.0"
```

## Usage

#### Maven Users

##### TestNG

1. Add this dependency to your project POM:
    ````xml
     <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
        <adapter.version>2.1.0</adapter.version>
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
            <artifactId>testit-adapter-cucumber5</artifactId>
            <version>${adapter.version}</version>
        </dependency>
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>gherkin</artifactId>
            <version>5.1.0</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>cucumber-core</artifactId>
            <version>5.5.0</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>cucumber-java</artifactId>
            <version>5.5.0</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>cucumber-testng</artifactId>
            <version>5.5.0</version>
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
        <adapter.version>2.1.0</adapter.version>
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
            <artifactId>gherkin</artifactId>
            <version>5.1.0</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>cucumber-core</artifactId>
            <version>5.5.0</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>cucumber-java</artifactId>
            <version>5.5.0</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>cucumber-junit</artifactId>
            <version>5.5.0</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>cucumber-junit-platform-engine</artifactId>
            <version>5.5.0</version>
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
        <adapter.version>2.1.0</adapter.version>
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
            <version>5.1.0</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>cucumber-core</artifactId>
            <version>5.5.0</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>cucumber-java</artifactId>
            <version>5.5.0</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>cucumber-junit</artifactId>
            <version>5.5.0</version>
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
    id 'java'
}

group 'org.example'
version '1.0-SNAPSHOT'

repositories {
    mavenCentral()
    mavenLocal()
}

def cucumberVersion = "5.1.2"
def cucumberGherkinVersion = "5.1.0"

dependencies {
    testImplementation "ru.testit:testit-adapter-cucumber5:2.1.0"
    testImplementation "ru.testit:testit-java-commons:2.1.0"
    testImplementation 'org.testng:testng:7.5'
    testImplementation("io.cucumber:gherkin:$cucumberGherkinVersion")
    testImplementation("io.cucumber:cucumber-core:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-java:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-testng:$cucumberVersion")
}

test {
    useTestNG()
}
```
2. Press the **Reload All Gradle Projects** button.

##### Junit5

1. Add this dependency to your project build file:
```groovy
plugins {
    id 'java'
}

group 'org.example'
version '1.0-SNAPSHOT'

repositories {
    mavenCentral()
    mavenLocal()
}

def cucumberVersion = "5.5.0"
def cucumberGherkinVersion = "5.1.0"

dependencies {
    testImplementation "ru.testit:testit-adapter-cucumber5:2.1.0"
    testImplementation "ru.testit:testit-java-commons:2.1.0"
    testImplementation("io.cucumber:gherkin:$cucumberGherkinVersion")
    testImplementation("io.cucumber:cucumber-core:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-java:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-junit:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-junit-platform-engine:$cucumberVersion")
    testImplementation 'org.junit.jupiter:junit-jupiter-api:5.8.1'
    testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.8.1'
}

test {
    useJUnitPlatform()
}
```
2. Press the **Reload All Gradle Projects** button.

##### Junit4

1. Add this dependency to your project build file:
```groovy
plugins {
    id 'java'
}

group 'org.example'
version '1.0-SNAPSHOT'

repositories {
    mavenCentral()
    mavenLocal()
}

def cucumberVersion = "5.5.0"
def cucumberGherkinVersion = "5.1.0"
def junit4Version = "4.12"

dependencies {
    testImplementation "ru.testit:testit-adapter-cucumber5:2.1.0"
    testImplementation "ru.testit:testit-java-commons:2.1.0"
    testImplementation("io.cucumber:gherkin:$cucumberGherkinVersion")
    testImplementation("io.cucumber:cucumber-core:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-java:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-junit:$cucumberVersion")
    testImplementation "junit:junit:$junit4Version"
}

test {
    useJUnit()
}
```
2. Press the **Reload All Gradle Projects** button.

### Configuration

#### File

1. Create **testit.properties** file in the resource directory of the project:
    ``` 
    url={%URL%}
    privateToken={%USER_PRIVATE_TOKEN%} 
    projectId={%PROJECT_ID%} 
    configurationId={%CONFIGURATION_ID%}
    testRunId={%TEST_RUN_ID%}
    testRunName={%TEST_RUN_NAME%}
    adapterMode={%ADAPTER_MODE%}
    ```
2. Fill parameters with your configuration, where:
   * `URL` - location of the TMS instance.
   * `USER_PRIVATE_TOKEN` - API secret key. To do that:
      1. Go to the `https://{DOMAIN}/user-profile` profile.
      2. Copy the API secret key.
         
   * `PROJECT_ID` - ID of a project in TMS instance.
      1. Create a project.
      2. Open DevTools > Network.
      3. Go to the project `https://{DOMAIN}/projects/20/tests`.
      4. GET-request project, Preview tab, copy iID field.
   * `CONFIGURATION_ID` - ID of a configuration in TMS instance.
      1. Create a project.
      2. Open DevTools > Network.
      3. Go to the project `https://{DOMAIN}/projects/20/tests`.
      4. GET-request configurations, Preview tab, copy id field.
         
   * `TEST_RUN_ID` - ID of the created test-run in TMS instance. `TEST_RUN_ID` is optional. If it is not provided, it is created automatically.
     
   * `TEST_RUN_NAME` - name of the new test-run.`TEST_RUN_NAME` is optional. If it is not provided, it is created automatically.
     
   * `ADAPTER_MODE` - adapter mode. Default value - 0. The adapter supports following modes:
      * 0 - in this mode, the adapter filters tests by test run ID and configuration ID, and sends the results to the test run.
      * 1 - in this mode, the adapter sends all results to the test run without filtering.
      * 2 - in this mode, the adapter creates a new test run and sends results to the new test run.
        
        
#### ENV

You can use environment variables (environment variables take precedence over file variables):

* `TMS_URL` - location of the TMS instance.
  
* `TMS_PRIVATE_TOKEN` - API secret key.
  
* `TMS_PROJECT_ID` - ID of a project in TMS instance.
  
* `TMS_CONFIGURATION_ID` - ID of a configuration in TMS instance.

* `TMS_ADAPTER_MODE` - adapter mode. Default value - 0.
  
* `TMS_TEST_RUN_ID` - ID of the created test-run in TMS instance. `TMS_TEST_RUN_ID` is optional. If it is not provided, it is created automatically.
  
* `TMS_TEST_RUN_NAME` - name of the new test-run.`TMS_TEST_RUN_NAME` is optional. If it is not provided, it is created automatically.
  
* `TMS_CONFIG_FILE` - name of the configuration file. `TMS_CONFIG_FILE` is optional. If it is not provided, it is used default file name.
  
  
#### Command line

You also can CLI variables (CLI variables take precedence over environment variables):

* `tmsUrl` - location of the TMS instance.
  
* `tmsPrivateToken` - API secret key.
  
* `tmsProjectId` - ID of a project in TMS instance.
  
* `tmsConfigurationId` - ID of a configuration in TMS instance.

* `tmsAdapterMode` - adapter mode. Default value - 0.

* `tmsTestRunId` - ID of the created test-run in TMS instance. `tmsTestRunId` is optional. If it is not provided, it is created automatically.
  
* `tmsTestRunName` - name of the new test-run.`tmsTestRunName` is optional. If it is not provided, it is created automatically.
  
* `tmsConfigFile` - name of the configuration file. `tmsConfigFile` is optional. If it is not provided, it is used default file name.

#### Examples

##### Gradle
```
gradle test -DtmsUrl=http://localhost:8080 -DtmsPrivateToken=Token -DtmsProjectId=f5da5bab-380a-4382-b36f-600083fdd795 -DtmsConfigurationId=3a14fa45-b54e-4859-9998-cc502d4cc8c6
-DtmsAdapterMode=0 -DtmsTestRunId=a17269da-bc65-4671-90dd-d3e3da92af80 -DtmsTestRunName=Regress
```

##### Maven
```
maven test -DtmsUrl=http://localhost:8080 -DtmsPrivateToken=Token -DtmsProjectId=f5da5bab-380a-4382-b36f-600083fdd795 -DtmsConfigurationId=3a14fa45-b54e-4859-9998-cc502d4cc8c6
-DtmsAdapterMode=0 -DtmsTestRunId=a17269da-bc65-4671-90dd-d3e3da92af80 -DtmsTestRunName=Regress
```

### Tags

Use tags to specify information about autotest.

Description of tags:
- `WorkItemIds` - linking an autotest to a test case.
- `DisplayName` - name of the autotest in Test IT.
- `ExternalId` - ID of the autotest within the project in Test IT.
- `Title` - title in the autotest card.
- `Description` - description in the autotest card.
- `Labels` - tags in the autotest card.
- `Links` - links in the autotest card.

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
        Assert.assertTrue(false);
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
        Assert.assertTrue(false);
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
