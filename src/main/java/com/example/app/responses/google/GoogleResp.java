package com.example.app.responses.google;

public record GoogleResp(
        String kind,
        Url url,
        SearchInformation searchInformation
) { }
