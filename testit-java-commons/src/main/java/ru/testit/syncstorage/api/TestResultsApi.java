package ru.testit.syncstorage.api;

import ru.testit.syncstorage.invoker.ApiException;
import ru.testit.syncstorage.invoker.ApiClient;
import ru.testit.syncstorage.invoker.ApiResponse;
import ru.testit.syncstorage.invoker.Configuration;
import ru.testit.syncstorage.invoker.Pair;

import jakarta.ws.rs.core.GenericType;

import ru.testit.syncstorage.model.TestResultCutApiModel;
import ru.testit.syncstorage.model.TestResultSaveResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.11.0")
public class TestResultsApi {
  private ApiClient apiClient;

  public TestResultsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public TestResultsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Get the API client
   *
   * @return API client
   */
  public ApiClient getApiClient() {
    return apiClient;
  }

  /**
   * Set the API client
   *
   * @param apiClient an instance of API client
   */
  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Save in-progress test result
   *  Save a test result with InProgress status.
   * @param testRunId Test Run ID (required)
   * @param testResultCutApiModel  (required)
   * @return TestResultSaveResponse
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 200 </td><td> Test result saved successfully </td><td>  -  </td></tr>
     </table>
   */
  public TestResultSaveResponse inProgressTestResultPost(String testRunId, TestResultCutApiModel testResultCutApiModel) throws ApiException {
    return inProgressTestResultPostWithHttpInfo(testRunId, testResultCutApiModel).getData();
  }

  /**
   * Save in-progress test result
   *  Save a test result with InProgress status.
   * @param testRunId Test Run ID (required)
   * @param testResultCutApiModel  (required)
   * @return ApiResponse&lt;TestResultSaveResponse&gt;
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 200 </td><td> Test result saved successfully </td><td>  -  </td></tr>
     </table>
   */
  public ApiResponse<TestResultSaveResponse> inProgressTestResultPostWithHttpInfo(String testRunId, TestResultCutApiModel testResultCutApiModel) throws ApiException {
    // Check required parameters
    if (testRunId == null) {
      throw new ApiException(400, "Missing the required parameter 'testRunId' when calling inProgressTestResultPost");
    }
    if (testResultCutApiModel == null) {
      throw new ApiException(400, "Missing the required parameter 'testResultCutApiModel' when calling inProgressTestResultPost");
    }

    // Query parameters
    List<Pair> localVarQueryParams = new ArrayList<>(
            apiClient.parameterToPairs("", "testRunId", testRunId)
    );

    String localVarAccept = apiClient.selectHeaderAccept("application/json");
    String localVarContentType = apiClient.selectHeaderContentType("application/json");
    GenericType<TestResultSaveResponse> localVarReturnType = new GenericType<TestResultSaveResponse>() {};
    return apiClient.invokeAPI("TestResultsApi.inProgressTestResultPost", "/in_progress_test_result", "POST", localVarQueryParams, testResultCutApiModel,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType,
                               null, localVarReturnType, false);
  }
}
