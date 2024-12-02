package com.example.app;

import com.example.app.responses.bing.BingResponse;
import com.example.app.responses.bing.WebPages;
import com.example.app.responses.google.GoogleResponse;
import com.example.app.responses.google.SearchInformation;
import com.example.app.responses.google.Url;
import reactor.core.publisher.Mono;

public class ResponseUtils {

    public static Mono<GoogleResponse> googleResponse(String results) {
        return Mono.just(new GoogleResponse("search", new Url("url", "any"),
                new SearchInformation(0, "0", results, "")));
    }

    public static Mono<BingResponse> bingResponse(long results) {
        return Mono.just(new BingResponse("bing", new WebPages("url", results)));
    }

}
