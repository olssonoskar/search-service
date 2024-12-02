# Search Service

A simple API to get a sum of matches from different search engines for a provided query string.

## Running the application

The application is intended to either be run directly from IntelliJ or in a terminal.
Using IntelliJ, just run a default configuration from AppApplication.
For running in a terminal, run

    mvn clean verify 

to produce a JAR in /target that can then be started with 

    java -jar <jar_file>

The application requires API keys for Google (as well as cx) and Bing, which need to be provided in application.yaml.

Querying can be done either via a browser or tool like curl, example below where we query for 'ystad simrishamn'

    curl '127.0.0.1:8080/search?q=ystad%20simrishamn'
    {"totalHits":14922000,"searchResults":[{"name":"Bing","hits":342000},{"name":"Google","hits":14580000}]}

The app was developed and tested with java 23 but I have tested and 21 works as well.
Setting java version in pom.xml to 21 should be enough to use it if preferable.

## Logical flow

The application has a single GET endpoint at /search that takes a query
param 'q' for the String that contains a set of words separated by a single space.

The words are split into a list which is passed to the integrated
search engines for individual querying. Currently, the following search engines are integrated:
 - Google (Tested with Free tier: Limited to 100 queries per day)
 - Bing (Tested with Free tier: Limited to 3 req/s, up to 1k per day)

Assumption: Due to the rate limiting of Bing, queries with more words seems to take a bit longer to finish.
I have tested with a length of 6 words, I assume a very long query would lead to timeout failures here.

Each SearchEngine will do a search for each individual word and map the JSON data we want from the response and return 
the sum of the hits for all the searches it performed. This data is returned and then summarized to provide a total of matches
for all engines as well as the individual result for each engine back to the user/consumer.

The requests are sent in parallel via parallel streams but on the response we block to wait for a response.
As we have enabled 

    spring.threads.virtual.enabled: true

Spring should make use of virtual threads for these requests and so actual blocking of platform threads should not happen.
Virtual threads will instead be suspended and then resumed when the required resource is available. 
This is something we wouldn't see much of an improvement from based on our usage and the rate limiting of the search
engines in use, but if we were to ever scale up it would be very beneficial.

I think most of the code is understandable assuming familiarity with Java Streams. However, The WebClient used
is reactive, using Project Reactors model (from Spring WebFlux), which is the reason for the following:

    .bodyToMono(BingResponse.class)
    .onErrorResume(e -> {
        ...
    }).blockOptional();

Mono here is a Reactive Publisher that emits 0-1 value or an error signal, which fits since we expect a single 
response to our request. When available, the response body (JSON) will be converted to an instance of the provided class.
As Mono can be seen as a 'future' value, we need to block to wait for the value to be emitted. 
This is where virtual threads would be a nice to have if the traffic to our server was high.

## Improvements

Some areas that could be improved with more time

### Config properties
Configuration properties are not currently validated. If they were, the application
would not be able to start when mandatory ones were missing, such as an API key. 
This would be good to have to fail faster and avoid runtime failures when already deployed.

### Custom error response
Some customization on the error response would probably be nice to have, currently just using
Springs default 'Whitelabel Error Page' for requests not matching any handlers.