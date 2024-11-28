package com.example.app.searchengines;

import com.example.app.config.GoogleConfig;
import com.example.app.responses.SearchResult;
import com.example.app.responses.google.GoogleResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Component
public class GoogleSearch implements SearchEngine {

    private final Logger log = LoggerFactory.getLogger(GoogleSearch.class);
    private final GoogleConfig config;
    private final WebClient client;

    @Autowired
    public GoogleSearch(WebClient client, GoogleConfig config) {
        this.client = client;
        this.config = config;
    }

    @Override
    public SearchResult searchResults(List<String> words) {
        var hits = words.parallelStream()
                .map(this::searchEach)
                .map(res -> res.map(it -> it.searchInformation().totalResults()).orElse("0"))
                .map(Long::parseLong)
                .reduce(Long::sum)
                .orElse(0L);
        return new SearchResult("Google", hits);
    }

    private Optional<GoogleResp> searchEach(String word) {
        return client.get()
                .uri(config.getPath(), uri -> uri
                        .queryParam("key", config.getKey())
                        .queryParam("cx", config.getCx())
                        .queryParam("q", word).build())
                .retrieve()
                .bodyToMono(GoogleResp.class)
                .onErrorResume(e -> {
                    log.error("Failed to query Google: {}", e.getMessage());
                    return Mono.empty();
                })
                .blockOptional();
    }
}
