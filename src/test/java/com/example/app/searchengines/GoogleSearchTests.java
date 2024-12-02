package com.example.app.searchengines;

import com.example.app.HttpClient;
import com.example.app.config.GoogleConfig;
import com.example.app.responses.SearchResult;
import com.example.app.responses.google.GoogleResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.example.app.ResponseUtils.googleResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GoogleSearchTests {

    private final WebClient.ResponseSpec responseSpec = Mockito.mock(WebClient.ResponseSpec.class);

    private final GoogleConfig config = Mockito.mock(GoogleConfig.class);
    private final HttpClient client = Mockito.mock(HttpClient.class);

    private final String path = "Path";
    private final String key = "KEY";
    private final String cx = "CX";

    private final GoogleSearch googleSearch = new GoogleSearch(config, client);

    @BeforeEach
    void setup() {
        when(config.getPath()).thenReturn(path);
        when(config.getKey()).thenReturn(key);
        when(config.getCx()).thenReturn(cx);
    }

    @Test
    void searchTest() {
        when(client.getWithQueryParams(anyString(), anyMap())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(GoogleResponse.class))
                .thenReturn(googleResponse("100"))
                .thenReturn(googleResponse("1000"));

        var query = List.of("test", "test2");
        var result = googleSearch.search(query);

        verify(client, times(2)).getWithQueryParams(anyString(), anyMap());
        assertEquals(new SearchResult("Google", 1100L), result);
    }

    @Test
    void searchFailureTest() {
        when(client.getWithQueryParams(anyString(), anyMap())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(GoogleResponse.class)).thenReturn(Mono.error(new RuntimeException("Response failed")));

        var query = List.of("ops");
        var result = googleSearch.search(query);

        verify(client, times(1)).getWithQueryParams(anyString(), anyMap());
        assertEquals(new SearchResult("Google", 0L), result);
    }

    @Test
    void handleParseFailureTest() {
        when(client.getWithQueryParams(anyString(), anyMap())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(GoogleResponse.class)).thenReturn(googleResponse("NaN"));

        var query = List.of("ops");
        var result = googleSearch.search(query);

        verify(client, times(1)).getWithQueryParams(anyString(), anyMap());
        assertEquals(new SearchResult("Google", 0L), result);
    }
}
