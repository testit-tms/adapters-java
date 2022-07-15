package ru.testit.writers;

import ru.testit.client.model.*;
import ru.testit.models.*;
import ru.testit.models.LinkType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Helper {
    private final static String EXTERNAL_ID = "5819479d";
    private final static String TITLE = "Test title";
    private final static String DESCRIPTION = "Test description";
    private final static String NAME = "Test name";
    private final static String CLASS_NAME = "ClassName";
    private final static String SPACE_NAME = "SpaceName";
    private final static String WORK_ITEM_ID = "WorkItemId";
    private final static ItemStatus ITEM_STATUS = ItemStatus.PASSED;
    private final static String TEST_UUID = "99d77db9-8d68-4835-9e17-3a6333f01251";

    private final static String LINK_TITLE = "Link title";
    private final static String LINK_DESCRIPTION = "Link description";
    private final static LinkType LINK_TYPE = LinkType.ISSUE;
    private final static String LINK_URL = "https://example.test/";

    private final static String STEP_UUID = "d7068da4-3dfc-41a9-a667-27d74902083b";
    private final static String STEP_TITLE = "Step title";
    private final static String STEP_DESCRIPTION = "Step description";

    private final static String LABEL_NAME = "Label name";

    private final static String CLASS_UUID = "179f193b-2519-4ae9-a364-173c3d8fa6cd";

    private final static String BEFORE_EACH_UUID = "da59461a-c851-43f5-8aac-71a973a4c634";
    private final static String BEFORE_EACH_NAME = "Before Each name";
    private final static String BEFORE_EACH_DESCRIPTION = "Before Each description";

    private final static String AFTER_EACH_UUID = "3f152e15-6949-41b9-aeb8-24a10e3837a3";
    private final static String AFTER_EACH_NAME = "After Each name";
    private final static String AFTER_EACH_DESCRIPTION = "After Each description";

    private final static String BEFORE_ALL_UUID = "f13359e3-2106-481b-8c24-928de63d4d63";
    private final static String BEFORE_ALL_NAME = "Before All name";
    private final static String BEFORE_ALL_DESCRIPTION = "Before All description";

    private final static String AFTER_ALL_UUID = "454aad8f-c862-4f37-824a-1ef80908f9ea";
    private final static String AFTER_ALL_NAME = "After All name";
    private final static String AFTER_ALL_DESCRIPTION = "After All description";

    public static TestResult generateTestResult() {
        Date startDate = new Date();
        Date stopDate = new Date(startDate.getTime() + 1000);

        List<LinkItem> links = new ArrayList<>();
        links.add(new LinkItem().setTitle(LINK_TITLE)
                .setDescription(LINK_DESCRIPTION)
                .setType(LINK_TYPE)
                .setUrl(LINK_URL));

        List<String> steps = new ArrayList<>();
        steps.add(STEP_UUID);

        List<Label> labels = new ArrayList<>();
        Label label = new Label();
        label.setName(LABEL_NAME);
        labels.add(label);

        TestResult testResult = new TestResult();
        testResult.setExternalId(EXTERNAL_ID)
                .setUuid(TEST_UUID)
                .setTitle(TITLE)
                .setDescription(DESCRIPTION)
                .setClassName(CLASS_NAME)
                .setName(NAME)
                .setSpaceName(SPACE_NAME)
                .setStart(startDate.getTime())
                .setStop(stopDate.getTime())
                .setWorkItemId(WORK_ITEM_ID)
                .setItemStatus(ITEM_STATUS)
                .setLinkItems(links)
                .setSteps(steps)
                .setLabels(labels);

        return testResult;
    }

    public static AutoTestModel generateAutoTestModel(String projectId) {
        AutoTestModel model = new AutoTestModel();

        model.setName(NAME);
        model.setTitle(TITLE);
        model.setDescription(DESCRIPTION);
        model.setClassname(CLASS_NAME);
        model.setExternalId(EXTERNAL_ID);
        model.setNamespace(SPACE_NAME);
        model.setProjectId(UUID.fromString(projectId));
        model.setSteps(generateSteps());
        model.setLinks(generatePutLinks());
        model.setLabels(generateShortLabels());
        model.setSetup(new ArrayList<>());
        model.setTeardown(new ArrayList<>());
        model.setId(UUID.fromString(TEST_UUID));

        return model;
    }

    public static AutoTestPutModel generateAutoTestPutModel(String projectId, boolean convertFromPostModel) {
        AutoTestPutModel model = new AutoTestPutModel();

        model.setTitle(TITLE);
        model.setExternalId(EXTERNAL_ID);
        model.setName(NAME);
        model.setDescription(DESCRIPTION);
        model.setClassname(CLASS_NAME);
        model.setNamespace(SPACE_NAME);
        model.setSteps(generateSteps());
        model.setLinks(generatePutLinks());
        model.setLabels(generatePostLabels());
        model.setProjectId(UUID.fromString(projectId));
        model.setSetup(new ArrayList<>());
        model.setTeardown(new ArrayList<>());
        if (convertFromPostModel) {
            model.setId(UUID.fromString(TEST_UUID));
        }

        return model;
    }

    public static AutoTestPostModel generateAutoTestPostModel(String projectId) {
        AutoTestPostModel model = new AutoTestPostModel();

        model.setTitle(TITLE);
        model.setExternalId(EXTERNAL_ID);
        model.setName(NAME);
        model.setDescription(DESCRIPTION);
        model.setClassname(CLASS_NAME);
        model.setNamespace(SPACE_NAME);
        model.setSteps(generateSteps());
        model.setLinks(generatePostLinks());
        model.setLabels(generatePostLabels());
        model.setProjectId(UUID.fromString(projectId));

        return model;
    }

    public static ClassContainer generateClassContainer() {
        ClassContainer container = new ClassContainer();

        container.setUuid(CLASS_UUID);
        container.getChildren().add(TEST_UUID);
        container.getBeforeEachTest().add(BEFORE_EACH_UUID);
        container.getAfterEachTest().add(AFTER_EACH_UUID);

        return container;
    }

    public static MainContainer generateMainContainer() {
        MainContainer container = new MainContainer();

        container.getChildren().add(CLASS_UUID);
        container.getBeforeMethods().add(BEFORE_ALL_UUID);
        container.getAfterMethods().add(AFTER_ALL_UUID);

        return container;
    }

    public static StepResult generateStepResult() {
        Date startDate = new Date();
        Date stopDate = new Date(startDate.getTime() + 500);

        StepResult result = new StepResult();

        result.setName(STEP_TITLE);
        result.setDescription(STEP_DESCRIPTION);
        result.setStart(startDate.getTime());
        result.setStop(stopDate.getTime());
        result.setSteps(new ArrayList<>());
        result.setItemStatus(ItemStatus.PASSED);

        return result;
    }

    public static FixtureResult generateBeforeEachFixtureResult() {
        Date startDate = new Date();
        Date stopDate = new Date(startDate.getTime() + 100);

        FixtureResult fixtureResult = new FixtureResult();

        fixtureResult.setName(BEFORE_EACH_NAME)
                .setParent(TEST_UUID)
                .setItemStatus(ItemStatus.PASSED)
                .setDescription(BEFORE_EACH_DESCRIPTION)
                .setStart(startDate.getTime())
                .setStop(stopDate.getTime());

        return fixtureResult;
    }

    public static FixtureResult generateAfterEachFixtureResult() {
        Date startDate = new Date();
        Date stopDate = new Date(startDate.getTime() + 100);

        FixtureResult fixtureResult = new FixtureResult();

        fixtureResult.setName(AFTER_EACH_NAME)
                .setParent(TEST_UUID)
                .setItemStatus(ItemStatus.PASSED)
                .setDescription(AFTER_EACH_DESCRIPTION)
                .setStart(startDate.getTime())
                .setStop(stopDate.getTime());

        return fixtureResult;
    }

    public static AutoTestStepModel generateBeforeEachSetup(){
        AutoTestStepModel model = new AutoTestStepModel();

        model.setTitle(BEFORE_EACH_NAME);
        model.setDescription(BEFORE_EACH_DESCRIPTION);
        model.setSteps(new ArrayList<>());

        return model;
    }

    public static AutoTestStepModel generateAfterEachSetup(){
        AutoTestStepModel model = new AutoTestStepModel();

        model.setTitle(AFTER_EACH_NAME);
        model.setDescription(AFTER_EACH_DESCRIPTION);
        model.setSteps(new ArrayList<>());

        return model;
    }

    public static FixtureResult generateBeforeAllFixtureResult() {
        Date startDate = new Date();
        Date stopDate = new Date(startDate.getTime() + 100);

        FixtureResult fixtureResult = new FixtureResult();

        fixtureResult.setName(BEFORE_ALL_NAME)
                .setParent(TEST_UUID)
                .setItemStatus(ItemStatus.PASSED)
                .setDescription(BEFORE_ALL_DESCRIPTION)
                .setStart(startDate.getTime())
                .setStop(stopDate.getTime());

        return fixtureResult;
    }

    public static FixtureResult generateAfterAllFixtureResult() {
        Date startDate = new Date();
        Date stopDate = new Date(startDate.getTime() + 100);

        FixtureResult fixtureResult = new FixtureResult();

        fixtureResult.setName(AFTER_ALL_NAME)
                .setParent(TEST_UUID)
                .setItemStatus(ItemStatus.PASSED)
                .setDescription(AFTER_ALL_DESCRIPTION)
                .setStart(startDate.getTime())
                .setStop(stopDate.getTime());

        return fixtureResult;
    }

    public static AutoTestStepModel generateBeforeAllSetup(){
        AutoTestStepModel model = new AutoTestStepModel();

        model.setTitle(BEFORE_ALL_NAME);
        model.setDescription(BEFORE_ALL_DESCRIPTION);
        model.setSteps(new ArrayList<>());

        return model;
    }

    public static AutoTestStepModel generateAfterAllSetup(){
        AutoTestStepModel model = new AutoTestStepModel();

        model.setTitle(AFTER_ALL_NAME);
        model.setDescription(AFTER_ALL_DESCRIPTION);
        model.setSteps(new ArrayList<>());

        return model;
    }


    private static List<LabelShortModel> generateShortLabels() {
        List<LabelShortModel> labels = new ArrayList<>();

        LabelShortModel label = new LabelShortModel();
        label.setName(LABEL_NAME);

        labels.add(label);

        return labels;
    }

    private static List<LabelPostModel> generatePostLabels() {
        List<LabelPostModel> labels = new ArrayList<>();

        LabelPostModel label = new LabelPostModel();
        label.setName(LABEL_NAME);

        labels.add(label);

        return labels;
    }

    private static List<LinkPutModel> generatePutLinks() {
        List<LinkPutModel> links = new ArrayList<>();

        LinkPutModel link = new LinkPutModel();
        link.setTitle(LINK_TITLE);
        link.setDescription(LINK_DESCRIPTION);
        link.setUrl(LINK_URL);
        link.setType(ru.testit.client.model.LinkType.fromValue(LINK_TYPE.getValue()));

        links.add(link);

        return links;
    }

    private static List<LinkPostModel> generatePostLinks() {
        List<LinkPostModel> links = new ArrayList<>();

        LinkPostModel link = new LinkPostModel();
        link.setTitle(LINK_TITLE);
        link.setDescription(LINK_DESCRIPTION);
        link.setUrl(LINK_URL);
        link.setType(ru.testit.client.model.LinkType.fromValue(LINK_TYPE.getValue()));

        links.add(link);

        return links;
    }

    private static List<AutoTestStepModel> generateSteps() {
        List<AutoTestStepModel> steps = new ArrayList<>();
        AutoTestStepModel step = new AutoTestStepModel();
        step.setTitle(STEP_TITLE);
        step.setDescription(STEP_DESCRIPTION);
        step.setSteps(new ArrayList<>());
        steps.add(step);
        return steps;
    }
}
