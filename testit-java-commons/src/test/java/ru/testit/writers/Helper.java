package ru.testit.writers;

import ru.testit.model.*;
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

    public static TestResult generateTestResult(){
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

    public static AutoTestModel generateAutoTestModel(){
        AutoTestModel model = new AutoTestModel();

        model.setName(NAME);
        model.setTitle(TITLE);
        model.setDescription(DESCRIPTION);
        model.setClassname(CLASS_NAME);
        model.setExternalId(EXTERNAL_ID);
        model.setNamespace(SPACE_NAME);
        model.setSteps(generateSteps());
        model.setLinks(generatePutLinks());
        model.setLabels(generateShortLabels());
        model.setSetup(new ArrayList<>());
        model.setTeardown(new ArrayList<>());

        return model;
    }

    public static AutoTestPutModel generateAutoTestPutModel(String projectId){
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

        return model;
    }

    public static AutoTestPostModel generateAutoTestPostModel(String projectId){
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

    public static ClassContainer generateClassContainer(){
        ClassContainer container = new ClassContainer();

        container.setUuid(CLASS_UUID);
        container.getChildren().add(TEST_UUID);

        return container;
    }

    public static MainContainer generateMainContainer(){
        MainContainer container = new MainContainer();

        container.getChildren().add(CLASS_UUID);

        return container;
    }

    private static List<LabelShortModel> generateShortLabels() {
        List<LabelShortModel> labels = new ArrayList<>();
        LabelShortModel label = new LabelShortModel();
        label.setName(LABEL_NAME);

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
        link.setType(ru.testit.model.LinkType.fromValue(LINK_TYPE.getValue()));

        links.add(link);

        return links;
    }
    private static List<LinkPostModel> generatePostLinks() {
        List<LinkPostModel> links = new ArrayList<>();

        LinkPostModel link = new LinkPostModel();
        link.setTitle(LINK_TITLE);
        link.setDescription(LINK_DESCRIPTION);
        link.setUrl(LINK_URL);
        link.setType(ru.testit.model.LinkType.fromValue(LINK_TYPE.getValue()));

        links.add(link);

        return links;
    }
    private static List<AutoTestStepModel> generateSteps() {
        List<AutoTestStepModel> steps = new ArrayList<>();
        AutoTestStepModel step = new AutoTestStepModel();
        step.setTitle(STEP_TITLE);
        step.setDescription(STEP_DESCRIPTION);
        steps.add(step);
        return steps;
    }
}
