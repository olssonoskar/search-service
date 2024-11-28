package com.example.app;

import com.example.app.responses.SearchResult;
import com.example.app.responses.Summary;
import com.example.app.searchengines.BingSearch;
import com.example.app.searchengines.GoogleSearch;
import com.example.app.searchengines.SearchEngines;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

class SearchControllerTest {

    GoogleSearch googleSearch = Mockito.mock(GoogleSearch.class);
    BingSearch bingSearch = Mockito.mock(BingSearch.class);

    private final SearchController searchController = new SearchController(new SearchEngines(googleSearch, bingSearch));

    @Test
    void testValidResponse() {
        var googleRes = new SearchResult("Google", 10L);
        var bingRes = new SearchResult("Bing", 20L);
        when(googleSearch.searchResults(anyList())).thenReturn(googleRes);
        when(bingSearch.searchResults(anyList())).thenReturn(bingRes);

        var result = searchController.search("query");

        assertEquals(new Summary(30, List.of(googleRes, bingRes)), result);
    }

    @Test
    void testEmptyRequest() {
        var result = searchController.search("   ");

        assertEquals(new Summary(0, Collections.emptyList()), result);
    }

    @Test
    void testStrangeQuery() {
        var googleRes = new SearchResult("Google", 1L);
        var bingRes = new SearchResult("Bing", 1L);
        var expectedQuery = List.of("@asd", "--ds", "zax");
        when(googleSearch.searchResults(expectedQuery)).thenReturn(googleRes);
        when(bingSearch.searchResults(expectedQuery)).thenReturn(bingRes);

        var result = searchController.search("  @asd --ds     zax    ");

        assertEquals(new Summary(2L, List.of(googleRes, bingRes)), result);
    }

    @Test
    void testRemoveDuplicates() {
        var googleRes = new SearchResult("Google", 1L);
        var bingRes = new SearchResult("Bing", 1L);
        var expectedQuery = List.of("duplicate");
        when(googleSearch.searchResults(expectedQuery)).thenReturn(googleRes);
        when(bingSearch.searchResults(expectedQuery)).thenReturn(bingRes);

        var result = searchController.search("duplicate duplicate duplicate");

        assertEquals(new Summary(2L, List.of(googleRes, bingRes)), result);
    }

}
