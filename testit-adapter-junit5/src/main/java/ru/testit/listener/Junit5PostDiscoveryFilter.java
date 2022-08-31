package ru.testit.listener;

import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.PostDiscoveryFilter;
import ru.testit.services.Adapter;
import ru.testit.services.AdapterManager;
import ru.testit.services.Utils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Junit5PostDiscoveryFilter implements PostDiscoveryFilter {
    private List<String> testsForRun;
    private final boolean isFilteredMode;

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

        final MethodSource source = (MethodSource) object.getSource().get();
        String externalId = Utils.extractExternalID(source.getJavaMethod(), null);

        Pattern pattern = Pattern.compile(externalId.replaceAll("\\{.*}", ".*"));

        for (String test : testsForRun) {
            Matcher matcher = pattern.matcher(test);
            if (matcher.find()) {
                return FilterResult.includedIf(true);
            }
        }

        return FilterResult.excluded("test excluded");
    }
}
