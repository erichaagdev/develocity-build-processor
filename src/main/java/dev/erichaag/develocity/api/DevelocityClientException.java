package dev.erichaag.develocity.api;

import java.net.URI;
import java.util.List;
import java.util.Map;

public final class DevelocityClientException extends RuntimeException {

    private final URI serverUrl;
    private final int statusCode;
    private final Map<String, List<String>> headers;

    public DevelocityClientException(URI serverUrl, int statusCode, Map<String, List<String>> headers) {
        super("Received response code " + statusCode + " from " + serverUrl);
        this.serverUrl = serverUrl;
        this.statusCode = statusCode;
        this.headers = headers;
    }

    public URI getServerUrl() {
        return serverUrl;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

}
