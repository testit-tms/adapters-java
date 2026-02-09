package ru.testit;

import ru.testit.client.model.*;
import ru.testit.models.Label;
import ru.testit.models.LinkType;
import ru.testit.models.*;
import ru.testit.models.StepResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Helper {
    private static final String EXTERNAL_ID = "5819479d";
    private static final String TITLE = "Test title";
    private static final String DESCRIPTION = "Test description";
    private static final String NAME = "Test name";
    private static final String CLASS_NAME = "ClassName";
    private static final String SPACE_NAME = "SpaceName";
    private static final String WORK_ITEM_ID = "6523";
    private static final ItemStatus ITEM_STATUS = ItemStatus.PASSED;
    private static final String TEST_UUID = "99d77db9-8d68-4835-9e17-3a6333f01251";

    private static final String LINK_TITLE = "Link title";
    private static final String LINK_DESCRIPTION = "Link description";
    private static final LinkType LINK_TYPE = LinkType.ISSUE;
    private static final String LINK_URL = "https://example.test/";

    private static final String STEP_TITLE = "Step title";
    private static final String STEP_DESCRIPTION = "Step description";

    private static final String LABEL_NAME = "Label name";

    private static final String CLASS_UUID = "179f193b-2519-4ae9-a364-173c3d8fa6cd";

    private static final String BEFORE_EACH_NAME = "Before Each name";
    private static final String BEFORE_EACH_DESCRIPTION = "Before Each description";

    private static final String AFTER_EACH_NAME = "After Each name";
    private static final String AFTER_EACH_DESCRIPTION = "After Each description";

    private static final String BEFORE_ALL_NAME = "Before All name";
    private static final String BEFORE_ALL_DESCRIPTION = "Before All description";

    private static final String AFTER_ALL_NAME = "After All name";
    private static final String AFTER_ALL_DESCRIPTION = "After All description";

    public static List<UUID> generateListUuid(){
        List<UUID> uuids = new ArrayList<>();
        uuids.add(UUID.randomUUID());
        return uuids;
    }

    public static TestResult generateTestResult() {
        Date startDate = new Date();
        Date stopDate = new Date(startDate.getTime() + 1000);

        List<LinkItem> links = new ArrayList<>();
        links.add(generateLinkItem());

        List<StepResult> steps = new ArrayList<>();
        steps.add(generateStepResult());

        List<Label> labels = new ArrayList<>();
        Label label = new Label().setName(LABEL_NAME);
        labels.add(label);

        TestResult testResult = new TestResult();
        List<String> workItems = new ArrayList<>();
        workItems.add(WORK_ITEM_ID);
        testResult.setExternalId(EXTERNAL_ID)
                .setUuid(TEST_UUID)
                .setTitle(TITLE)
                .setDescription(DESCRIPTION)
                .setClassName(CLASS_NAME)
                .setName(NAME)
                .setSpaceName(SPACE_NAME)
                .setStart(startDate.getTime())
                .setStop(stopDate.getTime())
                .setWorkItemIds(workItems)
                .setItemStatus(ITEM_STATUS)
                .setLinkItems(links)
                .setSteps(steps)
                .setLabels(labels);

        return testResult;
    }

    public static LinkItem generateLinkItem() {
        return new LinkItem()
                .setTitle(LINK_TITLE)
                .setDescription(LINK_DESCRIPTION)
                .setType(LINK_TYPE)
                .setUrl(LINK_URL);
    }

    public static TestResultResponse generateTestResultModel(){
        TestResultResponse model = new TestResultResponse();

        model.setDurationInMs(12345L);

        return model;
    }

    public static AutoTestApiResult generateAutoTestApiResult(String projectId) {
        AutoTestApiResult model = new AutoTestApiResult();

        model.setName(NAME);
        model.setTitle(TITLE);
        model.setDescription(DESCRIPTION);
        model.setClassname(CLASS_NAME);
        model.setExternalId(EXTERNAL_ID);
        model.setNamespace(SPACE_NAME);
        model.setProjectId(UUID.fromString(projectId));
        model.setSteps(generateStepsApiResults());
        model.setLinks(generatePutLinks());
        model.setLabels(generateLabelsApiResults());
        model.setSetup(new ArrayList<>());
        model.setTeardown(new ArrayList<>());
        model.setId(UUID.fromString(TEST_UUID));

        return model;
    }

    public static AutoTestUpdateApiModel generateAutoTestUpdateApiModel(String projectId) {
        AutoTestUpdateApiModel model = new AutoTestUpdateApiModel();

        model.setTitle(TITLE);
        model.setExternalId(EXTERNAL_ID);
        model.setName(NAME);
        model.setDescription(DESCRIPTION);
        model.setClassname(CLASS_NAME);
        model.setNamespace(SPACE_NAME);
        model.setSteps(generateSteps());
        model.setLinks(generateUpdateApiLinks());
        model.setLabels(generatePostLabels());
        model.setProjectId(UUID.fromString(projectId));
        model.setSetup(new ArrayList<>());
        model.setTeardown(new ArrayList<>());
        model.setId(UUID.fromString(TEST_UUID));
        model.isFlaky(null);

        return model;
    }

    public static AutoTestCreateApiModel generateAutoTestCreateApiModel(String projectId) {
        AutoTestCreateApiModel model = new AutoTestCreateApiModel();

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
        model.shouldCreateWorkItem(false);

        return model;
    }

    public static ClassContainer generateClassContainer() {
        ClassContainer container = new ClassContainer();

        container.setUuid(CLASS_UUID);
        container.getChildren().add(TEST_UUID);
        container.getBeforeEachTest().add(generateBeforeEachFixtureResult());
        container.getAfterEachTest().add(generateAfterEachFixtureResult());

        return container;
    }

    public static MainContainer generateMainContainer() {
        MainContainer container = new MainContainer();

        container.getChildren().add(CLASS_UUID);
        container.getBeforeMethods().add(generateBeforeAllFixtureResult());
        container.getAfterMethods().add(generateAfterAllFixtureResult());

        return container;
    }

    public static StepResult generateStepResult() {
        Date startDate = new Date();
        Date stopDate = new Date(startDate.getTime() + 500);

        StepResult result = new StepResult();

        result.setTitle(STEP_TITLE);
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

        fixtureResult.setTitle(BEFORE_EACH_NAME)
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

        fixtureResult.setTitle(AFTER_EACH_NAME)
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

        fixtureResult.setTitle(BEFORE_ALL_NAME)
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

        fixtureResult.setTitle(AFTER_ALL_NAME)
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

    private static List<LabelApiResult> generateLabelsApiResults() {
        List<LabelApiResult> labels = new ArrayList<>();

        LabelApiResult label = new LabelApiResult();
        label.setName(LABEL_NAME);

        labels.add(label);

        return labels;
    }



    private static List<LabelApiModel> generatePostLabels() {
        List<LabelApiModel> labels = new ArrayList<>();

        LabelApiModel label = new LabelApiModel();
        label.setName(LABEL_NAME);

        labels.add(label);

        return labels;
    }

    private static List<LinkApiResult> generatePutLinks() {
        List<LinkApiResult> links = new ArrayList<>();

        LinkApiResult link = new LinkApiResult();
        link.setTitle(LINK_TITLE);
        link.setDescription(LINK_DESCRIPTION);
        link.setUrl(LINK_URL);
        link.setType(ru.testit.client.model.LinkType.fromValue(LINK_TYPE.getValue()));

        links.add(link);

        return links;
    }

    private static List<LinkUpdateApiModel> generateUpdateApiLinks() {
        List<LinkUpdateApiModel> links = new ArrayList<>();

        LinkUpdateApiModel link = new LinkUpdateApiModel();
        link.setTitle(LINK_TITLE);
        link.setDescription(LINK_DESCRIPTION);
        link.setUrl(LINK_URL);
        link.setType(ru.testit.client.model.LinkType.fromValue(LINK_TYPE.getValue()));

        links.add(link);

        return links;
    }

    private static List<LinkCreateApiModel> generatePostLinks() {
        List<LinkCreateApiModel> links = new ArrayList<>();

        LinkCreateApiModel link = new LinkCreateApiModel();
        link.setTitle(LINK_TITLE);
        link.setDescription(LINK_DESCRIPTION);
        link.setUrl(LINK_URL);
        link.setType(ru.testit.client.model.LinkType.fromValue(LINK_TYPE.getValue()));

        links.add(link);

        return links;
    }

    private static List<AutoTestStepApiModel> generateSteps() {
        List<AutoTestStepApiModel> steps = new ArrayList<>();
        AutoTestStepApiModel step = new AutoTestStepApiModel();
        step.setTitle(STEP_TITLE);
        step.setDescription(STEP_DESCRIPTION);
        step.setSteps(new ArrayList<>());
        steps.add(step);
        return steps;
    }

    private static List<AutoTestStepApiResult> generateStepsApiResults() {
        List<AutoTestStepApiResult> steps = new ArrayList<>();
        AutoTestStepApiResult step = new AutoTestStepApiResult();
        step.setTitle(STEP_TITLE);
        step.setDescription(STEP_DESCRIPTION);
        step.setSteps(new ArrayList<>());
        steps.add(step);
        return steps;
    }
}
