package com.example.app.responses.google;

public record GoogleResponse(
        String kind,
        Url url,
        SearchInformation searchInformation
) { }
