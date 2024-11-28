package com.example.app.searchengines;

import com.example.app.HttpClient;
import com.example.app.config.GoogleConfig;
import com.example.app.responses.SearchResult;
import com.example.app.responses.google.GoogleResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class GoogleSearch implements SearchEngine {

    private final Logger log = LoggerFactory.getLogger(GoogleSearch.class);
    private final GoogleConfig config;
    private final HttpClient client;

    private static final String KEY = "key";
    private static final String CX = "cx";
    private static final String QUERY = "q";
    private static final String ZERO = "0";

    @Autowired
    public GoogleSearch(GoogleConfig config, HttpClient client) {
        this.config = config;
        this.client = client;
    }

    @Override
    public SearchResult searchResults(List<String> words) {
        var hits = words.parallelStream()
                .map(this::searchEach)
                .map(res -> res.map(it -> it.searchInformation().totalResults()).orElse(ZERO))
                .map(this::parseHits)
                .reduce(Long::sum)
                .orElse(0L);
        return new SearchResult("Google", hits);
    }

    private Optional<GoogleResponse> searchEach(String word) {
        var params = Map.of(KEY, config.getKey(), CX, config.getCx(), QUERY, word);
        return client.getWithQueryParams(config.getPath(), params)
                .bodyToMono(GoogleResponse.class)
                .onErrorResume(e -> {
                    log.error("Failed to query Google: {}", e.getMessage());
                    return Mono.empty();
                })
                .blockOptional();
    }

    private long parseHits(String hits) {
        try {
            return Long.parseLong(hits);
        } catch (NumberFormatException ex) {
            log.error("Google responded with unexpected data for hits", ex);
            return 0L;
        }
    }
}
