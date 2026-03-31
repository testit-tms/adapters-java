package ru.testit.syncstorage.api;

import ru.testit.syncstorage.invoker.ApiException;
import ru.testit.syncstorage.invoker.ApiClient;
import ru.testit.syncstorage.invoker.ApiResponse;
import ru.testit.syncstorage.invoker.Configuration;
import ru.testit.syncstorage.invoker.Pair;

import jakarta.ws.rs.core.GenericType;

import ru.testit.syncstorage.model.ShutdownResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.18.0")
public class SystemApi {
  private ApiClient apiClient;

  public SystemApi() {
    this(Configuration.getDefaultApiClient());
  }

  public SystemApi(ApiClient apiClient) {
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
   * Shutdown service
   *  Initiate shutdown of the service.
   * @return ShutdownResponse
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 200 </td><td> Shutdown initiated </td><td>  -  </td></tr>
     </table>
   */
  public ShutdownResponse shutdownPost() throws ApiException {
    return shutdownPostWithHttpInfo().getData();
  }

  /**
   * Shutdown service
   *  Initiate shutdown of the service.
   * @return ApiResponse&lt;ShutdownResponse&gt;
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 200 </td><td> Shutdown initiated </td><td>  -  </td></tr>
     </table>
   */
  public ApiResponse<ShutdownResponse> shutdownPostWithHttpInfo() throws ApiException {
    String localVarAccept = apiClient.selectHeaderAccept("application/json");
    String localVarContentType = apiClient.selectHeaderContentType();
    GenericType<ShutdownResponse> localVarReturnType = new GenericType<ShutdownResponse>() {};
    return apiClient.invokeAPI("SystemApi.shutdownPost", "/shutdown", "POST", new ArrayList<>(), null,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType,
                               null, localVarReturnType, false);
  }
}
