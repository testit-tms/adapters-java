package ru.testit.writers;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneOffset;
import ru.testit.model.*;
import ru.testit.models.FixtureResult;
import ru.testit.models.LinkItem;
import ru.testit.models.TestResult;
import ru.testit.services.Adapter;

import java.util.Date;
import java.util.List;
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

        return model;
    }

    public static List<AutoTestStepModel> convertFixture(List<String> fixtures, String parentUuid) {
        List<FixtureResult> fixtureResults = fixtures.stream()
                .map(f ->
                        Adapter.getResultStorage().getFixture(f).orElse(null)
                )
                .collect(Collectors.toList());

        return fixtureResults.stream().filter(f -> {
            if (f == null) {
                return false;
            }

            if (parentUuid != null && f.getParent() == parentUuid) {
                return true;
            } else if (parentUuid != null && f.getParent() != parentUuid) {
                return false;
            }

            return true;
        }).map(
                fixture -> {
                    AutoTestStepModel model = new AutoTestStepModel();

                    model.setTitle(fixture.getName());
                    model.setDescription(fixture.getDescription());
                    model.setSteps(convertSteps(fixture.getSteps()));

                    return model;
                }
        ).collect(Collectors.toList());
    }

    public static AutoTestResultsForTestRunModel testResultToAutoTestResultsForTestRunModel(TestResult result) {
        AutoTestResultsForTestRunModel model = new AutoTestResultsForTestRunModel();

        model.setLinks(convertPostLinks(result.getResultLinks()));
        model.setAutoTestExternalId(result.getExternalId());
        model.setStartedOn(dateToOffsetDateTime(result.getStart()));
        model.setCompletedOn(dateToOffsetDateTime(result.getStop()));
        model.setDuration(result.getStop() - result.getStart());
        model.setOutcome(result.getItemStatus().value());
        model.setStepResults(convertResultStep(result.getSteps()));

        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            model.setMessage(throwable.getMessage());
            model.setTraces(ExceptionUtils.getStackTrace(throwable));
        }

        return model;
    }

    public static List<AttachmentPutModelAutoTestStepResultsModel> convertResultFixture(List<String> fixtures, String parentUuid) {
        List<FixtureResult> fixtureResults = fixtures.stream()
                .map(f ->
                        Adapter.getResultStorage().getFixture(f).orElse(null)
                )
                .collect(Collectors.toList());

        return fixtureResults.stream().filter(f -> {
            if (f == null) {
                return false;
            }

            if (parentUuid != null && f.getParent() == parentUuid) {
                return true;
            } else if (parentUuid != null && f.getParent() != parentUuid) {
                return false;
            }

            return true;
        }).map(
                fixture -> {
                    AttachmentPutModelAutoTestStepResultsModel model = new AttachmentPutModelAutoTestStepResultsModel();

                    model.setTitle(fixture.getName());
                    model.setDescription(fixture.getDescription());
                    model.setStartedOn(dateToOffsetDateTime(fixture.getStart()));
                    model.setCompletedOn(dateToOffsetDateTime(fixture.getStop()));
                    model.setDuration(fixture.getStop() - fixture.getStart());
                    model.setOutcome(fixture.getItemStatus().value());
                    model.setStepResults(convertResultStep(fixture.getSteps()));

                    return model;
                }
        ).collect(Collectors.toList());
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

    private static List<LinkPutModel> convertPutLinks(List<LinkItem> links) {
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

    private static List<AutoTestStepModel> convertSteps(List<String> steps) {
        return steps.stream().map(stepUUID -> {
            AutoTestStepModel model = new AutoTestStepModel();

            Adapter.getResultStorage().getStep(stepUUID).ifPresent(step -> {
                model.setTitle(step.getName());
                model.setDescription(step.getDescription());
                model.setSteps(convertSteps(step.getSteps()));
            });

            return model;
        }).collect(Collectors.toList());
    }

    private static List<AttachmentPutModelAutoTestStepResultsModel> convertResultStep(List<String> steps) {
        return steps.stream().map(stepUUID -> {
            AttachmentPutModelAutoTestStepResultsModel model = new AttachmentPutModelAutoTestStepResultsModel();

            Adapter.getResultStorage().getStep(stepUUID).ifPresent(step -> {
                model.setTitle(step.getName());
                model.setDescription(step.getDescription());
                model.setStartedOn(dateToOffsetDateTime(step.getStart()));
                model.setCompletedOn(dateToOffsetDateTime(step.getStop()));
                model.setDuration(step.getStop() - step.getStart());
                model.setOutcome(step.getItemStatus().value());
                model.setStepResults(convertResultStep(step.getSteps()));
            });

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

    private static OffsetDateTime dateToOffsetDateTime(Long time) {
        Date date = new Date(time);
        return convertFrom(date.toInstant().atOffset(java.time.ZoneOffset.UTC));
    }

    private static OffsetDateTime convertFrom(java.time.OffsetDateTime ttOdt) {
        return OffsetDateTime.of(ttOdt.getYear(), ttOdt.getMonthValue(),
                ttOdt.getDayOfMonth(), ttOdt.getHour(), ttOdt.getMinute(),
                ttOdt.getSecond(), ttOdt.getNano(),
                ZoneOffset.ofTotalSeconds(ttOdt.getOffset().getTotalSeconds()));
    }
}
