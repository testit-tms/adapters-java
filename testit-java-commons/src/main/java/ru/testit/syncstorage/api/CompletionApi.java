package ru.testit.syncstorage.api;

import ru.testit.syncstorage.invoker.ApiException;
import ru.testit.syncstorage.invoker.ApiClient;
import ru.testit.syncstorage.invoker.ApiResponse;
import ru.testit.syncstorage.invoker.Configuration;
import ru.testit.syncstorage.invoker.Pair;

import jakarta.ws.rs.core.GenericType;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.11.0")
public class CompletionApi {
  private ApiClient apiClient;

  public CompletionApi() {
    this(Configuration.getDefaultApiClient());
  }

  public CompletionApi(ApiClient apiClient) {
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
   * Force completion of a test run
   *  Force processing completion for a specific test run.
   * @param testRunId Test Run ID (required)
   * @return Object
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 200 </td><td> Completion status JSON </td><td>  -  </td></tr>
     </table>
   */
  public Object forceCompletionGet(String testRunId) throws ApiException {
    return forceCompletionGetWithHttpInfo(testRunId).getData();
  }

  /**
   * Force completion of a test run
   *  Force processing completion for a specific test run.
   * @param testRunId Test Run ID (required)
   * @return ApiResponse&lt;Object&gt;
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 200 </td><td> Completion status JSON </td><td>  -  </td></tr>
     </table>
   */
  public ApiResponse<Object> forceCompletionGetWithHttpInfo(String testRunId) throws ApiException {
    // Check required parameters
    if (testRunId == null) {
      throw new ApiException(400, "Missing the required parameter 'testRunId' when calling forceCompletionGet");
    }

    // Query parameters
    List<Pair> localVarQueryParams = new ArrayList<>(
            apiClient.parameterToPairs("", "testRunId", testRunId)
    );

    String localVarAccept = apiClient.selectHeaderAccept("application/json");
    String localVarContentType = apiClient.selectHeaderContentType();
    GenericType<Object> localVarReturnType = new GenericType<Object>() {};
    return apiClient.invokeAPI("CompletionApi.forceCompletionGet", "/force-completion", "GET", localVarQueryParams, null,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType,
                               null, localVarReturnType, false);
  }
  /**
   * Wait for completion
   *  Wait until processing is completed for a test run.
   * @param testRunId Test Run ID (required)
   * @return Object
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 200 </td><td> Completion status </td><td>  -  </td></tr>
     </table>
   */
  public Object waitCompletionGet(String testRunId) throws ApiException {
    return waitCompletionGetWithHttpInfo(testRunId).getData();
  }

  /**
   * Wait for completion
   *  Wait until processing is completed for a test run.
   * @param testRunId Test Run ID (required)
   * @return ApiResponse&lt;Object&gt;
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 200 </td><td> Completion status </td><td>  -  </td></tr>
     </table>
   */
  public ApiResponse<Object> waitCompletionGetWithHttpInfo(String testRunId) throws ApiException {
    // Check required parameters
    if (testRunId == null) {
      throw new ApiException(400, "Missing the required parameter 'testRunId' when calling waitCompletionGet");
    }

    // Query parameters
    List<Pair> localVarQueryParams = new ArrayList<>(
            apiClient.parameterToPairs("", "testRunId", testRunId)
    );

    String localVarAccept = apiClient.selectHeaderAccept("application/json");
    String localVarContentType = apiClient.selectHeaderContentType();
    GenericType<Object> localVarReturnType = new GenericType<Object>() {};
    return apiClient.invokeAPI("CompletionApi.waitCompletionGet", "/wait-completion", "GET", localVarQueryParams, null,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType,
                               null, localVarReturnType, false);
  }
}
