package com.example.app.responses.google;

public record SearchInformation(
        int searchTime,
        String formattedSearchTime,
        String totalResults,
        String formattedTotalResults
) { }