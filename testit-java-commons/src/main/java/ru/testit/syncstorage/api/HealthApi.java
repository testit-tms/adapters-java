package ru.testit.syncstorage.api;

import ru.testit.syncstorage.invoker.ApiException;
import ru.testit.syncstorage.invoker.ApiClient;
import ru.testit.syncstorage.invoker.ApiResponse;
import ru.testit.syncstorage.invoker.Configuration;
import ru.testit.syncstorage.invoker.Pair;

import jakarta.ws.rs.core.GenericType;

import ru.testit.syncstorage.model.HealthStatusResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.11.0")
public class HealthApi {
  private ApiClient apiClient;

  public HealthApi() {
    this(Configuration.getDefaultApiClient());
  }

  public HealthApi(ApiClient apiClient) {
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
   * Health check
   *  Get the current health status of the service.
   * @return HealthStatusResponse
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 200 </td><td> Health status JSON </td><td>  -  </td></tr>
     </table>
   */
  public HealthStatusResponse healthGet() throws ApiException {
    return healthGetWithHttpInfo().getData();
  }

  /**
   * Health check
   *  Get the current health status of the service.
   * @return ApiResponse&lt;HealthStatusResponse&gt;
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 200 </td><td> Health status JSON </td><td>  -  </td></tr>
     </table>
   */
  public ApiResponse<HealthStatusResponse> healthGetWithHttpInfo() throws ApiException {
    String localVarAccept = apiClient.selectHeaderAccept("application/json");
    String localVarContentType = apiClient.selectHeaderContentType();
    GenericType<HealthStatusResponse> localVarReturnType = new GenericType<HealthStatusResponse>() {};
    return apiClient.invokeAPI("HealthApi.healthGet", "/health", "GET", new ArrayList<>(), null,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType,
                               null, localVarReturnType, false);
  }
}
