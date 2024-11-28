package com.example.app;

import com.example.app.responses.SearchResult;
import com.example.app.responses.Summary;
import com.example.app.searchengines.SearchEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RestController
public class SearchService {

    private final Logger log = LoggerFactory.getLogger(SearchService.class);
    private final List<SearchEngine> engines;

    @Autowired
    public SearchService(List<SearchEngine> engines) {
        this.engines = engines;
    }

    @GetMapping("/search")
    public Summary search(@RequestParam String q) {
        if (q.isBlank()) {
            return new Summary(0, Collections.emptyList());
        }
        log.info("Processing search request for: {} ...", q);
        var searchResults = engines.stream()
                .map(engine -> engine.searchResults(words(q)))
                .toList();

        log.debug("Finished");
        return new Summary(sumHits(searchResults), searchResults);
    }

    private Long sumHits(List<SearchResult> res) {
        return res.stream()
                .map(SearchResult::hits)
                .reduce(Long::sum)
                .orElse(0L);
    }

    private List<String> words(String query) {
        return Arrays.stream(query.trim().split(" "))
                .filter(word -> !word.isBlank())
                .toList();
    }

}
