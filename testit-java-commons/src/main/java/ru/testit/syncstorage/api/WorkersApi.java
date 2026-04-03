package ru.testit.syncstorage.api;

import ru.testit.syncstorage.invoker.ApiException;
import ru.testit.syncstorage.invoker.ApiClient;
import ru.testit.syncstorage.invoker.ApiResponse;
import ru.testit.syncstorage.invoker.Configuration;
import ru.testit.syncstorage.invoker.Pair;

import jakarta.ws.rs.core.GenericType;

import ru.testit.syncstorage.model.RegisterRequest;
import ru.testit.syncstorage.model.RegisterResponse;
import ru.testit.syncstorage.model.SetWorkerStatusRequest;
import ru.testit.syncstorage.model.SetWorkerStatusResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.11.0")
public class WorkersApi {
  private ApiClient apiClient;

  public WorkersApi() {
    this(Configuration.getDefaultApiClient());
  }

  public WorkersApi(ApiClient apiClient) {
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
   * Register a new worker
   *  Register a new worker with the sync storage service.
   * @param registerRequest  (required)
   * @return RegisterResponse
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 200 </td><td> Worker registered successfully </td><td>  -  </td></tr>
     </table>
   */
  public RegisterResponse registerPost(RegisterRequest registerRequest) throws ApiException {
    return registerPostWithHttpInfo(registerRequest).getData();
  }

  /**
   * Register a new worker
   *  Register a new worker with the sync storage service.
   * @param registerRequest  (required)
   * @return ApiResponse&lt;RegisterResponse&gt;
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 200 </td><td> Worker registered successfully </td><td>  -  </td></tr>
     </table>
   */
  public ApiResponse<RegisterResponse> registerPostWithHttpInfo(RegisterRequest registerRequest) throws ApiException {
    // Check required parameters
    if (registerRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'registerRequest' when calling registerPost");
    }

    String localVarAccept = apiClient.selectHeaderAccept("application/json");
    String localVarContentType = apiClient.selectHeaderContentType("application/json");
    GenericType<RegisterResponse> localVarReturnType = new GenericType<RegisterResponse>() {};
    return apiClient.invokeAPI("WorkersApi.registerPost", "/register", "POST", new ArrayList<>(), registerRequest,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType,
                               null, localVarReturnType, false);
  }
  /**
   * Set worker status
   *  Set the status of a worker by its PID.
   * @param setWorkerStatusRequest  (required)
   * @return SetWorkerStatusResponse
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 200 </td><td> Status updated successfully </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Bad request </td><td>  -  </td></tr>
       <tr><td> 405 </td><td> Method not allowed </td><td>  -  </td></tr>
     </table>
   */
  public SetWorkerStatusResponse setWorkerStatusPost(SetWorkerStatusRequest setWorkerStatusRequest) throws ApiException {
    return setWorkerStatusPostWithHttpInfo(setWorkerStatusRequest).getData();
  }

  /**
   * Set worker status
   *  Set the status of a worker by its PID.
   * @param setWorkerStatusRequest  (required)
   * @return ApiResponse&lt;SetWorkerStatusResponse&gt;
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 200 </td><td> Status updated successfully </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Bad request </td><td>  -  </td></tr>
       <tr><td> 405 </td><td> Method not allowed </td><td>  -  </td></tr>
     </table>
   */
  public ApiResponse<SetWorkerStatusResponse> setWorkerStatusPostWithHttpInfo(SetWorkerStatusRequest setWorkerStatusRequest) throws ApiException {
    // Check required parameters
    if (setWorkerStatusRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'setWorkerStatusRequest' when calling setWorkerStatusPost");
    }

    String localVarAccept = apiClient.selectHeaderAccept("application/json", "text/plain");
    String localVarContentType = apiClient.selectHeaderContentType("application/json");
    GenericType<SetWorkerStatusResponse> localVarReturnType = new GenericType<SetWorkerStatusResponse>() {};
    return apiClient.invokeAPI("WorkersApi.setWorkerStatusPost", "/set_worker_status", "POST", new ArrayList<>(), setWorkerStatusRequest,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType,
                               null, localVarReturnType, false);
  }
}
