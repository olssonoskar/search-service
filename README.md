# Search Service

A simple API to get a sum of matches from different search engines for a provided query string.

## Running the application

The application is intended to either be run directly from IntelliJ or in a terminal. 
For running in a terminal, running mvn verify to produce a JAR should be enough and then 
simply starting it vit java -jar. 

The application requires API keys for Google and Bing, which need to be provided in application.yaml.

Querying can be done either via a browser or tool like curl, example below where we query for 'ystad simrishamn'

    curl '127.0.0.1:8080/search?q=ystad%20simrishamn'
    {"totalHits":14922000,"searchResults":[{"name":"Bing","hits":342000},{"name":"Google","hits":14580000}]}

## Logical flow

The application has a single GET endpoint at /search that takes a query
param for the String that contains a set of words separated by a single space.

The words are split into a list and each words are passed to the integrated
search engines for individual querying. Currently the following search engines are integrated:
 - Google (Tested with Free tier: Limited to 100 queries per day)
 - Bing (Tested with Free tier: Limitied to 3 req/s, up to 1k per day)

Assumption: Due to the rate limiting of Bing, queries with more words seems to take a bit longer to finish.
I have tested with a length of 6 words, I assume a very long query would lead to timeout failures.

Each SearchEngine will simply map the JSON data we want from the response and return the sum of the hits
for all the word searches it performed. The SearchService that requests the search for each engine then
simply provides a summary of all engines and the result for each engines sum back to the consumer.

The requests are sent in parallel via parallel streams but on the response we block to wait for a response.
An optimization I see here would be to run these on a Virtual Thread pool instead but as this app does not
have other important work to do while waiting, I skipped it for now.

I think most of the code is understandable assuming familiarity with Java Streams. However, The WebClient used
is reactive using Project Reactors model (from Spring WebFlux), which is the reason for the following:

    .bodyToMono(BingResponse.class)
    .onErrorResume(e -> {
        ...
    })
    .blockOptional();

Mono here is a Reactive Publisher that emits 0-1 value or an error signal, which fits since we expect a single 
response to our request. The response body (JSON) will be converted to an instance of the provided class.
Finally, as Mono is a 'future' value, we need to block to wait for the value to be emitted. 

## Improvements

### Virtual thread pool
Configuring a dedicated thread pool for the requests to avoid blocking platform threads should be
a good improvement, especially when additional engines would be integrated.

### Config properties
Configuration properties are not currently validated. If they were, the application
would not be able to start if mandatory ones were missing, such as an API key. 
This would be good to have to avoid runtime failures.