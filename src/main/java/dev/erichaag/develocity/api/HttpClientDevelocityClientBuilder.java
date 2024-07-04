package dev.erichaag.develocity.api;

import java.net.URI;
import java.net.http.HttpClient;

import static dev.erichaag.develocity.api.AccessKeyProvider.lookupAccessKey;

public final class HttpClientDevelocityClientBuilder {

    private final URI serverUrl;
    private final HttpClient.Builder httpClientBuilder = HttpClient.newBuilder();

    private boolean useAnonymousAccess = false;

    HttpClientDevelocityClientBuilder(URI serverUrl) {
        this.serverUrl = serverUrl;
    }

    public HttpClientDevelocityClient build() {
        final var accessKey = useAnonymousAccess ? null : lookupAccessKey(serverUrl);
        return new HttpClientDevelocityClient(serverUrl, accessKey, httpClientBuilder.build());
    }

    public HttpClientDevelocityClientBuilder withAnonymousAccess() {
        this.useAnonymousAccess = true;
        return this;
    }

    public HttpClientDevelocityClientBuilder followingRedirects() {
        this.httpClientBuilder.followRedirects(HttpClient.Redirect.ALWAYS);
        return this;
    }

}
