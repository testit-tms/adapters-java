package ru.testit.listener;

import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.PostDiscoveryFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.testit.services.Adapter;
import ru.testit.services.AdapterManager;
import ru.testit.services.Utils;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Junit5PostDiscoveryFilter implements PostDiscoveryFilter {
    private List<String> testsForRun;
    private final boolean isFilteredMode;

    private static final Logger LOGGER = LoggerFactory.getLogger(Junit5PostDiscoveryFilter.class);


    public Junit5PostDiscoveryFilter() {
        AdapterManager manager = Adapter.getAdapterManager();
        isFilteredMode = manager.isFilteredMode();
        if (isFilteredMode) {
            testsForRun = manager.getTestFromTestRun();
        }
    }

    @Override
    public FilterResult apply(TestDescriptor object) {
        if (!isFilteredMode) {
            return FilterResult.included("Adapter mode isn't filtered");
        }

        if (!object.getChildren().isEmpty()) {
            return FilterResult.included("filter only applied for tests");
        }

        final Optional<TestSource> testSource = object.getSource();
        if (testSource.isPresent()) {
            final MethodSource methodSource = (MethodSource) testSource.get();

            String externalId = Utils.extractExternalID(methodSource.getJavaMethod(), null);

            if (externalId.matches("\\{.*}")) {
                return filterTestWithParameters(externalId);
            }

            return filterSimpleTest(externalId);
        }

        return FilterResult.excluded("Incorrect type");
    }

    private FilterResult filterSimpleTest(String externalId) {
        if (testsForRun.contains(externalId)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Test {} include for run", externalId);
            }

            return FilterResult.includedIf(true);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Test {} exclude for run", externalId);
        }

        return FilterResult.excluded("test excluded");
    }

    private FilterResult filterTestWithParameters(String externalId) {
        Pattern pattern = Pattern.compile(externalId.replaceAll("\\{.*}", ".*"));

        for (String test : testsForRun) {
            Matcher matcher = pattern.matcher(test);
            if (matcher.find()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Test {} include for run", externalId);
                }
                return FilterResult.includedIf(true);
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Test {} exclude for run", externalId);
        }

        return FilterResult.excluded("test excluded");
    }
}
