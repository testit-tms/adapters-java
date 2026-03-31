package ru.testit.services.core;

import ru.testit.models.ClassContainer;
import ru.testit.models.FixtureResult;
import ru.testit.models.MainContainer;
import ru.testit.models.StepResult;
import ru.testit.models.TestResult;

import java.util.function.Consumer;

public class AdapterCoreFacade {

    private final AdapterContainerHelper containerHelper;
    private final AdapterTestCaseHelper testCaseHelper;
    private final AdapterFixtureHelper fixtureHelper;
    private final AdapterStepHelper stepHelper;

    public AdapterCoreFacade(
            AdapterContainerHelper containerHelper,
            AdapterTestCaseHelper testCaseHelper,
            AdapterFixtureHelper fixtureHelper,
            AdapterStepHelper stepHelper
    ) {
        this.containerHelper = containerHelper;
        this.testCaseHelper = testCaseHelper;
        this.fixtureHelper = fixtureHelper;
        this.stepHelper = stepHelper;
    }

    public void startMainContainer(final MainContainer container) {
        containerHelper.startMainContainer(container);
    }

    public void stopMainContainer(final String uuid) {
        containerHelper.stopMainContainer(uuid);
    }

    public void startClassContainer(
            final String parentUuid,
            final ClassContainer container
    ) {
        containerHelper.startClassContainer(parentUuid, container);
    }

    public void stopClassContainer(final String uuid) {
        containerHelper.stopClassContainer(uuid);
    }

    public void updateClassContainer(
            final String uuid,
            final Consumer<ClassContainer> update
    ) {
        containerHelper.updateClassContainer(uuid, update);
    }

    public void startTestCase(final String uuid) {
        testCaseHelper.startTestCase(uuid);
    }

    public void scheduleTestCase(final TestResult result) {
        testCaseHelper.scheduleTestCase(result);
    }

    public void updateTestCase(final Consumer<TestResult> update) {
        testCaseHelper.updateTestCase(update);
    }

    public void updateTestCase(
            final String uuid,
            final Consumer<TestResult> update
    ) {
        testCaseHelper.updateTestCase(uuid, update);
    }

    public void stopTestCase(final String uuid) {
        testCaseHelper.stopTestCase(uuid);
    }

    public void startPrepareFixtureAll(
            final String parentUuid,
            final String uuid,
            final FixtureResult result
    ) {
        fixtureHelper.startPrepareFixtureAll(parentUuid, uuid, result);
    }

    public void startTearDownFixtureAll(
            final String parentUuid,
            final String uuid,
            final FixtureResult result
    ) {
        fixtureHelper.startTearDownFixtureAll(parentUuid, uuid, result);
    }

    public void startPrepareFixture(
            final String parentUuid,
            final String uuid,
            final FixtureResult result
    ) {
        fixtureHelper.startPrepareFixture(parentUuid, uuid, result);
    }

    public void startTearDownFixture(
            final String parentUuid,
            final String uuid,
            final FixtureResult result
    ) {
        fixtureHelper.startTearDownFixture(parentUuid, uuid, result);
    }

    public void startPrepareFixtureEachTest(
            final String parentUuid,
            final String uuid,
            final FixtureResult result
    ) {
        fixtureHelper.startPrepareFixtureEachTest(parentUuid, uuid, result);
    }

    public void startTearDownFixtureEachTest(
            final String parentUuid,
            final String uuid,
            final FixtureResult result
    ) {
        fixtureHelper.startTearDownFixtureEachTest(parentUuid, uuid, result);
    }

    public void updateFixture(
            final String uuid,
            final Consumer<FixtureResult> update
    ) {
        fixtureHelper.updateFixture(uuid, update);
    }

    public void stopFixture(final String uuid) {
        fixtureHelper.stopFixture(uuid);
    }

    public void startStep(final String uuid, final StepResult result) {
        stepHelper.startStep(uuid, result);
    }

    public void startStep(
            final String parentUuid,
            final String uuid,
            final StepResult result
    ) {
        stepHelper.startStep(parentUuid, uuid, result);
    }

    public void updateStep(final Consumer<StepResult> update) {
        stepHelper.updateStep(update);
    }

    public void updateStep(
            final String uuid,
            final Consumer<StepResult> update
    ) {
        stepHelper.updateStep(uuid, update);
    }

    public void stopStep() {
        stepHelper.stopStep();
    }

    public void stopStep(final String uuid) {
        stepHelper.stopStep(uuid);
    }
}
