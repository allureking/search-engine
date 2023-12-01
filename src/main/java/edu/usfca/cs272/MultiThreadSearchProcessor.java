package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import opennlp.tools.stemmer.snowball.SnowballStemmer;

/*
 * TODO Create a search processor interface that both of your classes implement
 */

/**
 * Processor for searching words from a file in a multithreaded manner.
 * This class extends the {@link SearchProcessor} and is designed to handle
 * search queries using multiple threads for improved performance.
 */
public class MultiThreadSearchProcessor extends SearchProcessor {
    /**
     * The work queue used to execute tasks in multiple threads.
     */
    private WorkQueue workQueue;

    /**
     * Constructs a MultiThreadSearchProcessor with a reference to an InvertedIndex, a flag
     * indicating the type of search (partial or exact), and a WorkQueue instance for managing
     * multithreaded tasks. This constructor initializes the underlying SearchProcessor with
     * the specified InvertedIndex and search type. It also sets up the WorkQueue to handle
     * concurrent search operations.
     *
     * @param index     The InvertedIndex to use for searching.
     * @param partial   True to perform a partial search, false for an exact search.
     * @param workQueue The WorkQueue instance to manage multithreaded tasks.
     */
    // TODO thread-safe
    public MultiThreadSearchProcessor(InvertedIndex index, boolean partial, WorkQueue workQueue) {
        super(index, partial);

        this.workQueue = workQueue;
    }

    /**
     * Reads a query file line by line and performs a search for each line using multiple threads.
     * Initializes a work queue with the specified number of threads to process the search queries concurrently.
     * Each line from the query file is treated as a separate search task.
     *
     * @param queryFile The path to the query file.
     * @throws IOException If an I/O error occurs reading from the file or a malformed or unmappable byte sequence is read.
     */
    @Override
    public void search(Path queryFile) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(queryFile)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String finalLine = line;
                workQueue.execute(() -> search(finalLine, new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH)));
            }
        }

        workQueue.finish();
    }

    /**
     * Executes a search for a set of stemmed query words.
     * If the query has already been processed, this method returns early to avoid duplicate processing.
     * Otherwise, it performs the search using the search function defined in the parent class
     * and stores the results in the {@code searchResults} map.
     *
     * @param queries The set of stemmed words to search for.
     */
    @Override
    public void search(Set<String> queries) {
        String queryWords = String.join(" ", queries);

        synchronized (searchResults) { // TODO Break up the sync block so the searchFunction happens outside
            // Return early if queryWords is already a key in the map
            if (searchResults.containsKey(queryWords)) {
                return;
            }
            var local = searchFunction.apply(queries);
            // Use the search function to get results and put them in the map
            searchResults.put(queryWords, local);
        }
    }
}
