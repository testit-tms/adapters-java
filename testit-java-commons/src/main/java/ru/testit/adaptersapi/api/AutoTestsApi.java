package ru.testit.adaptersapi.api;

import ru.testit.adaptersapi.invoker.ApiException;
import ru.testit.adaptersapi.invoker.ApiClient;
import ru.testit.adaptersapi.invoker.ApiResponse;
import ru.testit.adaptersapi.invoker.Configuration;
import ru.testit.adaptersapi.invoker.Pair;

import jakarta.ws.rs.core.GenericType;

import ru.testit.adaptersapi.model.AutoTestApiResult;
import ru.testit.adaptersapi.model.AutoTestCreateApiModel;
import ru.testit.adaptersapi.model.AutoTestModel;
import ru.testit.adaptersapi.model.AutoTestSearchApiModel;
import ru.testit.adaptersapi.model.AutoTestUpdateApiModel;
import ru.testit.adaptersapi.model.AutoTestWorkItemIdentifierApiResult;
import ru.testit.adaptersapi.model.Operation;
import ru.testit.adaptersapi.model.ProblemDetails;
import java.util.UUID;
import ru.testit.adaptersapi.model.ValidationProblemDetails;
import ru.testit.adaptersapi.model.WorkItemIdApiModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.11.0")
public class AutoTestsApi {
  private ApiClient apiClient;

  public AutoTestsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public AutoTestsApi(ApiClient apiClient) {
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
   * Create multiple autotests
   * 
   * @param autoTestCreateApiModel  (optional)
   * @return List&lt;AutoTestApiResult&gt;
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 201 </td><td> Created </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Bad Request </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Unauthorized </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Forbidden </td><td>  -  </td></tr>
       <tr><td> 404 </td><td> Not Found </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> Conflict </td><td>  -  </td></tr>
       <tr><td> 422 </td><td> Unprocessable Entity </td><td>  -  </td></tr>
     </table>
   */
  public List<AutoTestApiResult> adaptersAutoTestsBulkPost(List<AutoTestCreateApiModel> autoTestCreateApiModel) throws ApiException {
    return adaptersAutoTestsBulkPostWithHttpInfo(autoTestCreateApiModel).getData();
  }

  /**
   * Create multiple autotests
   * 
   * @param autoTestCreateApiModel  (optional)
   * @return ApiResponse&lt;List&lt;AutoTestApiResult&gt;&gt;
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 201 </td><td> Created </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Bad Request </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Unauthorized </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Forbidden </td><td>  -  </td></tr>
       <tr><td> 404 </td><td> Not Found </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> Conflict </td><td>  -  </td></tr>
       <tr><td> 422 </td><td> Unprocessable Entity </td><td>  -  </td></tr>
     </table>
   */
  public ApiResponse<List<AutoTestApiResult>> adaptersAutoTestsBulkPostWithHttpInfo(List<AutoTestCreateApiModel> autoTestCreateApiModel) throws ApiException {
    String localVarAccept = apiClient.selectHeaderAccept("application/json");
    String localVarContentType = apiClient.selectHeaderContentType("application/json");
    String[] localVarAuthNames = new String[] {"PrivateToken", "Cookies"};
    GenericType<List<AutoTestApiResult>> localVarReturnType = new GenericType<List<AutoTestApiResult>>() {};
    return apiClient.invokeAPI("AutoTestsApi.adaptersAutoTestsBulkPost", "/adapters/autoTests/bulk", "POST", new ArrayList<>(), autoTestCreateApiModel,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType,
                               localVarAuthNames, localVarReturnType, false);
  }
  /**
   * Update multiple autotests
   * 
   * @param autoTestUpdateApiModel  (optional)
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 204 </td><td> No Content </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Bad Request </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Unauthorized </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Forbidden </td><td>  -  </td></tr>
       <tr><td> 404 </td><td> Not Found </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> Conflict </td><td>  -  </td></tr>
       <tr><td> 422 </td><td> Unprocessable Entity </td><td>  -  </td></tr>
     </table>
   */
  public void adaptersAutoTestsBulkPut(List<AutoTestUpdateApiModel> autoTestUpdateApiModel) throws ApiException {
    adaptersAutoTestsBulkPutWithHttpInfo(autoTestUpdateApiModel);
  }

  /**
   * Update multiple autotests
   * 
   * @param autoTestUpdateApiModel  (optional)
   * @return ApiResponse&lt;Void&gt;
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 204 </td><td> No Content </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Bad Request </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Unauthorized </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Forbidden </td><td>  -  </td></tr>
       <tr><td> 404 </td><td> Not Found </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> Conflict </td><td>  -  </td></tr>
       <tr><td> 422 </td><td> Unprocessable Entity </td><td>  -  </td></tr>
     </table>
   */
  public ApiResponse<Void> adaptersAutoTestsBulkPutWithHttpInfo(List<AutoTestUpdateApiModel> autoTestUpdateApiModel) throws ApiException {
    String localVarAccept = apiClient.selectHeaderAccept("application/json");
    String localVarContentType = apiClient.selectHeaderContentType("application/json");
    String[] localVarAuthNames = new String[] {"PrivateToken", "Cookies"};
    return apiClient.invokeAPI("AutoTestsApi.adaptersAutoTestsBulkPut", "/adapters/autoTests/bulk", "PUT", new ArrayList<>(), autoTestUpdateApiModel,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType,
                               localVarAuthNames, null, false);
  }
  /**
   * 
   * 
   * @param projectId Project internal ID (optional)
   * @param externalId Autotest external ID (optional)
   * @param globalId Autotest global ID (optional)
   * @param namespace Namespace in which autotest is located (optional)
   * @param isNamespaceNull OBSOLETE: Use &#x60;includeEmptyNamespaces&#x60; instead (optional)
   * @param includeEmptyNamespaces If result must contain autotests without namespace (optional)
   * @param className Name of class in which autotest is located (optional)
   * @param isClassnameNull OBSOLETE: Use &#x60;includeEmptyClassNames&#x60; instead (optional)
   * @param includeEmptyClassNames If result must contain autotests without class (optional)
   * @param isDeleted OBSOLETE: Use &#x60;deleted&#x60; instead (optional)
   * @param deleted Is autotest deleted (optional)
   * @param labels Include only autotests with provided labels (optional)
   * @param stabilityMinimal OBSOLETE: Use &#x60;minStability&#x60; instead (optional)
   * @param minStability Minimum stability value of autotest (optional)
   * @param stabilityMaximal OBSOLETE: Use &#x60;maxStability&#x60; instead (optional)
   * @param maxStability Maximum stability value of autotest (optional)
   * @param isFlaky OBSOLETE: Use &#x60;flaky&#x60; instead (optional)
   * @param flaky Is autotest marked as \&quot;Flaky\&quot; (optional)
   * @param includeSteps If result must also include autotest steps (optional)
   * @param includeLabels If result must also include autotest labels (optional)
   * @param externalKey External key of autotest (optional)
   * @param skip Amount of items to be skipped (offset) (optional)
   * @param take Amount of items to be taken (limit) (optional)
   * @param orderBy SQL-like  ORDER BY statement (column1 ASC|DESC , column2 ASC|DESC) (optional)
   * @param searchField Property name for searching (optional)
   * @param searchValue Value for searching (optional)
   * @return List&lt;AutoTestModel&gt;
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 200 </td><td> OK </td><td>  * Pagination-Skip - Skipped amount of items <br>  * Pagination-Take - Taken items <br>  * Pagination-Pages - Expected number of pages <br>  * Pagination-Total-Items - Total count of items <br>  </td></tr>
       <tr><td> 400 </td><td> Bad Request </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Unauthorized </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Forbidden </td><td>  -  </td></tr>
       <tr><td> 404 </td><td> Not Found </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> Conflict </td><td>  -  </td></tr>
       <tr><td> 422 </td><td> Unprocessable Entity </td><td>  -  </td></tr>
     </table>
   * @deprecated
   */
  @Deprecated
  public List<AutoTestModel> adaptersAutoTestsGet(UUID projectId, String externalId, Long globalId, String namespace, Boolean isNamespaceNull, Boolean includeEmptyNamespaces, String className, Boolean isClassnameNull, Boolean includeEmptyClassNames, Boolean isDeleted, Boolean deleted, List<String> labels, Integer stabilityMinimal, Integer minStability, Integer stabilityMaximal, Integer maxStability, Boolean isFlaky, Boolean flaky, Boolean includeSteps, Boolean includeLabels, String externalKey, Integer skip, Integer take, String orderBy, String searchField, String searchValue) throws ApiException {
    return adaptersAutoTestsGetWithHttpInfo(projectId, externalId, globalId, namespace, isNamespaceNull, includeEmptyNamespaces, className, isClassnameNull, includeEmptyClassNames, isDeleted, deleted, labels, stabilityMinimal, minStability, stabilityMaximal, maxStability, isFlaky, flaky, includeSteps, includeLabels, externalKey, skip, take, orderBy, searchField, searchValue).getData();
  }

  /**
   * 
   * 
   * @param projectId Project internal ID (optional)
   * @param externalId Autotest external ID (optional)
   * @param globalId Autotest global ID (optional)
   * @param namespace Namespace in which autotest is located (optional)
   * @param isNamespaceNull OBSOLETE: Use &#x60;includeEmptyNamespaces&#x60; instead (optional)
   * @param includeEmptyNamespaces If result must contain autotests without namespace (optional)
   * @param className Name of class in which autotest is located (optional)
   * @param isClassnameNull OBSOLETE: Use &#x60;includeEmptyClassNames&#x60; instead (optional)
   * @param includeEmptyClassNames If result must contain autotests without class (optional)
   * @param isDeleted OBSOLETE: Use &#x60;deleted&#x60; instead (optional)
   * @param deleted Is autotest deleted (optional)
   * @param labels Include only autotests with provided labels (optional)
   * @param stabilityMinimal OBSOLETE: Use &#x60;minStability&#x60; instead (optional)
   * @param minStability Minimum stability value of autotest (optional)
   * @param stabilityMaximal OBSOLETE: Use &#x60;maxStability&#x60; instead (optional)
   * @param maxStability Maximum stability value of autotest (optional)
   * @param isFlaky OBSOLETE: Use &#x60;flaky&#x60; instead (optional)
   * @param flaky Is autotest marked as \&quot;Flaky\&quot; (optional)
   * @param includeSteps If result must also include autotest steps (optional)
   * @param includeLabels If result must also include autotest labels (optional)
   * @param externalKey External key of autotest (optional)
   * @param skip Amount of items to be skipped (offset) (optional)
   * @param take Amount of items to be taken (limit) (optional)
   * @param orderBy SQL-like  ORDER BY statement (column1 ASC|DESC , column2 ASC|DESC) (optional)
   * @param searchField Property name for searching (optional)
   * @param searchValue Value for searching (optional)
   * @return ApiResponse&lt;List&lt;AutoTestModel&gt;&gt;
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 200 </td><td> OK </td><td>  * Pagination-Skip - Skipped amount of items <br>  * Pagination-Take - Taken items <br>  * Pagination-Pages - Expected number of pages <br>  * Pagination-Total-Items - Total count of items <br>  </td></tr>
       <tr><td> 400 </td><td> Bad Request </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Unauthorized </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Forbidden </td><td>  -  </td></tr>
       <tr><td> 404 </td><td> Not Found </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> Conflict </td><td>  -  </td></tr>
       <tr><td> 422 </td><td> Unprocessable Entity </td><td>  -  </td></tr>
     </table>
   * @deprecated
   */
  @Deprecated
  public ApiResponse<List<AutoTestModel>> adaptersAutoTestsGetWithHttpInfo(UUID projectId, String externalId, Long globalId, String namespace, Boolean isNamespaceNull, Boolean includeEmptyNamespaces, String className, Boolean isClassnameNull, Boolean includeEmptyClassNames, Boolean isDeleted, Boolean deleted, List<String> labels, Integer stabilityMinimal, Integer minStability, Integer stabilityMaximal, Integer maxStability, Boolean isFlaky, Boolean flaky, Boolean includeSteps, Boolean includeLabels, String externalKey, Integer skip, Integer take, String orderBy, String searchField, String searchValue) throws ApiException {
    // Query parameters
    List<Pair> localVarQueryParams = new ArrayList<>(
            apiClient.parameterToPairs("", "projectId", projectId)
    );
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "externalId", externalId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "globalId", globalId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "namespace", namespace));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "isNamespaceNull", isNamespaceNull));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "includeEmptyNamespaces", includeEmptyNamespaces));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "className", className));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "isClassnameNull", isClassnameNull));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "includeEmptyClassNames", includeEmptyClassNames));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "isDeleted", isDeleted));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "deleted", deleted));
    localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "labels", labels));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "stabilityMinimal", stabilityMinimal));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "minStability", minStability));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "stabilityMaximal", stabilityMaximal));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxStability", maxStability));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "isFlaky", isFlaky));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "flaky", flaky));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "includeSteps", includeSteps));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "includeLabels", includeLabels));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "externalKey", externalKey));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "Skip", skip));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "Take", take));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "OrderBy", orderBy));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "SearchField", searchField));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "SearchValue", searchValue));

    String localVarAccept = apiClient.selectHeaderAccept("application/json");
    String localVarContentType = apiClient.selectHeaderContentType();
    String[] localVarAuthNames = new String[] {"PrivateToken", "Cookies"};
    GenericType<List<AutoTestModel>> localVarReturnType = new GenericType<List<AutoTestModel>>() {};
    return apiClient.invokeAPI("AutoTestsApi.adaptersAutoTestsGet", "/adapters/autoTests", "GET", localVarQueryParams, null,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType,
                               localVarAuthNames, localVarReturnType, false);
  }
  /**
   * Get autotest by internal or global ID
   * 
   * @param id Internal (UUID) or global (integer) identifier (required)
   * @return AutoTestApiResult
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 200 </td><td> OK </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Bad Request </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Unauthorized </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Forbidden </td><td>  -  </td></tr>
       <tr><td> 404 </td><td> Not Found </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> Conflict </td><td>  -  </td></tr>
       <tr><td> 422 </td><td> Unprocessable Entity </td><td>  -  </td></tr>
     </table>
   */
  public AutoTestApiResult adaptersAutoTestsIdGet(String id) throws ApiException {
    return adaptersAutoTestsIdGetWithHttpInfo(id).getData();
  }

  /**
   * Get autotest by internal or global ID
   * 
   * @param id Internal (UUID) or global (integer) identifier (required)
   * @return ApiResponse&lt;AutoTestApiResult&gt;
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 200 </td><td> OK </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Bad Request </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Unauthorized </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Forbidden </td><td>  -  </td></tr>
       <tr><td> 404 </td><td> Not Found </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> Conflict </td><td>  -  </td></tr>
       <tr><td> 422 </td><td> Unprocessable Entity </td><td>  -  </td></tr>
     </table>
   */
  public ApiResponse<AutoTestApiResult> adaptersAutoTestsIdGetWithHttpInfo(String id) throws ApiException {
    // Check required parameters
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling adaptersAutoTestsIdGet");
    }

    // Path parameters
    String localVarPath = "/adapters/autoTests/{id}"
            .replaceAll("\\{id}", apiClient.escapeString(id.toString()));

    String localVarAccept = apiClient.selectHeaderAccept("application/json");
    String localVarContentType = apiClient.selectHeaderContentType();
    String[] localVarAuthNames = new String[] {"PrivateToken", "Cookies"};
    GenericType<AutoTestApiResult> localVarReturnType = new GenericType<AutoTestApiResult>() {};
    return apiClient.invokeAPI("AutoTestsApi.adaptersAutoTestsIdGet", localVarPath, "GET", new ArrayList<>(), null,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType,
                               localVarAuthNames, localVarReturnType, false);
  }
  /**
   * Patch auto test
   * See &lt;a href&#x3D;\&quot;https://www.rfc-editor.org/rfc/rfc6902\&quot; target&#x3D;\&quot;_blank\&quot;&gt;RFC 6902: JavaScript Object Notation (JSON) Patch&lt;/a&gt; for details
   * @param id  (required)
   * @param operation  (optional)
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 204 </td><td> No Content </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Bad Request </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Unauthorized </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Forbidden </td><td>  -  </td></tr>
       <tr><td> 404 </td><td> Not Found </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> Conflict </td><td>  -  </td></tr>
       <tr><td> 422 </td><td> Unprocessable Entity </td><td>  -  </td></tr>
     </table>
   */
  public void adaptersAutoTestsIdPatch(UUID id, List<Operation> operation) throws ApiException {
    adaptersAutoTestsIdPatchWithHttpInfo(id, operation);
  }

  /**
   * Patch auto test
   * See &lt;a href&#x3D;\&quot;https://www.rfc-editor.org/rfc/rfc6902\&quot; target&#x3D;\&quot;_blank\&quot;&gt;RFC 6902: JavaScript Object Notation (JSON) Patch&lt;/a&gt; for details
   * @param id  (required)
   * @param operation  (optional)
   * @return ApiResponse&lt;Void&gt;
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 204 </td><td> No Content </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Bad Request </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Unauthorized </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Forbidden </td><td>  -  </td></tr>
       <tr><td> 404 </td><td> Not Found </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> Conflict </td><td>  -  </td></tr>
       <tr><td> 422 </td><td> Unprocessable Entity </td><td>  -  </td></tr>
     </table>
   */
  public ApiResponse<Void> adaptersAutoTestsIdPatchWithHttpInfo(UUID id, List<Operation> operation) throws ApiException {
    // Check required parameters
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling adaptersAutoTestsIdPatch");
    }

    // Path parameters
    String localVarPath = "/adapters/autoTests/{id}"
            .replaceAll("\\{id}", apiClient.escapeString(id.toString()));

    String localVarAccept = apiClient.selectHeaderAccept("application/json");
    String localVarContentType = apiClient.selectHeaderContentType("application/json");
    String[] localVarAuthNames = new String[] {"PrivateToken", "Cookies"};
    return apiClient.invokeAPI("AutoTestsApi.adaptersAutoTestsIdPatch", localVarPath, "PATCH", new ArrayList<>(), operation,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType,
                               localVarAuthNames, null, false);
  }
  /**
   * Unlink autotest from work item
   * 
   * @param id Internal (UUID) or global (integer) identifier (required)
   * @param workItemId Internal (UUID) or global (integer) identifier (optional)
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 204 </td><td> No Content </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Bad Request </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Unauthorized </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Forbidden </td><td>  -  </td></tr>
       <tr><td> 404 </td><td> Not Found </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> Conflict </td><td>  -  </td></tr>
       <tr><td> 422 </td><td> Unprocessable Entity </td><td>  -  </td></tr>
     </table>
   */
  public void adaptersAutoTestsIdWorkItemsDelete(String id, String workItemId) throws ApiException {
    adaptersAutoTestsIdWorkItemsDeleteWithHttpInfo(id, workItemId);
  }

  /**
   * Unlink autotest from work item
   * 
   * @param id Internal (UUID) or global (integer) identifier (required)
   * @param workItemId Internal (UUID) or global (integer) identifier (optional)
   * @return ApiResponse&lt;Void&gt;
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 204 </td><td> No Content </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Bad Request </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Unauthorized </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Forbidden </td><td>  -  </td></tr>
       <tr><td> 404 </td><td> Not Found </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> Conflict </td><td>  -  </td></tr>
       <tr><td> 422 </td><td> Unprocessable Entity </td><td>  -  </td></tr>
     </table>
   */
  public ApiResponse<Void> adaptersAutoTestsIdWorkItemsDeleteWithHttpInfo(String id, String workItemId) throws ApiException {
    // Check required parameters
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling adaptersAutoTestsIdWorkItemsDelete");
    }

    // Path parameters
    String localVarPath = "/adapters/autoTests/{id}/work-items"
            .replaceAll("\\{id}", apiClient.escapeString(id.toString()));

    // Query parameters
    List<Pair> localVarQueryParams = new ArrayList<>(
            apiClient.parameterToPairs("", "workItemId", workItemId)
    );

    String localVarAccept = apiClient.selectHeaderAccept("application/json");
    String localVarContentType = apiClient.selectHeaderContentType();
    String[] localVarAuthNames = new String[] {"PrivateToken", "Cookies"};
    return apiClient.invokeAPI("AutoTestsApi.adaptersAutoTestsIdWorkItemsDelete", localVarPath, "DELETE", localVarQueryParams, null,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType,
                               localVarAuthNames, null, false);
  }
  /**
   * Get work items linked to autotest
   * 
   * @param id Internal (UUID) or global (integer) identifier (required)
   * @param isDeleted  (optional)
   * @param isWorkItemDeleted  (optional, default to false)
   * @return List&lt;AutoTestWorkItemIdentifierApiResult&gt;
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 200 </td><td> OK </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Bad Request </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Unauthorized </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Forbidden </td><td>  -  </td></tr>
       <tr><td> 404 </td><td> Not Found </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> Conflict </td><td>  -  </td></tr>
       <tr><td> 422 </td><td> Unprocessable Entity </td><td>  -  </td></tr>
     </table>
   */
  public List<AutoTestWorkItemIdentifierApiResult> adaptersAutoTestsIdWorkItemsGet(String id, Boolean isDeleted, Boolean isWorkItemDeleted) throws ApiException {
    return adaptersAutoTestsIdWorkItemsGetWithHttpInfo(id, isDeleted, isWorkItemDeleted).getData();
  }

  /**
   * Get work items linked to autotest
   * 
   * @param id Internal (UUID) or global (integer) identifier (required)
   * @param isDeleted  (optional)
   * @param isWorkItemDeleted  (optional, default to false)
   * @return ApiResponse&lt;List&lt;AutoTestWorkItemIdentifierApiResult&gt;&gt;
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 200 </td><td> OK </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Bad Request </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Unauthorized </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Forbidden </td><td>  -  </td></tr>
       <tr><td> 404 </td><td> Not Found </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> Conflict </td><td>  -  </td></tr>
       <tr><td> 422 </td><td> Unprocessable Entity </td><td>  -  </td></tr>
     </table>
   */
  public ApiResponse<List<AutoTestWorkItemIdentifierApiResult>> adaptersAutoTestsIdWorkItemsGetWithHttpInfo(String id, Boolean isDeleted, Boolean isWorkItemDeleted) throws ApiException {
    // Check required parameters
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling adaptersAutoTestsIdWorkItemsGet");
    }

    // Path parameters
    String localVarPath = "/adapters/autoTests/{id}/work-items"
            .replaceAll("\\{id}", apiClient.escapeString(id.toString()));

    // Query parameters
    List<Pair> localVarQueryParams = new ArrayList<>(
            apiClient.parameterToPairs("", "isDeleted", isDeleted)
    );
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "isWorkItemDeleted", isWorkItemDeleted));

    String localVarAccept = apiClient.selectHeaderAccept("application/json");
    String localVarContentType = apiClient.selectHeaderContentType();
    String[] localVarAuthNames = new String[] {"PrivateToken", "Cookies"};
    GenericType<List<AutoTestWorkItemIdentifierApiResult>> localVarReturnType = new GenericType<List<AutoTestWorkItemIdentifierApiResult>>() {};
    return apiClient.invokeAPI("AutoTestsApi.adaptersAutoTestsIdWorkItemsGet", localVarPath, "GET", localVarQueryParams, null,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType,
                               localVarAuthNames, localVarReturnType, false);
  }
  /**
   * Link autotest with work items
   * 
   * @param id Internal (UUID) or global (integer) identifier (required)
   * @param workItemIdApiModel  (optional)
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 204 </td><td> No Content </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Bad Request </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Unauthorized </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Forbidden </td><td>  -  </td></tr>
       <tr><td> 404 </td><td> Not Found </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> Conflict </td><td>  -  </td></tr>
       <tr><td> 422 </td><td> Unprocessable Entity </td><td>  -  </td></tr>
     </table>
   */
  public void adaptersAutoTestsIdWorkItemsPost(String id, WorkItemIdApiModel workItemIdApiModel) throws ApiException {
    adaptersAutoTestsIdWorkItemsPostWithHttpInfo(id, workItemIdApiModel);
  }

  /**
   * Link autotest with work items
   * 
   * @param id Internal (UUID) or global (integer) identifier (required)
   * @param workItemIdApiModel  (optional)
   * @return ApiResponse&lt;Void&gt;
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 204 </td><td> No Content </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Bad Request </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Unauthorized </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Forbidden </td><td>  -  </td></tr>
       <tr><td> 404 </td><td> Not Found </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> Conflict </td><td>  -  </td></tr>
       <tr><td> 422 </td><td> Unprocessable Entity </td><td>  -  </td></tr>
     </table>
   */
  public ApiResponse<Void> adaptersAutoTestsIdWorkItemsPostWithHttpInfo(String id, WorkItemIdApiModel workItemIdApiModel) throws ApiException {
    // Check required parameters
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling adaptersAutoTestsIdWorkItemsPost");
    }

    // Path parameters
    String localVarPath = "/adapters/autoTests/{id}/work-items"
            .replaceAll("\\{id}", apiClient.escapeString(id.toString()));

    String localVarAccept = apiClient.selectHeaderAccept("application/json");
    String localVarContentType = apiClient.selectHeaderContentType("application/json");
    String[] localVarAuthNames = new String[] {"PrivateToken", "Cookies"};
    return apiClient.invokeAPI("AutoTestsApi.adaptersAutoTestsIdWorkItemsPost", localVarPath, "POST", new ArrayList<>(), workItemIdApiModel,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType,
                               localVarAuthNames, null, false);
  }
  /**
   * Create autotest
   * 
   * @param autoTestCreateApiModel  (optional)
   * @return AutoTestApiResult
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 201 </td><td> Created </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Bad Request </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Unauthorized </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Forbidden </td><td>  -  </td></tr>
       <tr><td> 404 </td><td> Not Found </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> Conflict </td><td>  -  </td></tr>
       <tr><td> 422 </td><td> Unprocessable Entity </td><td>  -  </td></tr>
     </table>
   */
  public AutoTestApiResult adaptersAutoTestsPost(AutoTestCreateApiModel autoTestCreateApiModel) throws ApiException {
    return adaptersAutoTestsPostWithHttpInfo(autoTestCreateApiModel).getData();
  }

  /**
   * Create autotest
   * 
   * @param autoTestCreateApiModel  (optional)
   * @return ApiResponse&lt;AutoTestApiResult&gt;
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 201 </td><td> Created </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Bad Request </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Unauthorized </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Forbidden </td><td>  -  </td></tr>
       <tr><td> 404 </td><td> Not Found </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> Conflict </td><td>  -  </td></tr>
       <tr><td> 422 </td><td> Unprocessable Entity </td><td>  -  </td></tr>
     </table>
   */
  public ApiResponse<AutoTestApiResult> adaptersAutoTestsPostWithHttpInfo(AutoTestCreateApiModel autoTestCreateApiModel) throws ApiException {
    String localVarAccept = apiClient.selectHeaderAccept("application/json");
    String localVarContentType = apiClient.selectHeaderContentType("application/json");
    String[] localVarAuthNames = new String[] {"PrivateToken", "Cookies"};
    GenericType<AutoTestApiResult> localVarReturnType = new GenericType<AutoTestApiResult>() {};
    return apiClient.invokeAPI("AutoTestsApi.adaptersAutoTestsPost", "/adapters/autoTests", "POST", new ArrayList<>(), autoTestCreateApiModel,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType,
                               localVarAuthNames, localVarReturnType, false);
  }
  /**
   * Update autotest
   * 
   * @param autoTestUpdateApiModel  (optional)
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 200 </td><td> OK </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Bad Request </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Unauthorized </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Forbidden </td><td>  -  </td></tr>
       <tr><td> 404 </td><td> Not Found </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> Conflict </td><td>  -  </td></tr>
       <tr><td> 422 </td><td> Unprocessable Entity </td><td>  -  </td></tr>
     </table>
   */
  public void adaptersAutoTestsPut(AutoTestUpdateApiModel autoTestUpdateApiModel) throws ApiException {
    adaptersAutoTestsPutWithHttpInfo(autoTestUpdateApiModel);
  }

  /**
   * Update autotest
   * 
   * @param autoTestUpdateApiModel  (optional)
   * @return ApiResponse&lt;Void&gt;
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 200 </td><td> OK </td><td>  -  </td></tr>
       <tr><td> 400 </td><td> Bad Request </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Unauthorized </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Forbidden </td><td>  -  </td></tr>
       <tr><td> 404 </td><td> Not Found </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> Conflict </td><td>  -  </td></tr>
       <tr><td> 422 </td><td> Unprocessable Entity </td><td>  -  </td></tr>
     </table>
   */
  public ApiResponse<Void> adaptersAutoTestsPutWithHttpInfo(AutoTestUpdateApiModel autoTestUpdateApiModel) throws ApiException {
    String localVarAccept = apiClient.selectHeaderAccept("application/json");
    String localVarContentType = apiClient.selectHeaderContentType("application/json");
    String[] localVarAuthNames = new String[] {"PrivateToken", "Cookies"};
    return apiClient.invokeAPI("AutoTestsApi.adaptersAutoTestsPut", "/adapters/autoTests", "PUT", new ArrayList<>(), autoTestUpdateApiModel,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType,
                               localVarAuthNames, null, false);
  }
  /**
   * Search for autotests
   * 
   * @param skip Amount of items to be skipped (offset) (optional)
   * @param take Amount of items to be taken (limit) (optional)
   * @param orderBy SQL-like  ORDER BY statement (column1 ASC|DESC , column2 ASC|DESC) (optional)
   * @param searchField Property name for searching (optional)
   * @param searchValue Value for searching (optional)
   * @param autoTestSearchApiModel  (optional)
   * @return List&lt;AutoTestApiResult&gt;
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 200 </td><td> OK </td><td>  * Pagination-Skip - Skipped amount of items <br>  * Pagination-Take - Taken items <br>  * Pagination-Pages - Expected number of pages <br>  * Pagination-Total-Items - Total count of items <br>  </td></tr>
       <tr><td> 400 </td><td> Bad Request </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Unauthorized </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Read permission for autotests library is required </td><td>  -  </td></tr>
       <tr><td> 404 </td><td> Not Found </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> Conflict </td><td>  -  </td></tr>
       <tr><td> 422 </td><td> Unprocessable Entity </td><td>  -  </td></tr>
     </table>
   */
  public List<AutoTestApiResult> adaptersAutoTestsSearchPost(Integer skip, Integer take, String orderBy, String searchField, String searchValue, AutoTestSearchApiModel autoTestSearchApiModel) throws ApiException {
    return adaptersAutoTestsSearchPostWithHttpInfo(skip, take, orderBy, searchField, searchValue, autoTestSearchApiModel).getData();
  }

  /**
   * Search for autotests
   * 
   * @param skip Amount of items to be skipped (offset) (optional)
   * @param take Amount of items to be taken (limit) (optional)
   * @param orderBy SQL-like  ORDER BY statement (column1 ASC|DESC , column2 ASC|DESC) (optional)
   * @param searchField Property name for searching (optional)
   * @param searchValue Value for searching (optional)
   * @param autoTestSearchApiModel  (optional)
   * @return ApiResponse&lt;List&lt;AutoTestApiResult&gt;&gt;
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 200 </td><td> OK </td><td>  * Pagination-Skip - Skipped amount of items <br>  * Pagination-Take - Taken items <br>  * Pagination-Pages - Expected number of pages <br>  * Pagination-Total-Items - Total count of items <br>  </td></tr>
       <tr><td> 400 </td><td> Bad Request </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Unauthorized </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Read permission for autotests library is required </td><td>  -  </td></tr>
       <tr><td> 404 </td><td> Not Found </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> Conflict </td><td>  -  </td></tr>
       <tr><td> 422 </td><td> Unprocessable Entity </td><td>  -  </td></tr>
     </table>
   */
  public ApiResponse<List<AutoTestApiResult>> adaptersAutoTestsSearchPostWithHttpInfo(Integer skip, Integer take, String orderBy, String searchField, String searchValue, AutoTestSearchApiModel autoTestSearchApiModel) throws ApiException {
    // Query parameters
    List<Pair> localVarQueryParams = new ArrayList<>(
            apiClient.parameterToPairs("", "Skip", skip)
    );
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "Take", take));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "OrderBy", orderBy));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "SearchField", searchField));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "SearchValue", searchValue));

    String localVarAccept = apiClient.selectHeaderAccept("application/json");
    String localVarContentType = apiClient.selectHeaderContentType("application/json");
    String[] localVarAuthNames = new String[] {"PrivateToken", "Cookies"};
    GenericType<List<AutoTestApiResult>> localVarReturnType = new GenericType<List<AutoTestApiResult>>() {};
    return apiClient.invokeAPI("AutoTestsApi.adaptersAutoTestsSearchPost", "/adapters/autoTests/search", "POST", localVarQueryParams, autoTestSearchApiModel,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType,
                               localVarAuthNames, localVarReturnType, false);
  }
}
