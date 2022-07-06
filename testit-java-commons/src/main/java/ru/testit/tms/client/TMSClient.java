package ru.testit.tms.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.testit.annotations.AddLink;
import ru.testit.models.LinkItem;
import ru.testit.models.Outcome;
import ru.testit.tms.models.config.ClientConfiguration;
import ru.testit.tms.models.request.*;
import ru.testit.tms.models.response.CreateTestItemResponse;
import ru.testit.tms.models.response.GetTestItemResponse;
import ru.testit.tms.models.response.StartLaunchResponse;
import ru.testit.writer.Converter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TMSClient implements ITMSClient {
    private static final Logger log;
    private final ObjectMapper objectMapper;
    private StartLaunchResponse startLaunchResponse;
    private static ClientConfiguration clientConfiguration;

    public TMSClient(ClientConfiguration clientConfiguration) {
        this.clientConfiguration = clientConfiguration;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String startLaunch() {
        final HttpPost post = new HttpPost(clientConfiguration.getUrl() + "/api/v2/testRuns");
        post.addHeader("Authorization", "PrivateToken " + clientConfiguration.getPrivateToken());
        try {
            final StartTestRunRequest request = new StartTestRunRequest();
            request.setProjectId(clientConfiguration.getProjectId());
            final StringEntity requestEntity = new StringEntity(this.objectMapper.writeValueAsString((Object)request), ContentType.APPLICATION_JSON);
            post.setEntity((HttpEntity)requestEntity);
            final CloseableHttpClient httpClient = HttpClients.createDefault();
            try {
                final CloseableHttpResponse response = httpClient.execute((HttpUriRequest)post);
                try {
                    this.startLaunchResponse = (StartLaunchResponse)this.objectMapper.readValue(EntityUtils.toString(response.getEntity()), (Class)StartLaunchResponse.class);
                    clientConfiguration.setTestRunId(this.startLaunchResponse.getId());
                    if (response != null) {
                        response.close();
                    }
                }
                catch (Throwable t) {
                    if (response != null) {
                        try {
                            response.close();
                        }
                        catch (Throwable t2) {
                            t.addSuppressed(t2);
                        }
                    }
                    throw t;
                }
                if (httpClient != null) {
                    httpClient.close();
                }
            }
            catch (Throwable t3) {
                if (httpClient != null) {
                    try {
                        httpClient.close();
                    }
                    catch (Throwable t4) {
                        t3.addSuppressed(t4);
                    }
                }
                throw t3;
            }
        }
        catch (IOException e) {
            TMSClient.log.error("Exception while starting test run", (Throwable)e);
        }

        return clientConfiguration.getTestRunId();
    }

    @Override
    public void sendTestItems(final Collection<CreateTestItemRequest> createTestRequests) {
        for (final CreateTestItemRequest createTestRequest : createTestRequests) {
            sendTestItem(createTestRequest);
        }
    }

    @Override
    public void sendTestItem(CreateTestItemRequest createTestRequest) {
        final GetTestItemResponse getTestItemResponse = this.getTestItem(createTestRequest.getExternalId());
        if (getTestItemResponse == null || StringUtils.isBlank((CharSequence)getTestItemResponse.getId())) {
            this.createTestItem(createTestRequest);
        }
        else {
            if (createTestRequest.getOutcome().equals(Outcome.FAILED)) {
                createTestRequest.setId(getTestItemResponse.getId());
                createTestRequest.setName(getTestItemResponse.getName());
                createTestRequest.setExternalId(getTestItemResponse.getExternalId());
                createTestRequest.setDescription(getTestItemResponse.getDescription());
                createTestRequest.setNameSpace(getTestItemResponse.getNameSpace());
                createTestRequest.setClassName(getTestItemResponse.getClassName());
                createTestRequest.setLabels(getTestItemResponse.getLabels());
                createTestRequest.setSetUp(getTestItemResponse.getSetUp());
                createTestRequest.setSteps(getTestItemResponse.getSteps());
                createTestRequest.setTearDown(getTestItemResponse.getTearDown());
                createTestRequest.setProjectId(getTestItemResponse.getProjectId());
                createTestRequest.setTitle(getTestItemResponse.getTitle());
            }
            this.updatePostItem(createTestRequest, getTestItemResponse.getId());
        }
    }

    @Override
    public GetTestItemResponse getTestItem(final String externalId) {
        final HttpGet get = new HttpGet(clientConfiguration.getUrl() + "/api/v2/autoTests?projectId=" + clientConfiguration.getProjectId() + "&externalId=" + externalId);
        get.addHeader("Authorization", "PrivateToken " + clientConfiguration.getPrivateToken());
        GetTestItemResponse getTestItemResponse = null;
        try {
            final CloseableHttpClient httpClient = HttpClients.createDefault();
            try {
                final CloseableHttpResponse response = httpClient.execute((HttpUriRequest)get);
                try {
                    final TypeFactory typeFactory = this.objectMapper.getTypeFactory();
                    final CollectionType collectionType = typeFactory.constructCollectionType((Class) List.class, (Class)GetTestItemResponse.class);
                    final List<GetTestItemResponse> listTestItems = (List<GetTestItemResponse>)this.objectMapper.readValue(EntityUtils.toString(response.getEntity()), (JavaType)collectionType);
                    if (!listTestItems.isEmpty()) {
                        getTestItemResponse = listTestItems.get(0);
                    }
                    if (response != null) {
                        response.close();
                    }
                }
                catch (Throwable t) {
                    if (response != null) {
                        try {
                            response.close();
                        }
                        catch (Throwable t2) {
                            t.addSuppressed(t2);
                        }
                    }
                    throw t;
                }
                if (httpClient != null) {
                    httpClient.close();
                }
            }
            catch (Throwable t3) {
                if (httpClient != null) {
                    try {
                        httpClient.close();
                    }
                    catch (Throwable t4) {
                        t3.addSuppressed(t4);
                    }
                }
                throw t3;
            }
        }
        catch (IOException e) {
            TMSClient.log.error("Exception while sending test item", (Throwable)e);
        }
        return getTestItemResponse;
    }

    @Override
    public void createTestItem(final CreateTestItemRequest createTestItemRequest) {
        final HttpPost post = new HttpPost(clientConfiguration.getUrl() + "/api/v2/autoTests");
        post.addHeader("Authorization", "PrivateToken " + clientConfiguration.getPrivateToken());
        CreateTestItemResponse createTestItemResponse = null;
        try {
            final StringEntity requestEntity = new StringEntity(this.objectMapper.writeValueAsString((Object)createTestItemRequest), ContentType.APPLICATION_JSON);
            post.setEntity((HttpEntity)requestEntity);
            final CloseableHttpClient httpClient = HttpClients.createDefault();
            try {
                final CloseableHttpResponse response = httpClient.execute((HttpUriRequest)post);
                try {
                    createTestItemResponse = (CreateTestItemResponse)this.objectMapper.readValue(EntityUtils.toString(response.getEntity()), (Class)CreateTestItemResponse.class);
                    if (response != null) {
                        response.close();
                    }
                }
                catch (Throwable t) {
                    if (response != null) {
                        try {
                            response.close();
                        }
                        catch (Throwable t2) {
                            t.addSuppressed(t2);
                        }
                    }
                    throw t;
                }
                if (httpClient != null) {
                    httpClient.close();
                }
            }
            catch (Throwable t3) {
                if (httpClient != null) {
                    try {
                        httpClient.close();
                    }
                    catch (Throwable t4) {
                        t3.addSuppressed(t4);
                    }
                }
                throw t3;
            }
        }
        catch (IOException e) {
            TMSClient.log.error("Exception while sending test item", (Throwable)e);
        }
        if (createTestItemResponse != null && StringUtils.isNotBlank((CharSequence)createTestItemResponse.getId())) {
            this.linkAutoTestWithTestCase(createTestItemResponse.getId(), new LinkAutoTestRequest(createTestItemRequest.getTestPlanId()));
        }
    }

    @Override
    public void updatePostItem(final CreateTestItemRequest createTestItemRequest, final String testId) {
        createTestItemRequest.setId(testId);
        final HttpPut put = new HttpPut(clientConfiguration.getUrl() + "/api/v2/autoTests");
        put.addHeader("Authorization", "PrivateToken " + clientConfiguration.getPrivateToken());
        final CreateTestItemResponse createTestItemResponse = null;
        try {
            final StringEntity requestEntity = new StringEntity(this.objectMapper.writeValueAsString((Object)createTestItemRequest), ContentType.APPLICATION_JSON);
            put.setEntity((HttpEntity)requestEntity);
            final CloseableHttpClient httpClient = HttpClients.createDefault();
            try {
                final CloseableHttpResponse response = httpClient.execute((HttpUriRequest)put);
                final Throwable t2 = null;
                if (response != null) {
                    if (t2 != null) {
                        try {
                            response.close();
                        }
                        catch (Throwable t3) {
                            t2.addSuppressed(t3);
                        }
                    }
                    else {
                        response.close();
                    }
                }
                if (httpClient != null) {
                    httpClient.close();
                }
            }
            catch (Throwable t4) {
                if (httpClient != null) {
                    try {
                        httpClient.close();
                    }
                    catch (Throwable t5) {
                        t4.addSuppressed(t5);
                    }
                }
                throw t4;
            }
        }
        catch (IOException e) {
            TMSClient.log.error("Exception while sending test item", (Throwable)e);
        }
        if (StringUtils.isNotBlank((CharSequence)createTestItemRequest.getTestPlanId())) {
            this.linkAutoTestWithTestCase(testId, new LinkAutoTestRequest(createTestItemRequest.getTestPlanId()));
        }
    }

    @Override
    public void finishLaunch(final TestResultsRequest request) {
        this.sendTestResult(request);
        this.sendCompleteTestRun();
    }

    @Override
    public List<String> sendTestResult(final TestResultsRequest request) {
        final HttpPost post = new HttpPost(clientConfiguration.getUrl() + "/api/v2/testRuns/" + clientConfiguration.getTestRunId() + "/testResults");
        post.addHeader("Authorization", "PrivateToken " + clientConfiguration.getPrivateToken());
        List<String> ids = null;
        try {
            final StringEntity requestEntity = new StringEntity(this.objectMapper.writeValueAsString((Object)request.getTestResults()), ContentType.APPLICATION_JSON);
            post.setEntity((HttpEntity)requestEntity);
            final CloseableHttpClient httpClient = HttpClients.createDefault();
            try {
                final CloseableHttpResponse response = httpClient.execute((HttpUriRequest)post);
                CollectionLikeType collectionLikeType = objectMapper.getTypeFactory()
                        .constructCollectionLikeType(List.class, String.class);
                ids = this.objectMapper.readValue(EntityUtils.toString(response.getEntity()), collectionLikeType);
                final Throwable t2 = null;
                if (response != null) {
                    if (t2 != null) {
                        try {
                            response.close();
                        }
                        catch (Throwable t3) {
                            t2.addSuppressed(t3);
                        }
                    }
                    else {
                        response.close();
                    }
                }
                if (httpClient != null) {
                    httpClient.close();
                }
            }
            catch (Throwable t4) {
                if (httpClient != null) {
                    try {
                        httpClient.close();
                    }
                    catch (Throwable t5) {
                        t4.addSuppressed(t5);
                    }
                }
                throw t4;
            }
        }
        catch (IOException e) {
            TMSClient.log.error("Exception while sending test result", (Throwable)e);
        }

        return ids;
    }
    @Override
    public void sendCompleteTestRun() {
        final HttpPost post = new HttpPost(clientConfiguration.getUrl() + "/api/v2/testRuns/" + clientConfiguration.getTestRunId() + "/complete");
        post.addHeader("Authorization", "PrivateToken " + clientConfiguration.getPrivateToken());
        try {
            final StringEntity requestEntity = new StringEntity(this.objectMapper.writeValueAsString((Object)""), ContentType.APPLICATION_JSON);
            post.setEntity((HttpEntity)requestEntity);
            final CloseableHttpClient httpClient = HttpClients.createDefault();
            try {
                final CloseableHttpResponse response = httpClient.execute((HttpUriRequest)post);
                final Throwable t2 = null;
                if (response != null) {
                    if (t2 != null) {
                        try {
                            response.close();
                        }
                        catch (Throwable t3) {
                            t2.addSuppressed(t3);
                        }
                    }
                    else {
                        response.close();
                    }
                }
                if (httpClient != null) {
                    httpClient.close();
                }
            }
            catch (Throwable t4) {
                if (httpClient != null) {
                    try {
                        httpClient.close();
                    }
                    catch (Throwable t5) {
                        t4.addSuppressed(t5);
                    }
                }
                throw t4;
            }
        }
        catch (IOException e) {
            TMSClient.log.error("Exception while sending complete test run", (Throwable)e);
        }
    }
    private void linkAutoTestWithTestCase(final String autoTestId, final LinkAutoTestRequest linkAutoTestRequest) {
        final HttpPost post = new HttpPost(clientConfiguration.getUrl() + "/api/v2/autoTests/" + autoTestId + "/workItems");
        post.addHeader("Authorization", "PrivateToken " + clientConfiguration.getPrivateToken());
        try {
            final StringEntity requestEntity = new StringEntity(this.objectMapper.writeValueAsString((Object)linkAutoTestRequest), ContentType.APPLICATION_JSON);
            post.setEntity((HttpEntity)requestEntity);
            final CloseableHttpClient httpClient = HttpClients.createDefault();
            try {
                final CloseableHttpResponse response = httpClient.execute((HttpUriRequest)post);
                final Throwable t2 = null;
                if (response != null) {
                    if (t2 != null) {
                        try {
                            response.close();
                        }
                        catch (Throwable t3) {
                            t2.addSuppressed(t3);
                        }
                    }
                    else {
                        response.close();
                    }
                }
                if (httpClient != null) {
                    httpClient.close();
                }
            }
            catch (Throwable t4) {
                if (httpClient != null) {
                    try {
                        httpClient.close();
                    }
                    catch (Throwable t5) {
                        t4.addSuppressed(t5);
                    }
                }
                throw t4;
            }
        }
        catch (IOException e) {
            TMSClient.log.error("Exception while linking auto test", (Throwable)e);
        }
    }

    @AddLink
    public static void addLink(final LinkItem linkItem) {
    }

    public static String getProjectID() {
        return clientConfiguration.getProjectId();
    }
    public static String getConfigurationId() {
        return clientConfiguration.getConfigurationId();
    }

    static {
        log = LoggerFactory.getLogger((Class) TMSClient.class);
    }
}
