package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Processor for searching words from a file in a multithreaded manner.
 * This class extends the {@link SearchProcessor} and is designed to handle
 * search queries using multiple threads for improved performance.
 */
public class MultiThreadSearchProcessor extends SearchProcessor {
    /**
     * Lock used for managing concurrent read/write access in a multithreaded environment.
     */
    private MultiReaderLock lock; // TODO Remove and use the sync keyword instead

    // TODO WorkQueue workQueue member that is sent to the constructor
    
    /**
     * Constructs a MultiThreadSearchProcessor with a reference to an InvertedIndex and a flag
     * indicating whether to perform partial search. This constructor initializes the stemmer
     * for word normalization and sets the appropriate search function based on the search type.
     *
     * @param index    The InvertedIndex to use for searching.
     * @param partial  True to perform partial search, false for exact search.
     */
    public MultiThreadSearchProcessor(InvertedIndex index, boolean partial) {
        super(index, partial);
    }

    /**
     * Reads a query file line by line and performs a search for each line using multiple threads.
     * Initializes a work queue with the specified number of threads to process the search queries concurrently.
     * Each line from the query file is treated as a separate search task.
     *
     * @param queryFile The path to the query file.
     * @param workQueue The work queue used to execute tasks in multiple threads.
     * @throws IOException If an I/O error occurs reading from the file or a malformed or unmappable byte sequence is read.
     */
    public void search(Path queryFile, WorkQueue workQueue) throws IOException {
        lock = new MultiReaderLock();

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

        // Return early if queryWords is already a key in the map
        if (searchResults.containsKey(queryWords)) { // TODO Need to protect this read
            return;
        }
        
        // TODO var local = searchFunction.apply(queries);

        if (lock != null) {
            lock.writeLock().lock();
        }
        // Use the search function to get results and put them in the map
        searchResults.put(queryWords, searchFunction.apply(queries)); // TODO local

        if (lock != null) {
            lock.writeLock().unlock();
        }
    }
}
