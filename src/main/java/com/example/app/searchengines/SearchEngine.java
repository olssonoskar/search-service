package com.example.app.searchengines;

import com.example.app.responses.SearchResult;

import java.util.List;

public interface SearchEngine {

    SearchResult searchResults(List<String> words);

}
