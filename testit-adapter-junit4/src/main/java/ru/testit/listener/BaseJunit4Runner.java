package ru.testit.listener;

import org.junit.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.testit.services.Adapter;
import ru.testit.services.AdapterManager;

import java.util.List;

public class BaseJunit4Runner extends BlockJUnit4ClassRunner {
    private List<String> testsForRun;
    private final boolean isFilteredMode;

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseJunit4Runner.class);

    public BaseJunit4Runner(Class<?> clazz) throws InitializationError {
        super(clazz);
        AdapterManager manager = Adapter.getAdapterManager();
        isFilteredMode = manager.isFilteredMode();
        if (isFilteredMode) {
            testsForRun = manager.getTestFromTestRun();
        }
    }

    @Override
    public void run(RunNotifier notifier) {
        notifier.addListener(new BaseJunit4Listener());
        EachTestNotifier testNotifier = new EachTestNotifier(
                notifier,
                getDescription()
        );
        try {
            notifier.fireTestRunStarted(getDescription());
            Statement statement = classBlock(notifier);
            statement.evaluate();
        } catch (AssumptionViolatedException e) {
            testNotifier.fireTestIgnored();
        } catch (StoppedByUserException e) {
            throw e;
        } catch (Throwable e) {
            testNotifier.addFailure(e);
        } finally {
            notifier.fireTestRunFinished(new Result());
        }
    }

    @Override
    protected void runChild(final FrameworkMethod method, RunNotifier notifier) {
        Description description = describeChild(method);

        if (super.isIgnored(method)) {
            notifier.fireTestIgnored(description);
            return;
        }

        if (!isFilteredMode) {
            runLeaf(super.methodBlock(method), description, notifier);
            return;
        }

        String externalId = Utils.extractExternalID(description);

        if (testsForRun.contains(externalId)) {
            runLeaf(methodBlock(method), description, notifier);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Test {} include for run", externalId);
            }

            return;
        }

        notifier.fireTestIgnored(description);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Test {} exclude for run", externalId);
        }
    }
}
