# Test IT TMS adapter for JUnit 4
![Test IT](https://raw.githubusercontent.com/testit-tms/adapters-python/master/images/banner.png)

## Getting Started

### Installation

#### Maven users

Add this dependency to your project's POM:

```xml
<dependency>
    <groupId>ru.testit</groupId>
    <artifactId>testit-adapter-junit4</artifactId>
    <version>LATEST_VERSION</version>
    <scope>compile</scope>
</dependency>
```

#### Gradle users

Add this dependency to your project's build file:

```groovy
implementation "ru.testit:testit-adapter-junit4:LATEST_VERSION"
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
            <version>LATEST_VERSION</version>
        </dependency>
        <dependency>
            <groupId>ru.testit</groupId>
            <artifactId>testit-adapter-junit4</artifactId>
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
}

repositories {
   mavenCentral()
   mavenLocal()
}

dependencies {
    testImplementation 'org.aspectj:aspectjrt:1.9.7'
    testImplementation "ru.testit:testit-adapter-junit4:$LATEST_VERSION"
    testImplementation "ru.testit:testit-java-commons:$LATEST_VERSION"
    testImplementation 'junit:junit:4.12'
    testImplementation 'org.junit.platform:junit-platform-runner:1.6.3'
    aspectConfig "org.aspectj:aspectjweaver:1.9.7"
}

test {
    useJUnit()
    doFirst {
        def weaver = configurations.aspectConfig.find { it.name.contains("aspectjweaver") }
        jvmArgs += "-javaagent:$weaver"
    }
}
```
2. Press "Reload All Gradle Projects" button

### Configuration

Create **testit.properties** file in the resource directory of the project or set environment variables (environment variables take precedence over file variables):
``` 
URL={%URL%}
PrivateToken={%USER_PRIVATE_TOKEN%} 
ProjectId={%PROJECT_ID%} 
ConfigurationId={%CONFIGURATION_ID%}
TestRunId={%TEST_RUN_ID%}
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

> TEST_RUN_ID is optional. If it's not provided than it create automatically.

### Annotations

Annotations can be used to specify information about autotest.

Description of Annotations (\* - required):
- \*`RunWith(BaseJunit4Runner.class)` - connect the adapter package to run tests
- `WorkItemIds` - linking an autotest to a test case
- \*`DisplayName` - name of the autotest in the Test IT system
- \*`ExternalID` - ID of the autotest within the project in the Test IT System
- `Title` - title in the autotest card and the step card
- `Labels` - tags in the autotest card
- `Link` - links in the autotest card
- `Step` - the designation of the step

Description of methods:
- `Adapter.addLink` - link in the autotest result
- `Adapter.addLinks` - links in the autotest result
- `Adapter.addAttachment` - attachment in the autotest result
- `Adapter.addAttachments` - attachments in the autotest result
- `Adapter.addMessage` - message in the autotest result

### Examples

```java
package ru.testit.samples;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import ru.testit.annotations.*;
import ru.testit.listener.BaseJunit4Runner;
import ru.testit.models.LinkItem;
import ru.testit.tms.client.TMSClient;

@RunWith(BaseJunit4Runner.class)
public class SampleTest {

   @Test
   @ExternalId("Simple_test_1")
   @DisplayName("Simple test 1")
   public void simpleTest1() {
      Assert.assertTrue(true);
   }

   @Test
   @ExternalId("Simple_test_2")
   @WorkItemIds({"12345","54321"})
   @DisplayName("Simple test 2")
   @Title("test №2")
   @Links(links = {@Link(url = "www.1.ru", title = "firstLink", description = "firstLinkDesc", type = LinkType.RELATED),
           @Link(url = "www.3.ru", title = "thirdLink", description = "thirdLinkDesc", type = LinkType.ISSUE),
           @Link(url = "www.2.ru", title = "secondLink", description = "secondLinkDesc", type = LinkType.BLOCKED_BY)})
   public void itsTrueReallyTrue() {
      step1();
      Adapter.addLink("https://testit.ru/", "Test 1", "Desc 1", LinkType.ISSUE);
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

