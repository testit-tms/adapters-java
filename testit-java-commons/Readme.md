# How to enable debug logging?
Create **simplelogger.properties** file in the resource directory of the project:
```text
org.slf4j.simpleLogger.defaultLogLevel=debug
org.slf4j.simpleLogger.showDateTime=true
org.slf4j.simpleLogger.dateTimeFormat=yyyy-MM-dd'T'HH:mm:ss.SSSZ
```

# How to add an attachment for a failed test?
You need to implement the AdapterListener interface and override the beforeTestStop method.
For example:

```java
import ru.testit.services.Adapter;
import ru.testit.listener.AdapterListener;
import ru.testit.models.ItemStatus;
import ru.testit.models.TestResult;
import ru.testit.services.Adapter;

public class AttachmentManager implements AdapterListener {

    @Override
    public void beforeTestStop(final TestResult result) {
        if (result.getItemStatus().equals(ItemStatus.FAILED)) {
            // Add a screenshot
            Adapter.addAttachments("Screenshot.jpg", new ByteArrayInputStream(((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES)));
            
            // Add log file
            Adapter.addAttachments("/logs/failed.log");
            
            // Add any text
            Adapter.addAttachments("any text", "file.txt");
        }
    }
}
```

After that, you need to add file **ru.testit.listener.AdapterListener** to **resources/META-INF/services** folder:
```text
<your-package>.AttachmentManager
```
