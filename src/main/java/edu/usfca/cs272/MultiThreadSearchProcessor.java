package edu.usfca.cs272;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

/**
 * Processor for searching words from a file.
 */
public class MultiThreadSearchProcessor extends SearchProcessor {
    /**
     * Lock for Multi threads processing.
     */
    private MultiReaderLock lock;

	/**
	 * Constructs a SearchProcessor with a reference to an InvertedIndex and a flag indicating
	 * whether to perform partial search. Initializes the stemmer for word normalization and
	 * sets the appropriate search function based on the search type.
	 *
	 * @param index The InvertedIndex to use for searching.
	 * @param partial True to perform partial search, false for exact search.
	 */
    public MultiThreadSearchProcessor(InvertedIndex index, boolean partial) {
        super(index, partial);
    }

    /**
     * Reads a query file line by line and performs a search for each line using multiple threads.
     * Initializes a work queue with the specified number of threads to process the search queries concurrently.
     *
     * @param queryFile The path to the query file.
     * @param workQueue multi thread queue to execute
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
     * If the query has already been processed, this method returns early.
     * Otherwise, it performs the search and stores the results.
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
}
