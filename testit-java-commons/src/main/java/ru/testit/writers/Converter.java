package ru.testit.writers;

import org.apache.commons.lang3.exception.ExceptionUtils;
import ru.testit.client.model.LinkType;
import ru.testit.client.model.*;
import ru.testit.models.*;
import ru.testit.models.StepResult;
import ru.testit.models.Label;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

public class Converter {
    public static AutoTestPostModel testResultToAutoTestPostModel(TestResult result) {
        AutoTestPostModel model = new AutoTestPostModel();

        model.setExternalId(result.getExternalId());
        model.setDescription(result.getDescription());
        model.setName(result.getName());
        model.setClassname(result.getClassName());
        model.setNamespace(result.getSpaceName());
        model.setTitle(result.getTitle());
        model.setLinks(convertPostLinks(result.getLinkItems()));
        model.setSteps(convertSteps(result.getSteps()));
        model.setLabels(labelsPostConvert(result.getLabels()));
        model.shouldCreateWorkItem(result.getAutomaticCreationTestCases());
        model.externalKey(result.getExternalKey());

        return model;
    }

    public static List<AutoTestStepModel> convertFixture(List<FixtureResult> fixtures, String parentUuid) {
        return fixtures.stream()
                .filter(fixture -> filterSteps(parentUuid, fixture))
                .map(fixture -> {
                            AutoTestStepModel model = new AutoTestStepModel();

                            model.setTitle(fixture.getName());
                            model.setDescription(fixture.getDescription());
                            model.setSteps(convertSteps(fixture.getSteps()));

                            return model;
                        }
                ).collect(Collectors.toList());
    }

    private static boolean filterSteps(String parentUuid, FixtureResult f) {
        if (f == null) {
            return false;
        }

        if (parentUuid != null && Objects.equals(f.getParent(), parentUuid)) {
            return true;
        } else return parentUuid == null || Objects.equals(f.getParent(), parentUuid);
    }

    public static AutoTestResultsForTestRunModel testResultToAutoTestResultsForTestRunModel(TestResult result) {
        AutoTestResultsForTestRunModel model = new AutoTestResultsForTestRunModel();

        model.setLinks(convertPostLinks(result.getResultLinks()));
        model.setAutoTestExternalId(result.getExternalId());
        model.setStartedOn(dateToOffsetDateTime(result.getStart()));
        model.setCompletedOn(dateToOffsetDateTime(result.getStop()));
        model.setDuration(result.getStop() - result.getStart());
        model.setOutcome(AvailableTestResultOutcome.fromValue(result.getItemStatus().value()));
        model.setStepResults(convertResultStep(result.getSteps()));
        model.attachments(convertAttachments(result.getAttachments()));
        model.setMessage(result.getMessage());
        model.setParameters(result.getParameters());

        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            model.setMessage(throwable.getMessage());
            model.setTraces(ExceptionUtils.getStackTrace(throwable));
        }

        return model;
    }

    public static List<AttachmentPutModelAutoTestStepResultsModel> convertResultFixture(List<FixtureResult> fixtures, String parentUuid) {
        return fixtures.stream().filter(f -> filterSteps(parentUuid, f))
                .map(fixture -> {
                            AttachmentPutModelAutoTestStepResultsModel model =
                                    new AttachmentPutModelAutoTestStepResultsModel();

                            model.setTitle(fixture.getName());
                            model.setDescription(fixture.getDescription());
                            model.setStartedOn(dateToOffsetDateTime(fixture.getStart()));
                            model.setCompletedOn(dateToOffsetDateTime(fixture.getStop()));
                            model.setDuration(fixture.getStop() - fixture.getStart());
                            model.setOutcome(AvailableTestResultOutcome.fromValue(fixture.getItemStatus().value()));
                            model.setStepResults(convertResultStep(fixture.getSteps()));
                            model.attachments(convertAttachments(fixture.getAttachments()));
                            model.parameters(fixture.getParameters());

                            return model;
                        }
                ).collect(Collectors.toList());
    }

    public static TestResultUpdateV2Request testResultToTestResultUpdateModel(TestResultResponse result) {
        TestResultUpdateV2Request model = new TestResultUpdateV2Request();

        model.setDuration(result.getDurationInMs());
        model.setOutcome(result.getOutcome());
        model.setLinks(result.getLinks());
        model.setStepResults(result.getStepResults());
        model.setFailureClassIds(result.getFailureClassIds());
        model.setComment(result.getComment());
        if (result.getAttachments() != null) {
            model.setAttachments(convertAttachmentsFromModel(result.getAttachments()));
        }
        return model;
    }

    public static AutoTestPutModel testResultToAutoTestPutModel(TestResult result) {
        AutoTestPutModel model = new AutoTestPutModel();

        model.setExternalId(result.getExternalId());
        model.setDescription(result.getDescription());
        model.setName(result.getName());
        model.setClassname(result.getClassName());
        model.setNamespace(result.getSpaceName());
        model.setTitle(result.getTitle());
        model.setLinks(convertPutLinks(result.getLinkItems()));
        model.setSteps(convertSteps(result.getSteps()));
        model.setLabels(labelsPostConvert(result.getLabels()));
        model.setSetup(new ArrayList<>());
        model.setTeardown(new ArrayList<>());
        model.externalKey(result.getExternalKey());

        return model;
    }

    public static AutoTestPutModel autoTestModelToAutoTestPutModel(AutoTestModel autoTestModel) {
        AutoTestPutModel model = new AutoTestPutModel();

        model.setId(autoTestModel.getId());
        model.setExternalId(autoTestModel.getExternalId());
        model.setLinks(autoTestModel.getLinks());
        model.setProjectId(autoTestModel.getProjectId());
        model.setName(autoTestModel.getName());
        model.setNamespace(autoTestModel.getNamespace());
        model.setClassname(autoTestModel.getClassname());
        model.setSteps(autoTestModel.getSteps());
        model.setSetup(autoTestModel.getSetup());
        model.setTeardown(autoTestModel.getTeardown());
        model.setTitle(autoTestModel.getTitle());
        model.setDescription(autoTestModel.getDescription());
        model.setLabels(labelsConvert(autoTestModel.getLabels()));
        model.externalKey(autoTestModel.getExternalKey());

        return model;
    }

    public static Map<String, List<String>> getRelationWorkItemIdsToAutotestIdsByExternalIds(
            Map<String, List<String>> relationWorkItemIdsToAutotestExternalIdsBeingCreated,
            List<AutoTestModel> autoTestModels) {
        Map<String, List<String>> relationWorkItemIdsToAutotestIds = new HashMap<>();
        Set<String> externalIds = relationWorkItemIdsToAutotestExternalIdsBeingCreated.keySet();

        for (String externalId : externalIds) {
            AutoTestModel autotest = autoTestModels.stream()
                    .filter(m -> m.getExternalId().equals(externalId))
                    .findFirst()
                    .orElse(null);

            if (autotest != null)
            {
                relationWorkItemIdsToAutotestIds.put(
                        autotest.getId().toString(),
                        relationWorkItemIdsToAutotestExternalIdsBeingCreated.get(externalId));
            }
        }

        return relationWorkItemIdsToAutotestIds;
    }

    private static List<LinkPostModel> convertPostLinks(List<LinkItem> links) {
        return links.stream().map(
                link -> {
                    LinkPostModel model = new LinkPostModel();

                    model.setTitle(link.getTitle());
                    model.setDescription(link.getDescription());
                    model.setUrl(link.getUrl());
                    model.setType(LinkType.fromValue(link.getType().getValue()));

                    return model;
                }
        ).collect(Collectors.toList());
    }

    public static List<LinkPutModel> convertPutLinks(List<LinkItem> links) {
        return links.stream().map(
                link -> {
                    LinkPutModel model = new LinkPutModel();

                    model.setTitle(link.getTitle());
                    model.setDescription(link.getDescription());
                    model.setUrl(link.getUrl());
                    model.setType(LinkType.fromValue(link.getType().getValue()));

                    return model;
                }
        ).collect(Collectors.toList());
    }

    private static List<AutoTestStepModel> convertSteps(List<StepResult> steps) {
        return steps.stream().map(step -> {
            AutoTestStepModel model = new AutoTestStepModel();
            model.setTitle(step.getName());
            model.setDescription(step.getDescription());
            model.setSteps(convertSteps(step.getSteps()));

            return model;
        }).collect(Collectors.toList());
    }

    private static List<AttachmentPutModelAutoTestStepResultsModel> convertResultStep(List<StepResult> steps) {
        return steps.stream().map(step -> {
            AttachmentPutModelAutoTestStepResultsModel model = new AttachmentPutModelAutoTestStepResultsModel();

            model.setTitle(step.getName());
            model.setDescription(step.getDescription());
            model.setStartedOn(dateToOffsetDateTime(step.getStart()));
            model.setCompletedOn(dateToOffsetDateTime(step.getStop()));
            model.setDuration(step.getStop() - step.getStart());
            model.setOutcome(AvailableTestResultOutcome.fromValue(step.getItemStatus().value()));
            model.setStepResults(convertResultStep(step.getSteps()));
            model.attachments(convertAttachments(step.getAttachments()));
            model.parameters(step.getParameters());

            return model;
        }).collect(Collectors.toList());
    }

    private static List<LabelPostModel> labelsConvert(List<LabelShortModel> labels) {
        return labels.stream().map(label -> {
            LabelPostModel model = new LabelPostModel();

            model.setName(label.getName());

            return model;
        }).collect(Collectors.toList());
    }

    private static List<LabelPostModel> labelsPostConvert(List<Label> labels) {
        return labels.stream().map(label -> {
            LabelPostModel model = new LabelPostModel();

            model.setName(label.getName());

            return model;
        }).collect(Collectors.toList());
    }

    private static OffsetDateTime dateToOffsetDateTime(Long time) {
        Date date = new Date(time);
        return date.toInstant().atOffset(ZoneOffset.UTC);
    }

    private static List<AttachmentPutModel> convertAttachments(List<String> uuids) {
        return uuids.stream().map(attach -> {
            AttachmentPutModel model = new AttachmentPutModel();

            model.setId(UUID.fromString(attach));

            return model;
        }).collect(Collectors.toList());
    }

    private static List<AttachmentUpdateRequest> convertAttachmentsFromModel(List<AttachmentApiResult> models) {
        return models.stream().map(attach -> {
            AttachmentUpdateRequest model = new AttachmentUpdateRequest();

            model.setId(attach.getId());

            return model;
        }).collect(Collectors.toList());
    }

    public static AutoTestModel convertAutoTestApiResultToAutoTestModel(AutoTestApiResult autoTestApiResult) {
        if (autoTestApiResult == null || autoTestApiResult.getExternalId() == null) {
            return null;
        }

        AutoTestModel model = new AutoTestModel();

        model.setId(autoTestApiResult.getId());
        model.setExternalId(autoTestApiResult.getExternalId());
        model.setLinks(convertLinkApiResultsToPutLinks(autoTestApiResult.getLinks()));
        model.setProjectId(autoTestApiResult.getProjectId());
        model.setName(autoTestApiResult.getName());
        model.setNamespace(autoTestApiResult.getNamespace());
        model.setClassname(autoTestApiResult.getClassname());
        model.setSteps(convertAutoTestStepApiResultsToSteps(autoTestApiResult.getSteps()));
        model.setSetup(convertAutoTestStepApiResultsToSteps(autoTestApiResult.getSetup()));
        model.setTeardown(convertAutoTestStepApiResultsToSteps(autoTestApiResult.getTeardown()));
        model.setTitle(autoTestApiResult.getTitle());
        model.setDescription(autoTestApiResult.getDescription());
        model.setLabels(convertLabelApiResultsToLabelShortModels(autoTestApiResult.getLabels()));
        model.externalKey(autoTestApiResult.getExternalKey());

        return model;
    }

    private static List<AutoTestStepModel> convertAutoTestStepApiResultsToSteps(List<AutoTestStepApiResult> steps) {
        if (steps == null) {
            return new ArrayList<>();
        }

        return steps.stream().map(step -> {
            AutoTestStepModel model = new AutoTestStepModel();
            model.setTitle(step.getTitle());
            model.setDescription(step.getDescription());

            if (step.getSteps() != null) {
                model.setSteps(convertAutoTestStepApiResultsToSteps(step.getSteps()));
            }

            return model;
        }).collect(Collectors.toList());
    }

    public static List<LinkPutModel> convertLinkApiResultsToPutLinks(List<LinkApiResult> links) {
        if (links == null) {
            return new ArrayList<>();
        }

        return links.stream().map(
                link -> {
                    LinkPutModel model = new LinkPutModel();

                    model.setTitle(link.getTitle());
                    model.setDescription(link.getDescription());
                    model.setUrl(link.getUrl());

                    if (link.getType() != null) {
                        model.setType(LinkType.fromValue(link.getType().getValue()));
                    }

                    return model;
                }
        ).collect(Collectors.toList());
    }

    private static List<LabelShortModel> convertLabelApiResultsToLabelShortModels(List<LabelApiResult> labels) {
        if (labels == null) {
            return new ArrayList<>();
        }

        return labels.stream().map(label -> {
            LabelShortModel model = new LabelShortModel();

            model.setName(label.getName());

            return model;
        }).collect(Collectors.toList());
    }
}
