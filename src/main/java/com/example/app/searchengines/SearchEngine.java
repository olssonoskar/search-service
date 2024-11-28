package com.example.app.searchengines;

import com.example.app.responses.SearchResult;

import java.util.List;

public interface SearchEngine {

    SearchResult search(List<String> words);

}
