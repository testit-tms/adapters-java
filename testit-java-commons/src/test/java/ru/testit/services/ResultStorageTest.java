package ru.testit.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.testit.Helper;
import ru.testit.models.*;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ResultStorageTest {

    @Test
    void getTestResult_WithCorrectUuid() {
        // arrange
        ResultStorage storage = new ResultStorage();
        TestResult setResult = Helper.generateTestResult();
        String uuid = setResult.getUuid();
        storage.put(uuid, setResult);

        // act
        Optional<TestResult> getResult = storage.getTestResult(uuid);

        // assert
        Assertions.assertEquals(Optional.of(setResult), getResult);
    }

    @Test
    void getTestResult_WithWrongUuid() {
        // arrange
        ResultStorage storage = new ResultStorage();
        TestResult setResult = Helper.generateTestResult();
        String uuid = Helper.generateClassContainer().getUuid();
        storage.put(setResult.getUuid(), setResult);

        // act
        Optional<TestResult> getResult = storage.getTestResult(uuid);

        // assert
        Assertions.assertEquals(Optional.empty(), getResult);
    }

    @Test
    void getTestResult_WithEmptyUuid_InvokeNullPointerException() {
        // arrange
        ResultStorage storage = new ResultStorage();
        TestResult setResult = Helper.generateTestResult();
        String message = "Can't get result from storage: uuid can't be null";
        storage.put(setResult.getUuid(), setResult);

        // act
        Throwable expectedException = assertThrows(
                NullPointerException.class,
                () -> {
                    storage.getTestResult(null);
                });

        // assert
        Assertions.assertEquals(message, expectedException.getMessage());
    }

    @Test
    void getFixture_WithCorrectUuid() {
        // arrange
        ResultStorage storage = new ResultStorage();
        FixtureResult setResult = Helper.generateBeforeAllFixtureResult();
        String uuid = UUID.randomUUID().toString();
        storage.put(uuid, setResult);

        // act
        Optional<FixtureResult> getResult = storage.getFixture(uuid);

        // assert
        Assertions.assertEquals(Optional.of(setResult), getResult);
    }

    @Test
    void getFixture_WithWrongUuid() {
        // arrange
        ResultStorage storage = new ResultStorage();
        FixtureResult setResult = Helper.generateAfterAllFixtureResult();
        String uuid = Helper.generateClassContainer().getUuid();
        storage.put(UUID.randomUUID().toString(), setResult);

        // act
        Optional<FixtureResult> getResult = storage.getFixture(uuid);

        // assert
        Assertions.assertEquals(Optional.empty(), getResult);
    }

    @Test
    void getFixture_WithEmptyUuid_InvokeNullPointerException() {
        // arrange
        ResultStorage storage = new ResultStorage();
        FixtureResult setResult = Helper.generateAfterAllFixtureResult();
        String message = "Can't get result from storage: uuid can't be null";
        storage.put(UUID.randomUUID().toString(), setResult);

        // act
        Throwable expectedException = assertThrows(
                NullPointerException.class,
                () -> {
                    storage.getFixture(null);
                });

        // assert
        Assertions.assertEquals(message, expectedException.getMessage());
    }

    @Test
    void getStep_WithCorrectUuid() {
        // arrange
        ResultStorage storage = new ResultStorage();
        StepResult setResult = Helper.generateStepResult();
        String uuid = UUID.randomUUID().toString();
        storage.put(uuid, setResult);

        // act
        Optional<StepResult> getResult = storage.getStep(uuid);

        // assert
        Assertions.assertEquals(Optional.of(setResult), getResult);
    }

    @Test
    void getStep_WithWrongUuid() {
        // arrange
        ResultStorage storage = new ResultStorage();
        StepResult setResult = Helper.generateStepResult();
        String uuid = Helper.generateClassContainer().getUuid();
        storage.put(UUID.randomUUID().toString(), setResult);

        // act
        Optional<StepResult> getResult = storage.getStep(uuid);

        // assert
        Assertions.assertEquals(Optional.empty(), getResult);
    }

    @Test
    void getStep_WithEmptyUuid_InvokeNullPointerException() {
        // arrange
        ResultStorage storage = new ResultStorage();
        StepResult setResult = Helper.generateStepResult();
        String message = "Can't get result from storage: uuid can't be null";
        storage.put(UUID.randomUUID().toString(), setResult);

        // act
        Throwable expectedException = assertThrows(
                NullPointerException.class,
                () -> {
                    storage.getStep(null);
                });

        // assert
        Assertions.assertEquals(message, expectedException.getMessage());
    }

    @Test
    void getTestsContainer_WithCorrectUuid() {
        // arrange
        ResultStorage storage = new ResultStorage();
        MainContainer setResult = Helper.generateMainContainer();
        String uuid = UUID.randomUUID().toString();
        storage.put(uuid, setResult);

        // act
        Optional<MainContainer> getResult = storage.getTestsContainer(uuid);

        // assert
        Assertions.assertEquals(Optional.of(setResult), getResult);
    }

    @Test
    void getTestsContainer_WithWrongUuid() {
        // arrange
        ResultStorage storage = new ResultStorage();
        MainContainer setResult = Helper.generateMainContainer();
        String uuid = Helper.generateClassContainer().getUuid();
        storage.put(UUID.randomUUID().toString(), setResult);

        // act
        Optional<MainContainer> getResult = storage.getTestsContainer(uuid);

        // assert
        Assertions.assertEquals(Optional.empty(), getResult);
    }

    @Test
    void getTestsContainer_WithEmptyUuid_InvokeNullPointerException() {
        // arrange
        ResultStorage storage = new ResultStorage();
        MainContainer setResult = Helper.generateMainContainer();
        String message = "Can't get result from storage: uuid can't be null";
        storage.put(UUID.randomUUID().toString(), setResult);

        // act
        Throwable expectedException = assertThrows(
                NullPointerException.class,
                () -> {
                    storage.getTestsContainer(null);
                });

        // assert
        Assertions.assertEquals(message, expectedException.getMessage());
    }

    @Test
    void getClassContainer_WithCorrectUuid() {
        // arrange
        ResultStorage storage = new ResultStorage();
        ClassContainer setResult = Helper.generateClassContainer();
        String uuid = setResult.getUuid();
        storage.put(uuid, setResult);

        // act
        Optional<ClassContainer> getResult = storage.getClassContainer(uuid);

        // assert
        Assertions.assertEquals(Optional.of(setResult), getResult);
    }

    @Test
    void getClassContainer_WithWrongUuid() {
        // arrange
        ResultStorage storage = new ResultStorage();
        ClassContainer setResult = Helper.generateClassContainer();
        String uuid = UUID.randomUUID().toString();
        storage.put(setResult.getUuid(), setResult);

        // act
        Optional<ClassContainer> getResult = storage.getClassContainer(uuid);

        // assert
        Assertions.assertEquals(Optional.empty(), getResult);
    }

    @Test
    void getClassContainer_WithEmptyUuid_InvokeNullPointerException() {
        // arrange
        ResultStorage storage = new ResultStorage();
        ClassContainer setResult = Helper.generateClassContainer();
        String message = "Can't get result from storage: uuid can't be null";
        storage.put(setResult.getUuid(), setResult);

        // act
        Throwable expectedException = assertThrows(
                NullPointerException.class,
                () -> {
                    storage.getClassContainer(null);
                });

        // assert
        Assertions.assertEquals(message, expectedException.getMessage());
    }

    @Test
    void put_WithEmptyUuid_InvokeNullPointerException() {
        // arrange
        ResultStorage storage = new ResultStorage();
        ClassContainer setResult = Helper.generateClassContainer();
        String message = "Can't put result to storage: uuid can't be null";

        // act
        Throwable expectedException = assertThrows(
                NullPointerException.class,
                () -> {
                    storage.put(null, setResult);
                });

        // assert
        Assertions.assertEquals(message, expectedException.getMessage());
    }

    @Test
    void put_WithEmptyResult_InvokeNullPointerException() {
        // arrange
        ResultStorage storage = new ResultStorage();
        String uuid = UUID.randomUUID().toString();
        String message = "Can't put result to storage: item can't be null";

        // act
        Throwable expectedException = assertThrows(
                NullPointerException.class,
                () -> {
                    storage.put(uuid, null);
                });

        // assert
        Assertions.assertEquals(message, expectedException.getMessage());
    }
}
