package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Processor for searching words from a file.
 */
public class SearchProcessor {

    /**
     * Stores search results mapped by query strings.
     */
	private final TreeMap<String, Collection<? extends JsonWriter.JsonObject>> searchResults;

	/**
	 * Stemmer instance used for normalizing words during the search process.
	 */
	private final Stemmer stemmer;

    /**
     * lock for multi thread processing.
     */
    private MultiReaderLock lock;

	/**
	 * A functional interface representing the search operation. It takes a set of query terms
	 * and returns a collection of {@link InvertedIndex.QueryResult} objects representing the search results.
	 * This function abstracts the search logic, allowing for different search implementations
	 * (e.g., exact or partial) to be used interchangeably.
	 */
	private final Function<Set<String>, Collection<InvertedIndex.QueryResult>> searchFunction;

    /**
     * Constructs a SearchProcessor with a reference to an InvertedIndex and a flag indicating
     * whether to perform partial search.
     *
     * @param index The InvertedIndex to use for searching.
     * @param partial True to perform partial search, false for exact search.
     */
    public SearchProcessor(InvertedIndex index, boolean partial) {
        stemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);
        searchResults = new TreeMap<>();

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
    public void search(Path queryFile) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(queryFile)) {
            String line;
            while ((line = reader.readLine()) != null) {
                search(line, this.stemmer);
            }
        }
    }

    /**
     * Reads a query file line by line and performs a search for each line.
     *
     * @param queryFile The path to the query file.
     * @param threadNum number of threads to run
     * @throws IOException If an I/O error occurs reading from the file or a malformed or unmappable byte sequence is read.
     */
    public void search(Path queryFile, int threadNum) throws IOException {
        lock = new MultiReaderLock();

        WorkQueue workQueue = new WorkQueue(threadNum);
        try (BufferedReader reader = Files.newBufferedReader(queryFile)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String finalLine = line;
                workQueue.execute(() -> search(finalLine, new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH)));
            }
        }

        workQueue.join();
    }

    /**
     * Processes a single line of text by stemming and searching for the resultant terms.
     *
     * @param line The line of text to process and search.
     * @param stemmer The stemmer to use.
     */
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
     *
     * @param queries The set of stemmed words to search.
     */
    public void search(Set<String> queries) {
        String queryWords = String.join(" ", queries);

        // Return early if queryWords is already a key in the map
        if (searchResults.containsKey(queryWords)) {
            return;
        }

        if (lock != null) {
            lock.writeLock().lock();
        }

        // Use the search function to get results and put them in the map
        searchResults.put(queryWords, searchFunction.apply(queries));

        if (lock != null) {
            lock.writeLock().unlock();
        }
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
     * If the query does not exist in the search results, an empty collection is returned.
     *
     * @param query The query string whose search results are to be retrieved.
     * @return An unmodifiable collection representing the search results for the given query.
     *         Returns an empty collection if the query is not present.
     */
    public Collection<JsonWriter.JsonObject> getSearchResult(String query) {
        return Collections.unmodifiableCollection(searchResults.getOrDefault(query, Collections.emptyList()));
    }

    /**
     * Retrieves an unmodifiable view of all search results.
     * This ensures that the internal data structure is not exposed or modified externally.
     *
     * @return An unmodifiable map containing all the search results, where each key is a query string,
     *         and the corresponding value is a collection of {@link JsonWriter.JsonObject}.
     */
    public Map<String, Collection<? extends JsonWriter.JsonObject>> getAllSearchResults() {
        return Collections.unmodifiableMap(searchResults);
    }

}
