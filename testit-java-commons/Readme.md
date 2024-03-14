# How to enable debug logging?
Create **simplelogger.properties** file in the resource directory of the project:
```text
org.slf4j.simpleLogger.defaultLogLevel=debug
org.slf4j.simpleLogger.showDateTime=true
org.slf4j.simpleLogger.dateTimeFormat=yyyy-MM-dd'T'HH:mm:ss.SSSZ
```

# How to add an attachment for a failed test?
You need to implement AfterEach method (depends on test framework).
For example:

```java
import com.codeborne.selenide.Screenshots;
import org.junit.jupiter.api.AfterEach;
import ru.testit.services.Adapter;

import java.util.Objects;

@AfterEach()
public void afterEachMethod() {
    Adapter.addAttachments("any text", "file.txt");
    Adapter.addAttachments(Objects.requireNonNull(Screenshots.takeScreenShotAsFile()).getPath());
}
```
