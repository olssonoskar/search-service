package com.example.app;

import com.example.app.responses.SearchResult;
import com.example.app.responses.Summary;
import com.example.app.searchengines.SearchEngine;
import com.example.app.searchengines.SearchEngines;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Controller handling the GET request for /search
 * Provides some input sanitation such as removing duplicates elements and spaces
 * Splits the query into words and calls the search for each engine
 */
@RestController
public class SearchController {

    private final Logger log = LoggerFactory.getLogger(SearchController.class);
    private final List<SearchEngine> engines;

    @Autowired
    public SearchController(SearchEngines engines) {
        this.engines = engines.getEngines();
    }

    @GetMapping("/search")
    public Summary search(@RequestParam String q) {
        if (q.isBlank()) {
            return new Summary(0, Collections.emptyList());
        }
        var queryWords = toWords(q);
        log.info("Processing search request for: {} ...", queryWords);
        var searchResults = engines.stream()
                .map(engine -> engine.search(queryWords))
                .toList();
        log.debug("Finished");
        return new Summary(summarizeHits(searchResults), searchResults);
    }

    private Long summarizeHits(List<SearchResult> res) {
        return res.stream()
                .map(SearchResult::hits)
                .reduce(Long::sum)
                .orElse(0L);
    }

    private List<String> toWords(String query) {
        return Arrays.stream(query.trim().split(" "))
                .filter(word -> !word.isBlank())
                .distinct()
                .toList();
    }

}
