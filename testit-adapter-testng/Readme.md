# Test IT TMS adapters for Java
![Test IT](https://raw.githubusercontent.com/testit-tms/adapters-python/master/images/banner.png)

# TestNG

## Getting Started

### Installation
Copy the contents of the **"packages"** folder to to `{%HOME_FOLDER%}/.m2/repository/`

## Usage

#### Maven
1. Add to **pom.xml** following lines:
    ````
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
            <version>0.1</version>
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
                    <weaveDependencies>
                        <weaveDependency>
                            <groupId>ru.testit</groupId>
                            <artifactId>adapters-java-testng</artifactId>
                        </weaveDependency>
                    </weaveDependencies>
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

#### Gradle
1. Add to **build.gradle** following lines:
```
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
    testImplementation 'ru.testit:testit-adapter-testng:0.1'
    testImplementation 'ru.testit:testit-java-commons:0.1'
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

Create **testit.properties** file in the resource directory of the project:
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

### Tags

Annotations can be used to specify information about autotest.

Description of Annotations (\* - required):
- `WorkItemID` - linking an autotest to a test case
- \*`displayName` - name of the autotest in the Test IT system (can be replaced with documentation strings)
- \*`ExternalID` - ID of the autotest within the project in the Test IT System
- `Title` - title in the autotest card
- `Description` - description in the autotest card
- `Labels` - tags in the work item
- `Link` - links in the autotest card
- `Step` - the designation of the step called in the body of the test or other step

All decorators support the use of parameterization attributes

Description of methods:
- `AddLink` - links in the autotest result

### Examples

```java
import ru.testit.annotations.*;
import ru.testit.models.LinkItem;
import org.testng.Assert;
import org.testng.annotations.*;

@Test()
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
    }

    @Step
    @Title("Step 2")
    @Description("Step 2 description")
    private void step2() {
        Assert.assertTrue(true);
    }

    @Test
    @ExternalId("Simple_test_2")
    @DisplayName("Simple test 2")
    @WorkItemId("12345")
    @Title("Simple test 2")
    @Description("Simple test 2 description")
    @Links(links = {@Link(url = "www.1.ru", title = "firstLink", description = "firstLinkDesc", type = LinkType.RELATED),
                @Link(url = "www.3.ru", title = "thirdLink", description = "thirdLinkDesc", type = LinkType.ISSUE),
                @Link(url = "www.2.ru", title = "secondLink", description = "secondLinkDesc", type = LinkType.BLOCKED_BY)})

    public void simpleTest2() {
        step1();
        TestITClient.addLink(new LinkItem("doSecondLink", "www.test.com", "testDesc", LinkType.RELATED));
        Assert.assertTrue(true);
    }
}
```

# Contributing

You can help to develop the project. Any contributions are **greatly appreciated**.

* If you have suggestions for adding or removing projects, feel free to [open an issue](https://github.com/testit-tms/adapters-java-testng/issues/new) to discuss it, or directly create a pull request after you edit the *README.md* file with necessary changes.
* Please make sure you check your spelling and grammar.
* Create individual PR for each suggestion.
* Please also read through the [Code Of Conduct](https://github.com/testit-tms/adapters-java-testng/blob/master/CODE_OF_CONDUCT.md) before posting your first idea as well.

# License

Distributed under the Apache-2.0 License. See [LICENSE](https://github.com/testit-tms/adapters-java-testng/blob/master/LICENSE.md) for more information.

