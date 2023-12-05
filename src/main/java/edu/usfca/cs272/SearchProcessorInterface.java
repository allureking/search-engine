package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import opennlp.tools.stemmer.Stemmer;

/**
 * an interface of SearchProcessor
 */
public interface SearchProcessorInterface {

    /**
     * Reads a query file line by line and performs a search for each line.
     * In the case of being used multithreading:
     * Initializes a work queue with the specified number of threads to process the search queries concurrently.
     * Each line from the query file is treated as a separate search task.
     *
     * @param queryFile The path to the query file.
     * @throws IOException If an I/O error occurs reading from the file or a malformed or unmappable byte sequence is read.
     */
    default void search(Path queryFile) throws IOException{
        try (BufferedReader reader = Files.newBufferedReader(queryFile)) {
            String line;
            while ((line = reader.readLine()) != null) {
                search(line);
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
    default void search(String line, Stemmer stemmer) {
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
     * Processes a single line of text by stemming and searching for the resultant terms.
     * Ignores empty lines and lines that yield no query terms after stemming.
     *
     * @param line The line of text to process and search.
     */
    void search(String line);

    /**
     * Executes a search for a set of stemmed query words.
     * If the query has already been processed, this method returns early to avoid duplicate processing.
     * Otherwise, it performs the search using the search function defined in the parent class
     * and stores the results in the {@code searchResults} map.
     *
     * @param queries The set of stemmed words to search for.
     */
    void search(Set<String> queries);

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
    void saveResult(Path output) throws IOException;

    /**
     * Retrieves the search results for a specific query.
     * If the query does not exist in the search results, an empty list is returned.
     * This method provides an unmodifiable view of the search results to prevent external modifications.
     *
     * @param query The query string whose search results are to be retrieved.
     * @return An unmodifiable list representing the search results for the given query.
     *         Returns an empty list if the query is not present.
     */
    List<InvertedIndex.QueryResult> getSearchResult(String query);

    /**
     * Provides an unmodifiable set of all processed queries.
     * This allows users to know which queries have been processed without revealing the actual results.
     *
     * @return An unmodifiable set containing all the queries.
     */
    Set<String> getAllQueries();

    /**
     * Retrieves the number of search results for a specific query.
     * If the query does not exist, returns 0.
     *
     * @param query The query string whose number of results is to be retrieved.
     * @return The number of results for the specified query, or 0 if the query does not exist.
     */
    default int getNumberOfResults(String query) {
        return getSearchResult(query).size();
    }
}
