package ru.testit.writers;

import org.apache.commons.lang3.exception.ExceptionUtils;
import ru.testit.client.model.LinkType;
import ru.testit.client.model.*;
import ru.testit.models.*;

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

    public static TestResultUpdateModel testResultToTestResultUpdateModel(TestResultModel result) {
        TestResultUpdateModel model = new TestResultUpdateModel();

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

        return model;
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

    private static List<AttachmentPutModel> convertAttachmentsFromModel(List<AttachmentModel> models) {
        return models.stream().map(attach -> {
            AttachmentPutModel model = new AttachmentPutModel();

            model.setId(attach.getId());

            return model;
        }).collect(Collectors.toList());
    }
}
