# How to enable debug logging?
Create **simplelogger.properties** file in the resource directory of the project:
```text
org.slf4j.simpleLogger.defaultLogLevel=debug
org.slf4j.simpleLogger.showDateTime=true
org.slf4j.simpleLogger.dateTimeFormat=yyyy-MM-dd'T'HH:mm:ss.SSSZ
```

# How to add an attachment for a failed test?
You need to implement the AfterTestExecutionCallback interface (depends on test framework).
For example:

```java
import com.codeborne.selenide.Screenshots;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import ru.testit.services.Adapter;

import java.util.Objects;

public class AfterTestExtension implements AfterTestExecutionCallback {

    @Override
    public void afterTestExecution(ExtensionContext extensionContext) {
        if(extensionContext.getExecutionException().isPresent())
        {
            Adapter.addAttachments("any text", "file.txt");
            Adapter.addAttachments(Objects.requireNonNull(Screenshots.takeScreenShotAsFile()).getPath());
        }
    }
}
```

Then, add this extension to test class:
```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({AfterTestExtension.class})
public class ExampleTests {
    
    @Test
    public void TestSuccess() {
    }
}
```
