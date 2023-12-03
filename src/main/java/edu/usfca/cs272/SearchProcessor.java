package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Processor for searching words from a file.
 */
public class SearchProcessor implements SearchProcessorInterface {
    /**
     * Stores search results mapped by query strings.
     */
    private final TreeMap<String, List<InvertedIndex.QueryResult>> searchResults;

    /**
     * A functional interface representing the search operation. It takes a set of query terms
     * and returns a collection of {@link InvertedIndex.QueryResult} objects representing the search results.
     * This function abstracts the search logic, allowing for different search implementations
     * (e.g., exact or partial) to be used interchangeably.
     */
    private final Function<Set<String>, List<InvertedIndex.QueryResult>> searchFunction;

    /**
     * Stemmer instance used for normalizing words during the search process.
     */
    private final Stemmer stemmer;

    /**
     * Constructs a SearchProcessor with a reference to an InvertedIndex and a flag indicating
     * whether to perform partial search. Initializes the stemmer for word normalization and
     * sets the appropriate search function based on the search type.
     *
     * @param index The InvertedIndex to use for searching.
     * @param partial True to perform partial search, false for exact search.
     */
    public SearchProcessor(InvertedIndex index, boolean partial) {
        searchResults = new TreeMap<>();
        stemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);

        if (partial) {
            searchFunction = index::partialSearch;
        } else {
            searchFunction = index::exactSearch;
        }
    }

    /**
     * Reads a query file line by line and performs a search for each line.
     *
     * @param queryFile The path to the query file.
     * @throws IOException If an I/O error occurs reading from the file or a malformed or unmappable byte sequence is read.
     */
    @Override
    public void search(Path queryFile) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(queryFile)) {
            String line;
            while ((line = reader.readLine()) != null) {
                search(line, this.stemmer);
            }
        }
    }

    /**
     * Processes a single line of text by stemming and searching for the resultant terms.
     * Ignores empty lines and lines that yield no query terms after stemming.
     *
     * @param line The line of text to process and search.
     * @param stemmer The stemmer instance to use for normalizing words.
     */
    @Override
    public void search(String line, Stemmer stemmer) {
        if (line.isEmpty()) {
            return;
        }

        TreeSet<String> queries = FileStemmer.uniqueStems(line, stemmer);
        if (queries.isEmpty()) {
            return;
        }

        search(queries);
    }

    /**
     * Executes a search for a set of stemmed query words.
     * If the query has already been processed, this method returns early.
     * Otherwise, it performs the search and stores the results.
     *
     * @param queries The set of stemmed words to search.
     */
    @Override
    public void search(Set<String> queries) {
        String queryWords = String.join(" ", queries);

        // Return early if queryWords is already a key in the map
        if (searchResults.containsKey(queryWords)) {
            return;
        }
        // Use the search function to get results and put them in the map
        searchResults.put(queryWords, searchFunction.apply(queries));
    }

    /**
     * Saves the search results to the specified file path in JSON format.
     * This method leverages the JsonWriter.writeObjectArrayObject method to serialize
     * and write the search results. The search results are expected to be in the form of
     * a map where each key represents a query and the corresponding value is a collection
     * of QueryResult objects implementing the JsonWriter.JsonObject interface.
     *
     * @param output The Path object representing the file path where the search results
     *               will be saved. If the file already exists, it will be overwritten.
     * @throws IOException If an I/O error occurs writing to the file path.
     */
    @Override
    public void saveResult(Path output) throws IOException {
        JsonWriter.writeObjectArrayObject(searchResults, output);
    }

    @Override
    public String toString() {
        return "SearchProcessor{" +
                "searchResults=" + searchResults +
                ", stemmer=" + stemmer +
                '}';
    }

    /**
     * Retrieves the search results for a specific query.
     * If the query does not exist in the search results, an empty list is returned.
     * This method provides an unmodifiable view of the search results to prevent external modifications.
     *
     * @param query The query string whose search results are to be retrieved.
     * @return An unmodifiable list representing the search results for the given query.
     *         Returns an empty list if the query is not present.
     */
    @Override
    public List<InvertedIndex.QueryResult> getSearchResult(String query) {
        if (searchResults.containsKey(query)) {
            return Collections.unmodifiableList(searchResults.get(query));
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Provides an unmodifiable set of all processed queries.
     * This allows users to know which queries have been processed without revealing the actual results.
     *
     * @return An unmodifiable set containing all the queries.
     */
    @Override
    public Set<String> getAllQueries() {
        return Collections.unmodifiableSet(searchResults.keySet());
    }

    /**
     * Retrieves the number of search results for a specific query.
     * If the query does not exist, returns 0.
     *
     * @param query The query string whose number of results is to be retrieved.
     * @return The number of results for the specified query, or 0 if the query does not exist.
     */
    @Override
    public int getNumberOfResults(String query) {
        List<InvertedIndex.QueryResult> results = searchResults.get(query);
        return results != null ? results.size() : 0;
    }

    /**
     * Retrieves a subset of search results for a specific query.
     * Allows controlled access to the search results by specifying a start index and count.
     * If the query does not exist, or the parameters are out of bounds, returns an empty list.
     *
     * @param query The query string whose search results are to be retrieved.
     * @param startIndex The starting index of the results to retrieve.
     * @param count The number of results to retrieve starting from the startIndex.
     * @return An unmodifiable list of search results starting from the specified index with the specified count.
     *         Returns an empty list if the query does not exist or the parameters are out of bounds.
     */
    @Override
    public List<InvertedIndex.QueryResult> getSearchResults(String query, int startIndex, int count) {
        if (!searchResults.containsKey(query)) {
            return Collections.emptyList();
        }
        List<InvertedIndex.QueryResult> results = searchResults.get(query);
        int endIndex = Math.min(startIndex + count, results.size());
        return Collections.unmodifiableList(results.subList(startIndex, endIndex));
    }

}
