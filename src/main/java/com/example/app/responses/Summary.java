package com.example.app.responses;

import java.util.List;

public record Summary(long totalHits, List<SearchResult> searchResults) { }
