package ru.testit.adaptersapi.api;

import ru.testit.adaptersapi.invoker.ApiException;
import ru.testit.adaptersapi.invoker.ApiClient;
import ru.testit.adaptersapi.invoker.ApiResponse;
import ru.testit.adaptersapi.invoker.Configuration;
import ru.testit.adaptersapi.invoker.Pair;

import jakarta.ws.rs.core.GenericType;

import ru.testit.adaptersapi.model.AttachmentModel;
import java.io.File;
import ru.testit.adaptersapi.model.ImageResizeType;
import ru.testit.adaptersapi.model.ProblemDetails;
import java.util.UUID;
import ru.testit.adaptersapi.model.ValidationProblemDetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.11.0")
public class AttachmentsApi {
  private ApiClient apiClient;

  public AttachmentsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public AttachmentsApi(ApiClient apiClient) {
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
   * 
   * 
   * @param files  (optional)
   * @return List&lt;AttachmentModel&gt;
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
  public List<AttachmentModel> adaptersAttachmentsBulkPost(List<File> files) throws ApiException {
    return adaptersAttachmentsBulkPostWithHttpInfo(files).getData();
  }

  /**
   * 
   * 
   * @param files  (optional)
   * @return ApiResponse&lt;List&lt;AttachmentModel&gt;&gt;
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
  public ApiResponse<List<AttachmentModel>> adaptersAttachmentsBulkPostWithHttpInfo(List<File> files) throws ApiException {
    // Form parameters
    Map<String, Object> localVarFormParams = new LinkedHashMap<>();
    if (files != null) {
      localVarFormParams.put("files", files);
    }

    String localVarAccept = apiClient.selectHeaderAccept("application/json");
    String localVarContentType = apiClient.selectHeaderContentType("multipart/form-data");
    String[] localVarAuthNames = new String[] {"PrivateToken", "Cookies"};
    GenericType<List<AttachmentModel>> localVarReturnType = new GenericType<List<AttachmentModel>>() {};
    return apiClient.invokeAPI("AttachmentsApi.adaptersAttachmentsBulkPost", "/adapters/attachments/bulk", "POST", new ArrayList<>(), null,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), localVarFormParams, localVarAccept, localVarContentType,
                               localVarAuthNames, localVarReturnType, false);
  }
  /**
   * Download attachment file
   * 
   * @param id  (required)
   * @param width Width of the result image (optional)
   * @param height Height of the result image (optional)
   * @param resizeType Type of resizing to apply to the result image (optional)
   * @param backgroundColor Color of the background if the &#x60;resizeType&#x60; is &#x60;AddBackgroundStripes&#x60; (optional)
   * @param preview If image must be converted to a preview (lower quality, no animation) (optional)
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
  public void adaptersAttachmentsIdGet(UUID id, Integer width, Integer height, ImageResizeType resizeType, String backgroundColor, Boolean preview) throws ApiException {
    adaptersAttachmentsIdGetWithHttpInfo(id, width, height, resizeType, backgroundColor, preview);
  }

  /**
   * Download attachment file
   * 
   * @param id  (required)
   * @param width Width of the result image (optional)
   * @param height Height of the result image (optional)
   * @param resizeType Type of resizing to apply to the result image (optional)
   * @param backgroundColor Color of the background if the &#x60;resizeType&#x60; is &#x60;AddBackgroundStripes&#x60; (optional)
   * @param preview If image must be converted to a preview (lower quality, no animation) (optional)
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
  public ApiResponse<Void> adaptersAttachmentsIdGetWithHttpInfo(UUID id, Integer width, Integer height, ImageResizeType resizeType, String backgroundColor, Boolean preview) throws ApiException {
    // Check required parameters
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling adaptersAttachmentsIdGet");
    }

    // Path parameters
    String localVarPath = "/adapters/attachments/{id}"
            .replaceAll("\\{id}", apiClient.escapeString(id.toString()));

    // Query parameters
    List<Pair> localVarQueryParams = new ArrayList<>(
            apiClient.parameterToPairs("", "width", width)
    );
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "height", height));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "resizeType", resizeType));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "backgroundColor", backgroundColor));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "preview", preview));

    String localVarAccept = apiClient.selectHeaderAccept("application/json");
    String localVarContentType = apiClient.selectHeaderContentType();
    String[] localVarAuthNames = new String[] {"PrivateToken", "Cookies"};
    return apiClient.invokeAPI("AttachmentsApi.adaptersAttachmentsIdGet", localVarPath, "GET", localVarQueryParams, null,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType,
                               localVarAuthNames, null, false);
  }
  /**
   * Upload new attachment file
   * File size is restricted to 1 GB (1 073 741 824 bytes)
   * @param _file  (optional)
   * @return AttachmentModel
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 201 </td><td> Created </td><td>  -  </td></tr>
       <tr><td> 400 </td><td>   - Invalid file contents    - Invalid HTTP headers </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Unauthorized </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Forbidden </td><td>  -  </td></tr>
       <tr><td> 404 </td><td> Not Found </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> Conflict </td><td>  -  </td></tr>
       <tr><td> 422 </td><td> Unprocessable Entity </td><td>  -  </td></tr>
     </table>
   */
  public AttachmentModel adaptersAttachmentsPost(File _file) throws ApiException {
    return adaptersAttachmentsPostWithHttpInfo(_file).getData();
  }

  /**
   * Upload new attachment file
   * File size is restricted to 1 GB (1 073 741 824 bytes)
   * @param _file  (optional)
   * @return ApiResponse&lt;AttachmentModel&gt;
   * @throws ApiException if fails to make API call
   * @http.response.details
     <table border="1">
       <caption>Response Details</caption>
       <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
       <tr><td> 201 </td><td> Created </td><td>  -  </td></tr>
       <tr><td> 400 </td><td>   - Invalid file contents    - Invalid HTTP headers </td><td>  -  </td></tr>
       <tr><td> 401 </td><td> Unauthorized </td><td>  -  </td></tr>
       <tr><td> 403 </td><td> Forbidden </td><td>  -  </td></tr>
       <tr><td> 404 </td><td> Not Found </td><td>  -  </td></tr>
       <tr><td> 409 </td><td> Conflict </td><td>  -  </td></tr>
       <tr><td> 422 </td><td> Unprocessable Entity </td><td>  -  </td></tr>
     </table>
   */
  public ApiResponse<AttachmentModel> adaptersAttachmentsPostWithHttpInfo(File _file) throws ApiException {
    // Form parameters
    Map<String, Object> localVarFormParams = new LinkedHashMap<>();
    if (_file != null) {
      localVarFormParams.put("file", _file);
    }

    String localVarAccept = apiClient.selectHeaderAccept("application/json");
    String localVarContentType = apiClient.selectHeaderContentType("multipart/form-data");
    String[] localVarAuthNames = new String[] {"PrivateToken", "Cookies"};
    GenericType<AttachmentModel> localVarReturnType = new GenericType<AttachmentModel>() {};
    return apiClient.invokeAPI("AttachmentsApi.adaptersAttachmentsPost", "/adapters/attachments", "POST", new ArrayList<>(), null,
                               new LinkedHashMap<>(), new LinkedHashMap<>(), localVarFormParams, localVarAccept, localVarContentType,
                               localVarAuthNames, localVarReturnType, false);
  }
}
