package com.example.app;

import com.example.app.responses.Summary;
import com.example.app.responses.bing.BingResponse;
import com.example.app.responses.google.GoogleResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

import static com.example.app.ResponseUtils.bingResponse;
import static com.example.app.ResponseUtils.googleResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

// Web tests that ensure application starts and runs everything excluding the WebClient Http call to Google/Bing
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebTest {

    private static final String LOCALHOST = "http://localhost:";

    @LocalServerPort
    private int port;

    @MockitoBean
    HttpClient client;

    @Autowired
    private TestRestTemplate testRestTemplate;


    @Test
    void successfulSearchTest() {
        WebClient.ResponseSpec googleResp = Mockito.mock(WebClient.ResponseSpec.class);
        when(client.getWithQueryParams(anyString(), anyMap())).thenReturn(googleResp);
        when(googleResp.bodyToMono(GoogleResponse.class)).thenReturn(googleResponse("6"));

        WebClient.ResponseSpec bingResp = Mockito.mock(WebClient.ResponseSpec.class);
        when(client.getWithHeader(anyString(), anyString(), anyString(), anyString())).thenReturn(bingResp);
        when(bingResp.bodyToMono(BingResponse.class)).thenReturn(bingResponse(4L));

        var result = testRestTemplate.getForEntity(LOCALHOST + port + "/search?q=hello", Summary.class);

        assertEquals(HttpStatusCode.valueOf(200), result.getStatusCode());
        var body = Optional.ofNullable(result.getBody()).orElse(new Summary(-1L, null));
        assertEquals(10, body.totalHits());
        assertEquals(2, body.searchResults().size());
    }

    @Test
    void emptySearchTest() {
        var result = testRestTemplate.getForEntity(LOCALHOST + port + "/search?q=    ", Summary.class);

        assertEquals(HttpStatusCode.valueOf(200), result.getStatusCode());
        var body = Optional.ofNullable(result.getBody()).orElse(new Summary(-1L, null));
        assertEquals(0, body.totalHits());
        assertEquals(0, body.searchResults().size());
    }

    @Test
    void missingQueryTest() {
        var result = testRestTemplate.getForEntity(LOCALHOST + port + "/search", String.class);
        assertEquals(HttpStatusCode.valueOf(400), result.getStatusCode());
    }

}
