package com.example.app.searchengines;

import com.example.app.HttpClient;
import com.example.app.config.BingConfig;
import com.example.app.responses.SearchResult;
import com.example.app.responses.bing.BingResponse;
import com.example.app.responses.bing.WebPages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class BingSearchTests {

    private final WebClient.ResponseSpec responseSpec = Mockito.mock(WebClient.ResponseSpec.class);

    private final BingConfig config = Mockito.mock(BingConfig.class);
    private final HttpClient client = Mockito.mock(HttpClient.class);

    private final BingResponse response = new BingResponse("bing", new WebPages("url", 50L));
    private final BingResponse response2 = new BingResponse("bing", new WebPages("url", 100L));

    private final String header = "Ocp-Apim-Subscription-Key";
    private final String path = "Path";
    private final String key = "KEY";

    private final BingSearch bingSearch = new BingSearch(config, client);

    @BeforeEach
    void setup() {
        when(config.getPath()).thenReturn(path);
        when(config.getKey()).thenReturn(key);
    }

    @Test
    void searchTest() {
        when(client.getWithHeader(path, "test", header, key)).thenReturn(responseSpec);
        when(client.getWithHeader(path, "test2", header, key)).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(BingResponse.class))
                .thenReturn(Mono.just(response))
                .thenReturn(Mono.just(response2));

        var query = List.of("test", "test2");
        var result = bingSearch.searchResults(query);

        verify(client, times(2)).getWithHeader(anyString(), anyString(), anyString(), anyString());
        assertEquals(new SearchResult("Bing", 150L), result);
    }

    @Test
    void searchFailureTest() {
        when(client.getWithHeader(path, "ops", header, key)).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(BingResponse.class)).thenReturn(Mono.error(new RuntimeException("Response failed")));

        var query = List.of("ops");
        var result = bingSearch.searchResults(query);

        verify(client, times(1)).getWithHeader(anyString(), anyString(), anyString(), anyString());
        assertEquals(new SearchResult("Bing", 0L), result);
    }
}
