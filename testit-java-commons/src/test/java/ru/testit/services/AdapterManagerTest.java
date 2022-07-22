package ru.testit.services;

import javafx.beans.binding.When;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.testit.Helper;
import ru.testit.models.*;
import ru.testit.writers.HttpWriter;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static org.mockito.Mockito.*;

public class AdapterManagerTest {

    private ThreadContext threadContext;
    private ResultStorage storage;
    private HttpWriter writer;
    private Consumer update;

    @BeforeEach
    void init() {
        this.storage = Mockito.mock(ResultStorage.class);
        this.threadContext = Mockito.mock(ThreadContext.class);
        this.writer = Mockito.mock(HttpWriter.class);
        this.update = Mockito.mock(Consumer.class);
    }

    @Test
    void startTests_InvokeStartLaunchHandler() {
        // arrange
        AdapterManager manager = new AdapterManager(threadContext, storage, writer);

        // act
        manager.startTests();

        // assert
        verify(writer, times(1)).startLaunch();
    }

    @Test
    void stopTests_InvokeFinishLaunchHandler() {
        // arrange
        AdapterManager manager = new AdapterManager(threadContext, storage, writer);

        // act
        manager.stopTests();

        // assert
        verify(writer, times(1)).finishLaunch();
    }

    @Test
    void startMainContainer_InvokePutHandler() {
        // arrange
        MainContainer container = Helper.generateMainContainer();
        container.setStart(null);

        AdapterManager manager = new AdapterManager(threadContext, storage, writer);

        // act
        manager.startMainContainer(container);

        // assert
        Assertions.assertNotEquals(null, container.getStart());
        verify(storage, times(1)).put(container.getUuid(), container);
    }

    @Test
    void stopMainContainer_WithContainer_InvokeWriteTestsHandler() {
        // arrange
        MainContainer container = Helper.generateMainContainer();
        String uuid = container.getUuid();
        container.setStop(null);

        when(storage.getTestsContainer(uuid)).thenReturn(Optional.of(container));

        AdapterManager manager = new AdapterManager(threadContext, storage, writer);

        // act
        manager.stopMainContainer(uuid);

        // assert
        Assertions.assertNotNull(container.getStop());
        verify(writer, times(1)).writeTests(container);
    }

    @Test
    void stopMainContainer_WithoutContainer_NoInvokeWriteTestsHandler() {
        // arrange
        MainContainer container = Helper.generateMainContainer();
        String uuid = container.getUuid();
        container.setStop(null);

        when(storage.getTestsContainer(uuid)).thenReturn(Optional.empty());

        AdapterManager manager = new AdapterManager(threadContext, storage, writer);

        // act
        manager.stopMainContainer(container.getUuid());

        // assert
        Assertions.assertNull(container.getStop());
        verify(writer, never()).writeTests(container);
    }

    @Test
    void startClassContainer_WithTestContainer_InvokeGetTestsContainerHandler() {
        // arrange
        MainContainer testContainer = Helper.generateMainContainer();
        testContainer.setChildren(new ArrayList<>());
        ClassContainer classContainer = Helper.generateClassContainer();
        classContainer.setStart(null);
        String parentUuid = testContainer.getUuid();

        when(storage.getTestsContainer(parentUuid)).thenReturn(Optional.of(testContainer));

        AdapterManager manager = new AdapterManager(threadContext, storage, writer);

        // act
        manager.startClassContainer(parentUuid, classContainer);

        // assert
        Assertions.assertFalse(testContainer.getChildren().isEmpty());
        Assertions.assertNotNull(classContainer.getStart());
        verify(storage, times(1)).put(classContainer.getUuid(), classContainer);
    }

    @Test
    void startClassContainer_WithoutTestContainer_InvokeGetTestsContainerHandler() {
        // arrange
        MainContainer testContainer = Helper.generateMainContainer();
        testContainer.setChildren(new ArrayList<>());
        ClassContainer classContainer = Helper.generateClassContainer();
        classContainer.setStart(null);
        String parentUuid = testContainer.getUuid();

        when(storage.getTestsContainer(parentUuid)).thenReturn(Optional.empty());

        AdapterManager manager = new AdapterManager(threadContext, storage, writer);

        // act
        manager.startClassContainer(parentUuid, classContainer);

        // assert
        Assertions.assertTrue(testContainer.getChildren().isEmpty());
        Assertions.assertNotNull(classContainer.getStart());
        verify(storage, times(1)).put(classContainer.getUuid(), classContainer);
    }

    @Test
    void stopClassContainer_WithContainer_InvokeWriteTestsHandler() {
        // arrange
        ClassContainer container = Helper.generateClassContainer();
        container.setStop(null);
        String uuid = container.getUuid();

        when(storage.getClassContainer(uuid)).thenReturn(Optional.of(container));

        AdapterManager manager = new AdapterManager(threadContext, storage, writer);

        // act
        manager.stopClassContainer(uuid);

        // assert
        Assertions.assertNotNull(container.getStop());
        verify(writer, times(1)).writeClass(container);
    }

    @Test
    void stopClassContainer_WithoutContainer_NoInvokeWriteTestsHandler() {
        // arrange
        ClassContainer container = Helper.generateClassContainer();
        container.setStop(null);
        String uuid = container.getUuid();

        when(storage.getClassContainer(uuid)).thenReturn(Optional.empty());

        AdapterManager manager = new AdapterManager(threadContext, storage, writer);

        // act
        manager.stopClassContainer(uuid);

        // assert
        Assertions.assertNull(container.getStop());
        verify(writer, never()).writeClass(container);
    }

    @Test
    void updateClassContainer_WithContainer_InvokeUpdateHandler() {
        // arrange
        ClassContainer container = Helper.generateClassContainer();
        String uuid = container.getUuid();

        when(storage.getClassContainer(uuid)).thenReturn(Optional.of(container));

        AdapterManager manager = new AdapterManager(threadContext, storage, writer);

        // act
        manager.updateClassContainer(uuid, update);

        // assert
        verify(update, times(1)).accept(container);
    }

    @Test
    void updateClassContainer_WithoutContainer_NoInvokeUpdateHandler() {
        // arrange
        ClassContainer container = Helper.generateClassContainer();
        String uuid = container.getUuid();

        when(storage.getClassContainer(uuid)).thenReturn(Optional.empty());

        AdapterManager manager = new AdapterManager(threadContext, storage, writer);

        // act
        manager.updateClassContainer(uuid, update);

        // assert
        verify(update, never()).accept(container);
    }

    @Test
    void startTestCase_WithTestResult_InvokeStartHandler() {
        // arrange
        TestResult result = Helper.generateTestResult();
        result.setStart(null);
        String uuid = result.getUuid();

        when(storage.getTestResult(uuid)).thenReturn(Optional.of(result));

        AdapterManager manager = new AdapterManager(threadContext, storage, writer);

        // act
        manager.startTestCase(uuid);

        // assert
        Assertions.assertNotNull(result.getStart());
        Assertions.assertEquals(ItemStage.RUNNING, result.getItemStage());
        verify(threadContext, times(1)).clear();
        verify(threadContext, times(1)).start(uuid);
    }

    @Test
    void startTestCase_WithoutTestResult_NoInvokeStartHandler() {
        // arrange
        TestResult result = Helper.generateTestResult();
        result.setStart(null);
        String uuid = result.getUuid();

        when(storage.getTestResult(uuid)).thenReturn(Optional.empty());

        AdapterManager manager = new AdapterManager(threadContext, storage, writer);

        // act
        manager.startTestCase(uuid);

        // assert
        Assertions.assertNull(result.getStart());
        Assertions.assertNull(result.getItemStage());
        verify(threadContext, times(1)).clear();
        verify(threadContext, never()).start(uuid);
    }

    @Test
    void scheduleTestCase_InvokePutHandler() {
        // arrange
        TestResult result = Helper.generateTestResult();

        AdapterManager manager = new AdapterManager(threadContext, storage, writer);

        // act
        manager.scheduleTestCase(result);

        // assert
        Assertions.assertEquals(ItemStage.SCHEDULED, result.getItemStage());
        verify(storage, times(1)).put(result.getUuid(), result);
    }

    @Test
    void updateTestCase_WithRootWithTestResult_InvokeUpdateHandler() {
        // arrange
        TestResult result = Helper.generateTestResult();
        String uuid = result.getUuid();

        when(threadContext.getRoot()).thenReturn(Optional.of(uuid));
        when(storage.getTestResult(uuid)).thenReturn(Optional.of(result));

        AdapterManager manager = new AdapterManager(threadContext, storage, writer);

        // act
        manager.updateTestCase(update);

        // assert
        verify(update, times(1)).accept(result);
    }

    @Test
    void updateTestCase_WithoutRoot_InvokeUpdateHandler() {
        // arrange
        TestResult result = Helper.generateTestResult();

        when(threadContext.getRoot()).thenReturn(Optional.empty());

        AdapterManager manager = new AdapterManager(threadContext, storage, writer);

        // act
        manager.updateTestCase(update);

        // assert
        verify(update, never()).accept(result);
    }

    @Test
    void updateTestCase_WithRootWithoutTestResult_InvokeUpdateHandler() {
        // arrange
        TestResult result = Helper.generateTestResult();
        String uuid = result.getUuid();

        when(threadContext.getRoot()).thenReturn(Optional.of(uuid));
        when(storage.getTestResult(uuid)).thenReturn(Optional.empty());

        AdapterManager manager = new AdapterManager(threadContext, storage, writer);

        // act
        manager.updateTestCase(update);

        // assert
        verify(update, never()).accept(result);
    }

    @Test
    void stopTestCase_WithTestResult_InvokeWriteTestHandler() {
        // arrange
        TestResult result = Helper.generateTestResult();
        result.setStop(null);
        String uuid = result.getUuid();

        when(storage.getTestResult(uuid)).thenReturn(Optional.of(result));

        AdapterManager manager = new AdapterManager(threadContext, storage, writer);

        // act
        manager.stopTestCase(uuid);

        // assert
        Assertions.assertNotNull(result.getStop());
        Assertions.assertEquals(ItemStage.FINISHED, result.getItemStage());
        verify(threadContext, times(1)).clear();
        verify(writer, times(1)).writeTest(result);
    }

    @Test
    void stopTestCase_WithoutTestResult_NoInvokeWriteTestHandler() {
        // arrange
        TestResult result = Helper.generateTestResult();
        result.setStop(null);
        String uuid = result.getUuid();

        when(storage.getTestResult(uuid)).thenReturn(Optional.empty());

        AdapterManager manager = new AdapterManager(threadContext, storage, writer);

        // act
        manager.stopTestCase(uuid);

        // assert
        Assertions.assertNull(result.getStop());
        Assertions.assertNull(result.getItemStage());
        verify(threadContext, never()).clear();
        verify(writer, never()).writeTest(result);
    }

    @Test
    void startPrepareFixtureAll_InvokeStartHandler() {
        // arrange
        FixtureResult result = Helper.generateBeforeAllFixtureResult();
        result.setStart(null);
        MainContainer container = Helper.generateMainContainer();
        container.setBeforeMethods(new ArrayList<>());
        String parentUuid = container.getUuid();
        String uuid = UUID.randomUUID().toString();

        when(storage.getTestsContainer(parentUuid))
                .thenReturn(Optional.of(container));

        AdapterManager manager = new AdapterManager(threadContext, storage, writer);

        // act
        manager.startPrepareFixtureAll(parentUuid, uuid, result);

        // assert
        Assertions.assertNotNull(result.getStart());
        Assertions.assertEquals(ItemStage.RUNNING, result.getItemStage());
        Assertions.assertFalse(container.getBeforeMethods().isEmpty());
        verify(storage, times(1)).put(uuid, result);
        verify(threadContext, times(1)).clear();
        verify(threadContext, times(1)).start(uuid);
    }

    @Test
    void startTearDownFixtureAll_InvokeStartHandler() {
        // arrange
        FixtureResult result = Helper.generateAfterAllFixtureResult();
        result.setStart(null);
        MainContainer container = Helper.generateMainContainer();
        container.setAfterMethods(new ArrayList<>());
        String parentUuid = container.getUuid();
        String uuid = UUID.randomUUID().toString();

        when(storage.getTestsContainer(parentUuid))
                .thenReturn(Optional.of(container));

        AdapterManager manager = new AdapterManager(threadContext, storage, writer);

        // act
        manager.startTearDownFixtureAll(parentUuid, uuid, result);

        // assert
        Assertions.assertNotNull(result.getStart());
        Assertions.assertEquals(ItemStage.RUNNING, result.getItemStage());
        Assertions.assertFalse(container.getAfterMethods().isEmpty());
        verify(storage, times(1)).put(uuid, result);
        verify(threadContext, times(1)).clear();
        verify(threadContext, times(1)).start(uuid);
    }

    @Test
    void startPrepareFixture_InvokeStartHandler() {
        // arrange
        FixtureResult result = Helper.generateBeforeEachFixtureResult();
        result.setStart(null);
        ClassContainer container = Helper.generateClassContainer();
        container.setBeforeClassMethods(new ArrayList<>());
        String parentUuid = container.getUuid();
        String uuid = UUID.randomUUID().toString();

        when(storage.getClassContainer(parentUuid))
                .thenReturn(Optional.of(container));

        AdapterManager manager = new AdapterManager(threadContext, storage, writer);

        // act
        manager.startPrepareFixture(parentUuid, uuid, result);

        // assert
        Assertions.assertNotNull(result.getStart());
        Assertions.assertEquals(ItemStage.RUNNING, result.getItemStage());
        Assertions.assertFalse(container.getBeforeClassMethods().isEmpty());
        verify(storage, times(1)).put(uuid, result);
        verify(threadContext, times(1)).clear();
        verify(threadContext, times(1)).start(uuid);
    }

    @Test
    void startTearDownFixture_InvokeStartHandler() {
        // arrange
        FixtureResult result = Helper.generateAfterEachFixtureResult();
        result.setStart(null);
        ClassContainer container = Helper.generateClassContainer();
        container.setAfterClassMethods(new ArrayList<>());
        String parentUuid = container.getUuid();
        String uuid = UUID.randomUUID().toString();

        when(storage.getClassContainer(parentUuid))
                .thenReturn(Optional.of(container));

        AdapterManager manager = new AdapterManager(threadContext, storage, writer);

        // act
        manager.startTearDownFixture(parentUuid, uuid, result);

        // assert
        Assertions.assertNotNull(result.getStart());
        Assertions.assertEquals(ItemStage.RUNNING, result.getItemStage());
        Assertions.assertFalse(container.getAfterClassMethods().isEmpty());
        verify(storage, times(1)).put(uuid, result);
        verify(threadContext, times(1)).clear();
        verify(threadContext, times(1)).start(uuid);
    }

    @Test
    void startPrepareFixtureEachTest_InvokeStartHandler() {
        // arrange
        FixtureResult result = Helper.generateBeforeEachFixtureResult();
        result.setStart(null);
        ClassContainer container = Helper.generateClassContainer();
        container.setBeforeEachTest(new ArrayList<>());
        String parentUuid = container.getUuid();
        String uuid = UUID.randomUUID().toString();

        when(storage.getClassContainer(parentUuid))
                .thenReturn(Optional.of(container));

        AdapterManager manager = new AdapterManager(threadContext, storage, writer);

        // act
        manager.startPrepareFixtureEachTest(parentUuid, uuid, result);

        // assert
        Assertions.assertNotNull(result.getStart());
        Assertions.assertEquals(ItemStage.RUNNING, result.getItemStage());
        Assertions.assertFalse(container.getBeforeEachTest().isEmpty());
        verify(storage, times(1)).put(uuid, result);
        verify(threadContext, times(1)).clear();
        verify(threadContext, times(1)).start(uuid);
    }

    @Test
    void startTearDownFixtureEachTest_InvokeStartHandler() {
        // arrange
        FixtureResult result = Helper.generateAfterEachFixtureResult();
        result.setStart(null);
        ClassContainer container = Helper.generateClassContainer();
        container.setAfterEachTest(new ArrayList<>());
        String parentUuid = container.getUuid();
        String uuid = UUID.randomUUID().toString();

        when(storage.getClassContainer(parentUuid))
                .thenReturn(Optional.of(container));

        AdapterManager manager = new AdapterManager(threadContext, storage, writer);

        // act
        manager.startTearDownFixtureEachTest(parentUuid, uuid, result);

        // assert
        Assertions.assertNotNull(result.getStart());
        Assertions.assertEquals(ItemStage.RUNNING, result.getItemStage());
        Assertions.assertFalse(container.getAfterEachTest().isEmpty());
        verify(storage, times(1)).put(uuid, result);
        verify(threadContext, times(1)).clear();
        verify(threadContext, times(1)).start(uuid);
    }

    @Test
    void updateFixture_WithFixture_InvokeUpdateHandler() {
        // arrange
        FixtureResult result = Helper.generateBeforeEachFixtureResult();
        String uuid = Helper.generateTestResult().getUuid();

        when(storage.getFixture(uuid)).thenReturn(Optional.of(result));

        AdapterManager manager = new AdapterManager(threadContext, storage, writer);

        // act
        manager.updateFixture(uuid, update);

        // assert
        verify(update, times(1)).accept(result);
    }

    @Test
    void updateFixture_WithoutFixture_NoInvokeUpdateHandler() {
        // arrange
        FixtureResult result = Helper.generateBeforeEachFixtureResult();
        String uuid = Helper.generateTestResult().getUuid();

        when(storage.getFixture(uuid)).thenReturn(Optional.empty());

        AdapterManager manager = new AdapterManager(threadContext, storage, writer);

        // act
        manager.updateFixture(uuid, update);

        // assert
        verify(update, never()).accept(result);
    }

    @Test
    void stopFixture_WithFixture_InvokeClearHandler() {
        // arrange
        FixtureResult result = Helper.generateBeforeEachFixtureResult();
        result.setStop(null);
        String uuid = Helper.generateTestResult().getUuid();

        when(storage.getFixture(uuid)).thenReturn(Optional.of(result));

        AdapterManager manager = new AdapterManager(threadContext, storage, writer);

        // act
        manager.stopFixture(uuid);

        // assert
        Assertions.assertNotNull(result.getStop());
        Assertions.assertEquals(ItemStage.FINISHED, result.getItemStage());
        verify(threadContext, times(1)).clear();
    }

    @Test
    void stopFixture_WithoutFixture_NoInvokeClearHandler() {
        // arrange
        FixtureResult result = Helper.generateBeforeEachFixtureResult();
        result.setStop(null);
        String uuid = Helper.generateTestResult().getUuid();

        when(storage.getFixture(uuid)).thenReturn(Optional.empty());

        AdapterManager manager = new AdapterManager(threadContext, storage, writer);

        // act
        manager.stopFixture(uuid);

        // assert
        Assertions.assertNull(result.getStop());
        Assertions.assertNull(result.getItemStage());
        verify(threadContext, never()).clear();
    }

    @Test
    void startStep_WithCurrent_InvokeStartHandler() {
        // arrange
        StepResult result = Helper.generateStepResult();
        result.setStart(null);
        String uuid = UUID.randomUUID().toString();
        String parentUuid = UUID.randomUUID().toString();

        when(threadContext.getCurrent()).thenReturn(Optional.of(parentUuid));
        when(storage.get(parentUuid, ResultWithSteps.class)).thenReturn(Optional.of(result));

        AdapterManager manager = new AdapterManager(threadContext, storage, writer);

        // act
        manager.startStep(uuid, result);

        // assert
        Assertions.assertNotNull(result.getStart());
        Assertions.assertEquals(ItemStage.RUNNING, result.getItemStage());
        Assertions.assertFalse(result.getSteps().isEmpty());
        verify(threadContext, times(1)).start(uuid);
        verify(storage, times(1)).put(uuid, result);
    }

    @Test
    void startStep_WithoutCurrent_NoInvokeStartHandler() {
        // arrange
        StepResult result = Helper.generateStepResult();
        result.setStart(null);
        String uuid = UUID.randomUUID().toString();

        when(threadContext.getCurrent()).thenReturn(Optional.empty());

        AdapterManager manager = new AdapterManager(threadContext, storage, writer);

        // act
        manager.startStep(uuid, result);

        // assert
        Assertions.assertNull(result.getStart());
        Assertions.assertNull(result.getItemStage());
        Assertions.assertTrue(result.getSteps().isEmpty());
        verify(threadContext, never()).start(uuid);
        verify(storage, never()).put(uuid, result);
    }

    @Test
    void updateStep_WithCurrentWithStep_InvokeUpdateHandler() {
        // arrange
        StepResult result = Helper.generateStepResult();
        String uuid = UUID.randomUUID().toString();

        when(threadContext.getCurrent()).thenReturn(Optional.of(uuid));
        when(storage.getStep(uuid)).thenReturn(Optional.of(result));

        AdapterManager manager = new AdapterManager(threadContext, storage, writer);

        // act
        manager.updateStep(update);

        // assert
        verify(update, times(1)).accept(result);
    }

    @Test
    void updateStep_WithoutCurrent_NoInvokeUpdateHandler() {
        // arrange
        StepResult result = Helper.generateStepResult();

        when(threadContext.getCurrent()).thenReturn(Optional.empty());

        AdapterManager manager = new AdapterManager(threadContext, storage, writer);

        // act
        manager.updateStep(update);

        // assert
        verify(update, never()).accept(result);
    }

    @Test
    void updateStep_WithCurrentWithoutStep_InvokeUpdateHandler() {
        // arrange
        StepResult result = Helper.generateStepResult();
        String uuid = UUID.randomUUID().toString();

        when(threadContext.getCurrent()).thenReturn(Optional.of(uuid));
        when(storage.getStep(uuid)).thenReturn(Optional.empty());

        AdapterManager manager = new AdapterManager(threadContext, storage, writer);

        // act
        manager.updateStep(update);

        // assert
        verify(update, never()).accept(result);
    }

    @Test
    void stopStep_WithRoot_InvokeStopHandler() {
        // arrange
        StepResult result = Helper.generateStepResult();
        result.setStop(null);
        String root = UUID.randomUUID().toString();
        String current = UUID.randomUUID().toString();

        when(threadContext.getRoot()).thenReturn(Optional.of(root));
        when(threadContext.getCurrent()).thenReturn(Optional.of(current));
        when(storage.getStep(current)).thenReturn(Optional.of(result));

        AdapterManager manager = new AdapterManager(threadContext, storage, writer);

        // act
        manager.stopStep();

        // assert
        Assertions.assertNotNull(result.getStop());
        Assertions.assertEquals(ItemStage.FINISHED, result.getItemStage());
        verify(threadContext, times(1)).stop();
    }

    @Test
    void stopStep_WithRootEqualsCurrent_NoInvokeStopHandler() {
        // arrange
        StepResult result = Helper.generateStepResult();
        result.setStop(null);
        String root = UUID.randomUUID().toString();

        when(threadContext.getRoot()).thenReturn(Optional.of(root));
        when(threadContext.getCurrent()).thenReturn(Optional.of(root));

        AdapterManager manager = new AdapterManager(threadContext, storage, writer);

        // act
        manager.stopStep();

        // assert
        Assertions.assertNull(result.getStop());
        Assertions.assertNull(result.getItemStage());
        verify(threadContext, never()).stop();
    }

    @Test
    void stopStep_WithoutRootWithCurrent_NoInvokeStopHandler() {
        // arrange
        StepResult result = Helper.generateStepResult();
        result.setStop(null);
        String current = UUID.randomUUID().toString();

        when(threadContext.getRoot()).thenReturn(Optional.empty());
        when(threadContext.getCurrent()).thenReturn(Optional.of(current));

        AdapterManager manager = new AdapterManager(threadContext, storage, writer);

        // act
        manager.stopStep();

        // assert
        Assertions.assertNull(result.getStop());
        Assertions.assertNull(result.getItemStage());
        verify(threadContext, never()).stop();
    }

    @Test
    void stopStep_WithRootWithoutCurrent_NoInvokeStopHandler() {
        // arrange
        StepResult result = Helper.generateStepResult();
        result.setStop(null);
        String root = UUID.randomUUID().toString();

        when(threadContext.getRoot()).thenReturn(Optional.of(root));
        when(threadContext.getCurrent()).thenReturn(Optional.empty());

        AdapterManager manager = new AdapterManager(threadContext, storage, writer);

        // act
        manager.stopStep();

        // assert
        Assertions.assertNull(result.getStop());
        Assertions.assertNull(result.getItemStage());
        verify(threadContext, never()).stop();
    }

    @Test
    void stopStep_WithoutStepResult_NoInvokeStopHandler() {
        // arrange
        StepResult result = Helper.generateStepResult();
        result.setStop(null);
        String root = UUID.randomUUID().toString();

        when(threadContext.getRoot()).thenReturn(Optional.of(root));
        when(threadContext.getCurrent()).thenReturn(Optional.of(root));
        when(storage.getStep(root)).thenReturn(Optional.empty());

        AdapterManager manager = new AdapterManager(threadContext, storage, writer);

        // act
        manager.stopStep();

        // assert
        Assertions.assertNull(result.getStop());
        Assertions.assertNull(result.getItemStage());
        verify(threadContext, never()).stop();
    }
}
