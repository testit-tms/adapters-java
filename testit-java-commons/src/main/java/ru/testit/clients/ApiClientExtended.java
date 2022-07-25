package ru.testit.clients;

import jakarta.ws.rs.core.GenericType;
import ru.testit.client.invoker.ApiException;
import ru.testit.client.invoker.ApiResponse;
import ru.testit.client.invoker.Pair;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class ApiClientExtended extends ru.testit.client.invoker.ApiClient {
    protected Random random = new Random();
    protected String multipartBoundary = "";

    public ApiClientExtended() {
        super();
    }

    @Override
    public String serializeToString(Object obj, Map<String, Object> formParams, String contentType, boolean isBodyNullable) throws
            ApiException {
        final String sep = "\r\n";
        final String disposition = "Content-Disposition: form-data; name=\"$\"";
        final String dispositionFile = "Content-Disposition: form-data; name=\"$\"; filename=\"@\"";
        if (!contentType.startsWith("multipart/form-data"))
            return super.serializeToString(obj, formParams, contentType, isBodyNullable);
        StringBuilder s = new StringBuilder();
        formParams.entrySet().forEach(e -> {
            String name = e.getKey()
                    .replace("&", "ampersand");
            try {
                s.append(sep).append(multipartBoundary).append(sep);
                if (e.getValue() instanceof File) {
                    File f = (File) e.getValue();
                    s.append(dispositionFile
                                    .replace("$", name)
                                    .replace("@", f.getName().replace("\"", "")))
                            .append(sep).append(sep);
                    byte[] bytes = Files.readAllBytes(f.toPath());
                    s.append(bytes);
                } else {
                    s.append(disposition
                                    .replace("$", name))
                            .append(sep).append(sep);
                    s.append(e.getValue());
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex.getMessage());
            }
        });
        return s.toString();
    }

    @Override
    public <T> ApiResponse<T> invokeAPI(String operation, String path, String method, List<Pair> queryParams, Object body, Map<String, String> headerParams, Map<String, String> cookieParams, Map<String, Object> formParams, String accept, String contentType, String[] authNames, GenericType<T> returnType, boolean isBodyNullable) throws ApiException {
        if (Objects.equals(contentType, "multipart/form-data")) {
            multipartBoundary = "-----abcdefg" + random.nextInt(9_999_9999);
            contentType = contentType + "; boundary=" + multipartBoundary;
        }
        return super.invokeAPI(operation, path, method, queryParams, body, headerParams, cookieParams, formParams, accept, contentType, authNames, returnType, isBodyNullable);
    }
}

