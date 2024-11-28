package com.example.app.searchengines;

import com.example.app.HttpClient;
import com.example.app.config.BingConfig;
import com.example.app.responses.SearchResult;
import com.example.app.responses.bing.BingResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Component
public class BingSearch implements SearchEngine{

    private final Logger log = LoggerFactory.getLogger(BingSearch.class);
    private final BingConfig config;
    private final HttpClient client;

    private static final String KEY_HEADER = "Ocp-Apim-Subscription-Key";

    @Autowired
    public BingSearch(BingConfig bingConfig, HttpClient client){
        this.config = bingConfig;
        this.client = client;
    }

    @Override
    public SearchResult searchResults(List<String> words) {
        var hits = words.parallelStream()
                .map(this::searchEach)
                .map(res -> res.map(it -> it.webPages().totalEstimatedMatches()).orElse(0L))
                .reduce(Long::sum)
                .orElse(0L);
        return new SearchResult("Bing", hits);
    }

    private Optional<BingResponse> searchEach(String word) {
        return client.getWithHeader(config.getPath(), word, KEY_HEADER, config.getKey())
                .bodyToMono(BingResponse.class)
                .onErrorResume(e -> {
                    log.error("Failed to query Bing: {}", e.getMessage());
                    return Mono.empty();
                })
                .blockOptional();
    }
}
