# TestIT Java Integrations
The repository contains new versions of adaptors for JVM-based test frameworks.

## Compatibility

| Test IT | Cucumber | JBehave | JUnit | TestNG |
|---------|----------|---------|-------|--------|
| 3.5     | 1.1      | 1.1     | 1.1   | 1.1    |
| 4.0     | 1.2      | 1.2     | 1.2   | 1.2    |
| 4.5     | 1.5      | 1.5     | 1.5   | 1.5    |
| 4.6     | 1.6      | 1.6     | 1.6   | 1.6    |
| 5.0     | 2.3      | 2.3     | 2.3   | 2.3    |

Supported test frameworks :
 1. [TestNG](https://github.com/testit-tms/adapters-java/tree/main/testit-adapter-testng)
 2. [Junit4](https://github.com/testit-tms/adapters-java/tree/main/testit-adapter-junit4)
 3. [Junit5](https://github.com/testit-tms/adapters-java/tree/main/testit-adapter-junit5)
 4. [Cucumber4](https://github.com/testit-tms/adapters-java/tree/main/testit-adapter-cucumber4)
 5. [Cucumber5](https://github.com/testit-tms/adapters-java/tree/main/testit-adapter-cucumber5)
 6. [Cucumber6](https://github.com/testit-tms/adapters-java/tree/main/testit-adapter-cucumber6)
 7. [Cucumber7](https://github.com/testit-tms/adapters-java/tree/main/testit-adapter-cucumber7)
 8. [JBehave](https://github.com/testit-tms/adapters-java/tree/main/testit-adapter-jbehave)
 9. [Selenide](https://github.com/testit-tms/adapters-java/tree/main/testit-adapter-selenide)

# 🚀 Warning
Since 2.2.0 version:
- If value from @WorkItemIds annotation not found in TMS then test result will NOT be uploaded.