# Test IT TMS adapters for Java
![Test IT](https://raw.githubusercontent.com/testit-tms/adapters-python/master/images/banner.png)

# TestNG

## Getting Started

### Installation
Copy adapters-java-testng-0.1.jar, adapters-java-testng-0.1.pom 
and adapters-java-testng-0.1-sources.jar to `{%HOME_FOLDER%}\.m2\repository\ru\testit\adapters-java-testng\{%APP_VERSION%}`

For example, `/Users/user01/.m2/repository/ru/testit/adapters-java-testng/0.1`

## Usage

#### Maven
1. Add to **pom.xml** following lines:
    ````
    <dependencies>
        <dependency>
            <groupId>org.testng</groupId>
            <artifactId>testng</artifactId>
            <version>7.5</version>
        </dependency>
        <dependency>
            <groupId>ru.testit</groupId>
            <artifactId>adapters-java-testng</artifactId>
            <version>0.1</version>
        </dependency>
        <dependency>
            <groupId>org.aspectj</groupId>
            <artifactId>aspectjrt</artifactId>
            <version>1.9.7</version>
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
                        <goals>
                            <goal>compile</goal>
                            <goal>test-compile</goal>
                        </goals>
                    </execution>
                </executions>
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
   id "io.freefair.aspectj.post-compile-weaving" version "6.5.0.2"
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
   testImplementation 'ru.testit:adapters-java-testng:0.1'
   testImplementation 'org.testng:testng:7.5'
   testAspect "ru.testit:adapters-java-testng:0.1"
}

test {
   useTestNG() {
      listeners << 'ru.testit.listener.BaseTestNgListener'
   }
}

compileTestJava {
   ajc {
      enabled = true
      classpath
      options {
         aspectpath.setFrom configurations.testAspect
         compilerArgs = []
      }
   }
}
```
2. Press "Reload All Gradle Projects" button

#### How to run tests in IntelliJ IDEA?
1. Do previous steps
2. Open IDE preferences -> Build, Execution, Deployment -> Build Tools -> Maven -> Runner. 
Select "Delegate IDE build/run actions to Maven" checkbox

   ![IDEPref](https://github.com/testit-tms/adapters-java-testng/images/IDEPref.jpg)
3. Create new run/debug configuration with TestNG and add our listener
   
    ![IDEConf](https://github.com/testit-tms/adapters-java-testng/images/IDEConf.jpg)
4. Click "Build Project" button and after "Run" button

#### How to run tests with testing.xml?
1. Do previous steps
2. Create or copy testing.xml to your project folder and change information about tests. 
You can find example in examples/commandline folder
3. Run following command:
````
java -Dfile.encoding=UTF-8 -classpath <libs paths> org.testng.TestNG temp-testng-customsuite.xml
````
for example:
````
/Library/Java/JavaVirtualMachines/jdk1.8.0_333.jdk/Contents/Home/bin/java -Dfile.encoding=UTF-8 -classpath /Library/Java/JavaVirtualMachines/jdk1.8.0_333.jdk/Contents/Home/jre/lib/charsets.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_333.jdk/Contents/Home/jre/lib/deploy.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_333.jdk/Contents/Home/jre/lib/ext/cldrdata.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_333.jdk/Contents/Home/jre/lib/ext/dnsns.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_333.jdk/Contents/Home/jre/lib/ext/jaccess.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_333.jdk/Contents/Home/jre/lib/ext/jfxrt.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_333.jdk/Contents/Home/jre/lib/ext/localedata.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_333.jdk/Contents/Home/jre/lib/ext/nashorn.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_333.jdk/Contents/Home/jre/lib/ext/sunec.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_333.jdk/Contents/Home/jre/lib/ext/sunjce_provider.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_333.jdk/Contents/Home/jre/lib/ext/sunpkcs11.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_333.jdk/Contents/Home/jre/lib/ext/zipfs.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_333.jdk/Contents/Home/jre/lib/javaws.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_333.jdk/Contents/Home/jre/lib/jce.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_333.jdk/Contents/Home/jre/lib/jfr.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_333.jdk/Contents/Home/jre/lib/jfxswt.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_333.jdk/Contents/Home/jre/lib/jsse.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_333.jdk/Contents/Home/jre/lib/management-agent.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_333.jdk/Contents/Home/jre/lib/plugin.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_333.jdk/Contents/Home/jre/lib/resources.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_333.jdk/Contents/Home/jre/lib/rt.jar:/Users/dmitry.gridnev/IdeaProjects/test-adapter/target/test-classes:/Users/dmitry.gridnev/IdeaProjects/test-adapter/target/classes:/Users/dmitry.gridnev/.m2/repository/org/testng/testng/7.5/testng-7.5.jar:/Users/dmitry.gridnev/.m2/repository/com/google/code/findbugs/jsr305/3.0.1/jsr305-3.0.1.jar:/Users/dmitry.gridnev/.m2/repository/org/slf4j/slf4j-api/1.7.32/slf4j-api-1.7.32.jar:/Users/dmitry.gridnev/.m2/repository/com/beust/jcommander/1.78/jcommander-1.78.jar:/Users/dmitry.gridnev/.m2/repository/org/webjars/jquery/3.5.1/jquery-3.5.1.jar:/Users/dmitry.gridnev/.m2/repository/ru/testit/adapters-java-testng/0.1/adapters-java-testng-0.1.jar:/Users/dmitry.gridnev/.m2/repository/org/slf4j/slf4j-simple/1.7.36/slf4j-simple-1.7.36.jar:/Users/dmitry.gridnev/.m2/repository/com/fasterxml/jackson/core/jackson-annotations/2.13.3/jackson-annotations-2.13.3.jar:/Users/dmitry.gridnev/.m2/repository/com/fasterxml/jackson/core/jackson-databind/2.13.3/jackson-databind-2.13.3.jar:/Users/dmitry.gridnev/.m2/repository/com/fasterxml/jackson/core/jackson-core/2.13.3/jackson-core-2.13.3.jar:/Users/dmitry.gridnev/.m2/repository/org/apache/httpcomponents/httpclient/4.5.13/httpclient-4.5.13.jar:/Users/dmitry.gridnev/.m2/repository/org/apache/httpcomponents/httpcore/4.4.13/httpcore-4.4.13.jar:/Users/dmitry.gridnev/.m2/repository/commons-logging/commons-logging/1.2/commons-logging-1.2.jar:/Users/dmitry.gridnev/.m2/repository/commons-codec/commons-codec/1.11/commons-codec-1.11.jar:/Users/dmitry.gridnev/.m2/repository/org/apache/commons/commons-lang3/3.12.0/commons-lang3-3.12.0.jar:/Users/dmitry.gridnev/.m2/repository/org/aspectj/aspectjrt/1.9.7/aspectjrt-1.9.7.jar org.testng.TestNG temp-testng-customsuite.xml
````

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

