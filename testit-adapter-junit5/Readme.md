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
    <version>1.1.2</version>
    <scope>compile</scope>
</dependency>
```

#### Gradle users

Add this dependency to your project build file:

```groovy
implementation "ru.testit:testit-adapter-junit5:1.1.2"
```

## Usage

#### Maven users

1. Add this dependency to your project POM:
    ````xml
     <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
        <aspectj.version>1.9.7</aspectj.version>
        <adapter.version>1.1.2</adapter.version>
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
    testImplementation "ru.testit:testit-adapter-junit5:1.1.2"
    testImplementation "ru.testit:testit-java-commons:1.1.2"
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

If you want to enable debug mode then see [How to enable debug logging?](https://github.com/testit-tms/adapters-java/tree/main/testit-java-commons)

### Annotations

Use annotations to specify information about autotest.

Description of annotations:
- `WorkItemIds` - linking an autotest to a test case.
- `DisplayName` - name of the autotest in Test IT.
- `ExternalId` - ID of the autotest within the project in Test IT.
- `Title` - title in the autotest card and the step card.
- `Description` - description in the autotest card and the step card.
- `Labels` - tags in the autotest card.
- `Links` - links in the autotest card.
- `Step` - the designation of the step.

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
   @WorkItemIds({"12345","54321"})
   @DisplayName("Simple test 2")
   @Title("test №2")
   @Description("Description")
   @Links(links = {@Link(url = "www.1.ru", title = "firstLink", description = "firstLinkDesc", type = LinkType.RELATED),
           @Link(url = "www.3.ru", title = "thirdLink", description = "thirdLinkDesc", type = LinkType.ISSUE),
           @Link(url = "www.2.ru", title = "secondLink", description = "secondLinkDesc", type = LinkType.BLOCKED_BY)})
   public void itsTrueReallyTrue() {
      step1();
      Adapter.addLinks("https://testit.ru/", "Test 1", "Desc 1", LinkType.ISSUE);
      Assertions.assertTrue(true);
   }

   @Step
   @Title("Step 1")
   @Description("Step 1 description")
   private void step1() {
      step2();
      Assertions.assertTrue(true);
      Adapter.addMessage("Message");
   }

   @Step
   @Title("Step 2")
   @Description("Step 2 description")
   private void step2() {
      Assertions.assertTrue(true);
      Adapter.addAttachment("/Users/user/screen.json");
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
      return new Object[][] {
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

* If you have suggestions for adding or removing projects, feel free to [open an issue](https://github.com/testit-tms/adapters-java/issues/new) to discuss it, or create a direct pull request after you edit the *README.md* file with necessary changes.
* Make sure to check your spelling and grammar.
* Create individual PR for each suggestion.
* Read the [Code Of Conduct](https://github.com/testit-tms/adapters-java/blob/main/CODE_OF_CONDUCT.md) before posting your first idea as well.

# License

Distributed under the Apache-2.0 License. See [LICENSE](https://github.com/testit-tms/adapters-java/blob/main/LICENSE.md) for more information.
