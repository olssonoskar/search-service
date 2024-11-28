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

/**
 * Implementation for Google search
 * Takes a list of words and sends a search request for each one
 * Converts JSON response to data class and extracts the 'hits' returned as a sum
 */
@Component
public class GoogleSearch implements SearchEngine {

    private final Logger log = LoggerFactory.getLogger(GoogleSearch.class);
    private static final String KEY = "key";
    private static final String CX = "cx";
    private static final String QUERY = "q";
    private static final String ZERO = "0";
    
    private final GoogleConfig config;
    private final HttpClient client;

    @Autowired
    public GoogleSearch(GoogleConfig config, HttpClient client) {
        this.config = config;
        this.client = client;
    }

    @Override
    public SearchResult search(List<String> words) {
        var hits = words.parallelStream()
                .map(this::searchEach)
                .map(opt -> opt.map(res -> res.searchInformation().totalResults()).orElse(ZERO))
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
                }).blockOptional();
    }

    private long parseHits(String hits) {
        try {
            return Long.parseLong(hits);
        } catch (NumberFormatException ex) {
            log.error("Google responded with unexpected data for hits: {}", ex.getMessage());
            return 0L;
        }
    }
}
