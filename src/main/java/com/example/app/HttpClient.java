package com.example.app;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class HttpClient {

    private final WebClient client;

    public HttpClient(WebClient client) {
        this.client = client;
    }

    public WebClient.ResponseSpec getWithQueryParams(String path, Map<String, String> params) {
        return client.get()
                .uri(path, uri -> {
                    params.forEach(uri::queryParam);
                    return uri.build();
                }).retrieve();
    }

    public WebClient.ResponseSpec getWithHeader(String path, String query, String header, String value) {
        return client.get()
                .uri(path, uriBuilder -> uriBuilder.queryParam("q", query).build())
                .header(header, value)
                .retrieve();
    }
}
