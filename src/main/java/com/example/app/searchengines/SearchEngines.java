package com.example.app.searchengines;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Bean containing the supported search engines
 * Enables us to add new engines in the current flow
 */
@Component
public class SearchEngines {

    private final List<SearchEngine> engines;

    @Autowired
    public SearchEngines(GoogleSearch googleSearch, BingSearch bingSearch) {
        engines = List.of(googleSearch, bingSearch);
    }

    public List<SearchEngine> engines() {
        return engines;
    }

}
