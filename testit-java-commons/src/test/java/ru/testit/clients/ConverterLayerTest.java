package ru.testit.clients;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.testit.Helper;
import ru.testit.adaptersapi.model.AutoTestApiResult;
import ru.testit.adaptersapi.model.AutoTestCreateApiModel;
import ru.testit.adaptersapi.model.AutoTestUpdateApiModel;
import ru.testit.adaptersapi.model.LayerSource;
import ru.testit.annotations.Layer;
import ru.testit.models.ItemStatus;
import ru.testit.models.TestLayers;
import ru.testit.models.TestResult;
import ru.testit.services.Utils;

import java.lang.reflect.Method;
import java.util.Collections;

class ConverterLayerTest {

    private static final String PROJECT_ID = "d7defd1e-c1ed-400d-8be8-091ebfdda744";

    @Test
    void createModel_setsLayerWhenPresent() {
        TestResult result = Helper.generateTestResult().setLayer(TestLayers.API);

        AutoTestCreateApiModel model = Converter.testResultToAutoTestCreateApiModel(result);

        Assertions.assertNotNull(model.getLayer());
        Assertions.assertEquals(TestLayers.API, model.getLayer().getName());
        Assertions.assertEquals(LayerSource.RUN, model.getLayer().getSource());
    }

    @Test
    void createModel_omitsLayerWhenAbsent() {
        TestResult result = Helper.generateTestResult();

        AutoTestCreateApiModel model = Converter.testResultToAutoTestCreateApiModel(result);

        Assertions.assertNull(model.getLayer());
    }

    @Test
    void createModel_omitsBlankLayer() {
        TestResult result = Helper.generateTestResult().setLayer("   ");

        AutoTestCreateApiModel model = Converter.testResultToAutoTestCreateApiModel(result);

        Assertions.assertNull(model.getLayer());
    }

    @Test
    void updateModel_setsLayerAndResetLayerWhenPresent() {
        TestResult result = Helper.generateTestResult().setLayer("my-custom");

        AutoTestUpdateApiModel model = Converter.testResultToAutoTestUpdateApiModel(result);

        Assertions.assertNotNull(model.getLayer());
        Assertions.assertEquals("my-custom", model.getLayer().getName());
        Assertions.assertEquals(LayerSource.RUN, model.getLayer().getSource());
        Assertions.assertFalse(model.getResetLayer());
    }

    @Test
    void updateModel_omitsLayerWhenAbsent() {
        TestResult result = Helper.generateTestResult();

        AutoTestUpdateApiModel model = Converter.testResultToAutoTestUpdateApiModel(result);

        Assertions.assertNull(model.getLayer());
        Assertions.assertFalse(model.getResetLayer());
    }

    @Test
    void updateModel_omitsBlankLayer() {
        TestResult result = Helper.generateTestResult().setLayer("  \t  ");

        AutoTestUpdateApiModel model = Converter.testResultToAutoTestUpdateApiModel(result);

        Assertions.assertNull(model.getLayer());
        Assertions.assertFalse(model.getResetLayer());
    }

    @Test
    void prepareToCreateAutoTest_appliesLayer() throws Exception {
        TestResult result = Helper.generateTestResult().setLayer(TestLayers.COMPONENT);

        AutoTestCreateApiModel model = Converter.prepareToCreateAutoTest(result, PROJECT_ID);

        Assertions.assertEquals(TestLayers.COMPONENT, model.getLayer().getName());
        Assertions.assertEquals(LayerSource.RUN, model.getLayer().getSource());
    }

    @Test
    void prepareToUpdate_passedTest_appliesLayer() throws Exception {
        TestResult result = Helper.generateTestResult().setLayer(TestLayers.E2E);
        result.setItemStatus(ItemStatus.PASSED);
        AutoTestApiResult autotest = Helper.generateAutoTestApiResult(PROJECT_ID);

        AutoTestUpdateApiModel model = Converter.prepareToUpdateAutoTest(result, autotest, PROJECT_ID);

        Assertions.assertEquals(TestLayers.E2E, model.getLayer().getName());
        Assertions.assertEquals(LayerSource.RUN, model.getLayer().getSource());
        Assertions.assertFalse(model.getResetLayer());
    }

    @Test
    void prepareToUpdate_failedTest_appliesLayerFromTestResult() throws Exception {
        TestResult result = Helper.generateTestResult().setLayer(TestLayers.UI);
        result.setItemStatus(ItemStatus.FAILED);
        AutoTestApiResult autotest = Helper.generateAutoTestApiResult(PROJECT_ID);

        AutoTestUpdateApiModel model = Converter.prepareToUpdateAutoTest(result, autotest, PROJECT_ID);

        Assertions.assertEquals(TestLayers.UI, model.getLayer().getName());
        Assertions.assertEquals(LayerSource.RUN, model.getLayer().getSource());
        Assertions.assertFalse(model.getResetLayer());
    }

    @Test
    void prepareToUpdate_failedTest_omitsLayerWhenAbsent() throws Exception {
        TestResult result = Helper.generateTestResult();
        result.setItemStatus(ItemStatus.FAILED);
        AutoTestApiResult autotest = Helper.generateAutoTestApiResult(PROJECT_ID);

        AutoTestUpdateApiModel model = Converter.prepareToUpdateAutoTest(result, autotest, PROJECT_ID);

        Assertions.assertNull(model.getLayer());
        Assertions.assertFalse(model.getResetLayer());
    }

    @Test
    void extractLayer_readsAnnotationValue() throws NoSuchMethodException {
        class Sample {
            @Layer(TestLayers.INTEGRATION)
            void test() {}
        }

        Method method = Sample.class.getDeclaredMethod("test");

        Assertions.assertEquals(TestLayers.INTEGRATION, Utils.extractLayer(method, null));
    }

    @Test
    void extractLayer_substitutesParameters() throws NoSuchMethodException {
        class Sample {
            @Layer("Layer-{level}")
            void test() {}
        }

        Method method = Sample.class.getDeclaredMethod("test");

        Assertions.assertEquals(
                "Layer-API",
                Utils.extractLayer(method, Collections.singletonMap("level", "API")));
    }

    @Test
    void extractLayer_returnsNullWithoutAnnotation() throws NoSuchMethodException {
        class Sample {
            void test() {}
        }

        Method method = Sample.class.getDeclaredMethod("test");

        Assertions.assertNull(Utils.extractLayer(method, null));
    }

    @Test
    void extractLayer_returnsNullForBlankAnnotation() throws NoSuchMethodException {
        class Sample {
            @Layer("   ")
            void test() {}
        }

        Method method = Sample.class.getDeclaredMethod("test");

        Assertions.assertNull(Utils.extractLayer(method, null));
    }

    @Test
    void extractLayer_returnsNullForEmptyAnnotation() throws NoSuchMethodException {
        class Sample {
            @Layer("")
            void test() {}
        }

        Method method = Sample.class.getDeclaredMethod("test");

        Assertions.assertNull(Utils.extractLayer(method, null));
    }
}
