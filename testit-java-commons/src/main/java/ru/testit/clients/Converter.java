package ru.testit.clients;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.testit.client.invoker.ApiException;
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

    private Converter() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Converter.class);

    public static AutoTestCreateApiModel testResultToAutoTestCreateApiModel(TestResult result) {
        AutoTestCreateApiModel model = new AutoTestCreateApiModel();

        model.setExternalId(result.getExternalId());
        model.setDescription(result.getDescription());
        model.setName(result.getName());
        model.setClassname(result.getClassName());
        model.setNamespace(result.getSpaceName());
        model.setTitle(result.getTitle());
        model.setLinks(convertCreateLinks(result.getLinkItems()));
        model.setSteps(convertStepsToApiModels(result.getSteps()));
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

    public static List<AutoTestStepApiModel> convertFixtureToApi(List<FixtureResult> fixtures, String parentUuid) {
        return fixtures.stream()
                .filter(fixture -> filterSteps(parentUuid, fixture))
                .map(fixture -> {
                            AutoTestStepApiModel model = new AutoTestStepApiModel();

                            model.setTitle(fixture.getName());
                            model.setDescription(fixture.getDescription());
                            model.setSteps(convertStepsToApiModels(fixture.getSteps()));

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
        model.setStatusCode(result.getItemStatus().value());
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
        model.setStatusCode(result.getStatus().getCode());
        model.setLinks(result.getLinks());
        model.setStepResults(result.getStepResults());
        model.setFailureClassIds(result.getFailureClassIds());
        model.setComment(result.getComment());
        if (result.getAttachments() != null) {
            model.setAttachments(convertAttachmentsFromModel(result.getAttachments()));
        }
        return model;
    }

    public static AutoTestUpdateApiModel testResultToAutoTestUpdateApiModel(TestResult result) {
        AutoTestUpdateApiModel model = new AutoTestUpdateApiModel();

        model.setExternalId(result.getExternalId());
        model.setDescription(result.getDescription());
        model.setName(result.getName());
        model.setClassname(result.getClassName());
        model.setNamespace(result.getSpaceName());
        model.setTitle(result.getTitle());
        model.setLinks(convertPutLinks(result.getLinkItems()));
        model.setSteps(convertStepsToApiModels(result.getSteps()));
        model.setLabels(labelsPostConvert(result.getLabels()));
        model.setSetup(new ArrayList<>());
        model.setTeardown(new ArrayList<>());
        model.externalKey(result.getExternalKey());

        return model;
    }

    public static AutoTestUpdateApiModel AutoTestApiResultToAutoTestUpdateApiModel(AutoTestApiResult autoTestApiResult) {
        AutoTestUpdateApiModel model = new AutoTestUpdateApiModel();

        model.setId(autoTestApiResult.getId());
        model.setExternalId(autoTestApiResult.getExternalId());
        model.setLinks(Converter.buildLinkUpdateApiModel(autoTestApiResult.getLinks()));
        model.setProjectId(autoTestApiResult.getProjectId());
        model.setName(autoTestApiResult.getName());
        model.setNamespace(autoTestApiResult.getNamespace());
        model.setClassname(autoTestApiResult.getClassname());
        model.setSteps(convertAutoTestStepApiResultsToModels(autoTestApiResult.getSteps()));
        model.setSetup(convertAutoTestStepApiResultsToModels(autoTestApiResult.getSetup()));
        model.setTeardown(convertAutoTestStepApiResultsToModels(autoTestApiResult.getTeardown()));
        model.setTitle(autoTestApiResult.getTitle());
        model.setDescription(autoTestApiResult.getDescription());
        model.setLabels(labelsConvertFromApi(autoTestApiResult.getLabels()));
        model.externalKey(autoTestApiResult.getExternalKey());

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
                    model.setHasInfo(false);

                    return model;
                }
        ).collect(Collectors.toList());
    }

    private static List<LinkCreateApiModel> convertCreateLinks(List<LinkItem> links) {
        return links.stream().map(
                link -> {
                    LinkCreateApiModel model = new LinkCreateApiModel();

                    model.setTitle(link.getTitle());
                    model.setDescription(link.getDescription());
                    model.setUrl(link.getUrl());
                    model.setType(LinkType.fromValue(link.getType().getValue()));
                    model.setHasInfo(false);

                    return model;
                }
        ).collect(Collectors.toList());
    }

    public static List<LinkUpdateApiModel> convertPutLinks(List<LinkItem> links) {
        return links.stream().map(
                link -> {
                    LinkUpdateApiModel model = new LinkUpdateApiModel();

                    model.setTitle(link.getTitle());
                    model.setDescription(link.getDescription());
                    model.setUrl(link.getUrl());
                    model.setType(LinkType.fromValue(link.getType().getValue()));
                    model.setHasInfo(false);

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

    private static List<AutoTestStepApiModel> convertStepsToApiModels(List<StepResult> steps) {
        return steps.stream().map(step -> {
            AutoTestStepApiModel model = new AutoTestStepApiModel();
            model.setTitle(step.getName());
            model.setDescription(step.getDescription());
            model.setSteps(convertStepsToApiModels(step.getSteps()));

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
            Long stop = step.getStop();
            Long start = step.getStart();
            if (stop != null && start != null) {
                model.setDuration(stop - start);
            }
            if (step.getItemStatus() != null) {
                model.setOutcome(AvailableTestResultOutcome.fromValue(step.getItemStatus().value()));
            }
            model.setStepResults(convertResultStep(step.getSteps()));
            model.attachments(convertAttachments(step.getAttachments()));
            model.parameters(step.getParameters());

            return model;
        }).collect(Collectors.toList());
    }

    private static List<LabelApiModel> labelsConvert(List<LabelShortModel> labels) {
        return labels.stream().map(label -> {
            LabelApiModel model = new LabelApiModel();

            model.setName(label.getName());

            return model;
        }).collect(Collectors.toList());
    }

    private static List<LabelApiModel> labelsConvertFromApi(List<LabelApiResult> labels) {
        return labels.stream().map(label -> {
            LabelApiModel model = new LabelApiModel();

            model.setName(label.getName());

            return model;
        }).collect(Collectors.toList());
    }

    private static List<LabelApiModel> labelsPostConvert(List<Label> labels) {
        return labels.stream().map(label -> {
            LabelApiModel model = new LabelApiModel();

            model.setName(label.getName());

            return model;
        }).collect(Collectors.toList());
    }

    private static OffsetDateTime dateToOffsetDateTime(Long time) {
        if (time == null) {
            return null;
        }
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

    public static AutoTestApiResult convertAutoTestApiResultToAutoTestApiResult(AutoTestApiResult autoTestApiResult) {
        if (autoTestApiResult == null || autoTestApiResult.getExternalId() == null) {
            return null;
        }

        AutoTestApiResult model = new AutoTestApiResult();

        model.setId(autoTestApiResult.getId());
        model.setGlobalId(autoTestApiResult.getGlobalId());
        model.setExternalId(autoTestApiResult.getExternalId());
        model.setLinks(autoTestApiResult.getLinks());
        model.setProjectId(autoTestApiResult.getProjectId());
        model.setName(autoTestApiResult.getName());
        model.setNamespace(autoTestApiResult.getNamespace());
        model.setClassname(autoTestApiResult.getClassname());
        model.setSteps(autoTestApiResult.getSteps());
        model.setSetup(autoTestApiResult.getSetup());
        model.setTeardown(autoTestApiResult.getTeardown());
        model.setTitle(autoTestApiResult.getTitle());
        model.setDescription(autoTestApiResult.getDescription());
        model.setLabels(autoTestApiResult.getLabels());
        model.externalKey(autoTestApiResult.getExternalKey());

        return model;
    }

    private static List<AutoTestStepApiModel> convertAutoTestStepApiResultsToModels(List<AutoTestStepApiResult> steps) {
        if (steps == null) {
            return new ArrayList<>();
        }

        return steps.stream().map(step -> {
            AutoTestStepApiModel model = new AutoTestStepApiModel();
            model.setTitle(step.getTitle());
            model.setDescription(step.getDescription());

            if (step.getSteps() != null) {
                model.setSteps(convertAutoTestStepApiResultsToModels(step.getSteps()));
            }

            return model;
        }).collect(Collectors.toList());
    }


    public static AutoTestStepResultUpdateRequest stepResultToRequest(AttachmentPutModelAutoTestStepResultsModel model) {
        AutoTestStepResultUpdateRequest req = new AutoTestStepResultUpdateRequest();
        req.setTitle(model.getTitle());
        req.setDescription(model.getDescription());
        req.setInfo(model.getInfo());
        req.setStartedOn(model.getStartedOn());
        req.setCompletedOn(model.getCompletedOn());
        req.setDuration(model.getDuration());
        req.setOutcome(model.getOutcome());
        req.setStepResults(stepResultsToRequests(model.getStepResults()));
        req.setAttachments(attachmentsToRequests(model.getAttachments()));
        req.setParameters(model.getParameters());

        return req;
    }

    public static List<AutoTestStepResultUpdateRequest> stepResultsToRequests(List<AttachmentPutModelAutoTestStepResultsModel> models) {
        if (models == null) return null;
        return models.stream().map(Converter::stepResultToRequest).collect(Collectors.toList());
    }

    public static AttachmentUpdateRequest attachmentToRequest(AttachmentPutModel model) {
        AttachmentUpdateRequest req = new AttachmentUpdateRequest();
        req.setId(model.getId());

        return req;
    }

    public static List<AttachmentUpdateRequest> attachmentsToRequests(List<AttachmentPutModel> models) {
        if (models == null) return null;
        return models.stream().map(Converter::attachmentToRequest).collect(Collectors.toList());
    }

    public static AutoTestCreateApiModel prepareToCreateAutoTest(
            TestResult testResult,
            String projectId) throws ApiException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Preparing to create the auto test {}", testResult.getExternalId());
        }

        AutoTestCreateApiModel model = Converter.testResultToAutoTestCreateApiModel(testResult);
        model.setProjectId(UUID.fromString(projectId));

        // TODO: add WorkItemIds to AutoTestUpdateApiModel and AutoTestCreateApiModel models after fixing the API
        // List<UUID> workItemUuids = apiClient.GetWorkItemUuidsByIds(testResult.getWorkItemIds());

        // model.setWorkItemIdsForLinkWithAutoTest(new HashSet<>(workItemUuids));

        return model;
    }

    public static AutoTestUpdateApiModel prepareToUpdateAutoTest(
            TestResult testResult,
            AutoTestApiResult autotest,
            String projectId) throws ApiException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Preparing to update the auto test {}", testResult.getExternalId());
        }

        AutoTestUpdateApiModel model;

        if (testResult.getItemStatus() == ItemStatus.FAILED) {
            model = Converter.AutoTestApiResultToAutoTestUpdateApiModel(autotest);
            model.links(Converter.convertPutLinks(testResult.getLinkItems()));
        } else {
            model = Converter.testResultToAutoTestUpdateApiModel(testResult);
            model.setProjectId(UUID.fromString(projectId));
        }

        model.setIsFlaky(autotest.getIsFlaky());

        // TODO: add WorkItemIds to AutoTestUpdateApiModel and AutoTestCreateApiModel models after fixing the API
        // List<UUID> workItemUuids = apiClient.GetWorkItemUuidsByIds(testResult.getWorkItemIds());

        // workItemUuids = prepareWorkItemUuidsForUpdateAutoTest(workItemUuids, autotest.getId().toString());

        // model.setWorkItemIdsForLinkWithAutoTest(new HashSet<>(workItemUuids));

        return model;
    }

    public static AutoTestResultsForTestRunModel prepareTestResultForTestRun(
            TestResult testResult,
            String configurationId
    ) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Preparing the test result for the auto test {}", testResult.getExternalId());
        }

        AutoTestResultsForTestRunModel model = Converter.testResultToAutoTestResultsForTestRunModel(testResult);
        model.setConfigurationId(UUID.fromString(configurationId));

        return model;
    }

    public static TestResultsFilterApiModel buildTestResultsFilterApiModelWithInProgressOutcome(
            UUID testRunId, UUID configurationId) {
        TestResultsFilterApiModel model = new TestResultsFilterApiModel();

        model.setTestRunIds(listOf(testRunId));
        model.setConfigurationIds(listOf(configurationId));
        model.setStatusCodes(listOf("InProgress"));

        return model;
    }

    public static AutoTestSearchApiModel buildAutoTestSearchApiModel(Set<Long> globalIds) {
        AutoTestFilterApiModel filter = new AutoTestFilterApiModel();

        filter.setGlobalIds(globalIds);

        AutoTestSearchIncludeApiModel includes = new AutoTestSearchIncludeApiModel();
        includes.setIncludeLabels(false);
        includes.setIncludeSteps(false);
        includes.setIncludeLinks(false);

        AutoTestSearchApiModel model = new AutoTestSearchApiModel();
        model.setFilter(filter);
        model.setIncludes(includes);

        return model;
    }

    public static UpdateEmptyTestRunApiModel buildUpdateEmptyTestRunApiModel(TestRunV2ApiResult testRun) {
        UpdateEmptyTestRunApiModel model = new UpdateEmptyTestRunApiModel();

        model.setId(testRun.getId());
        model.setName(testRun.getName());
        model.setDescription(testRun.getDescription());
        model.setAttachments(Converter.buildAssignAttachmentApiModels(testRun.getAttachments()));
        model.setLinks(Converter.buildUpdateLinkApiModels(testRun.getLinks()));
        model.setLaunchSource(testRun.getLaunchSource());

        return model;
    }

    public static List<AssignAttachmentApiModel> buildAssignAttachmentApiModels(List<AttachmentApiResult> attachments) {
        return attachments.stream().map(
                attachment -> {
                    AssignAttachmentApiModel model = new AssignAttachmentApiModel();

                    model.setId(attachment.getId());

                    return model;
                }
        ).collect(Collectors.toList());
    }

    public static List<UpdateLinkApiModel> buildUpdateLinkApiModels(List<LinkApiResult> links) {
        return links.stream().map(
                link -> {
                    UpdateLinkApiModel model = new UpdateLinkApiModel();

                    model.setId(link.getId());
                    model.setTitle(link.getTitle());
                    model.setDescription(link.getDescription());
                    model.setUrl(link.getUrl());
                    model.setType(LinkType.fromValue(link.getType().getValue()));
                    model.setHasInfo(false);

                    return model;
                }
        ).collect(Collectors.toList());
    }

    public static List<LinkUpdateApiModel> buildLinkUpdateApiModel(List<LinkApiResult> links) {
        return links.stream().map(
                link -> {
                    LinkUpdateApiModel model = new LinkUpdateApiModel();

                    model.setId(link.getId());
                    model.setTitle(link.getTitle());
                    model.setDescription(link.getDescription());
                    model.setUrl(link.getUrl());
                    model.setType(LinkType.fromValue(link.getType().getValue()));
                    model.setHasInfo(false);

                    return model;
                }
        ).collect(Collectors.toList());
    }

    @SafeVarargs
    private static <T> List<T> listOf(T... elements) {
        if (elements == null || elements.length == 0) {
            return Collections.emptyList();
        }
        return Arrays.asList(elements);
    }

    public static Map<String, List<String>> getRelationWorkItemIdsToAutotestIdsByExternalIds(
            Map<String, List<String>> relationWorkItemIdsToAutotestExternalIdsBeingCreated,
            List<AutoTestApiResult> AutoTestApiResults) {
        Map<String, List<String>> relationWorkItemIdsToAutotestIds = new HashMap<>();
        Set<String> externalIds = relationWorkItemIdsToAutotestExternalIdsBeingCreated.keySet();

        for (String externalId : externalIds) {
            AutoTestApiResults.stream()
                    .filter(m -> Objects.equals(m.getExternalId(), externalId))
                    .findFirst().ifPresent(autotest -> relationWorkItemIdsToAutotestIds.put(
                            autotest.getId().toString(),
                            relationWorkItemIdsToAutotestExternalIdsBeingCreated.get(externalId)));

        }

        return relationWorkItemIdsToAutotestIds;
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
                    model.setHasInfo(false);

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
