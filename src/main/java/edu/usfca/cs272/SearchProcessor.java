package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Processor for searching words from a file.
 */
public class SearchProcessor {

    /**
     * Stores search results mapped by query strings.
     */
    private final TreeMap<String, List<QueryResult>> searchResults;

	/**
	 * InvertedIndex instance used for performing search operations.
	 */
	private InvertedIndex index;

	/**
	 * Flag indicating whether to perform partial (true) or exact (false) search operations.
	 */
	private boolean partial;

	/**
	 * Stemmer instance used for normalizing words during the search process.
	 */
	private Stemmer stemmer;


    /**
     * Constructs a SearchProcessor with a reference to an InvertedIndex and a flag indicating
     * whether to perform partial search.
     *
     * @param index The InvertedIndex to use for searching.
     * @param partial True to perform partial search, false for exact search.
     */
    public SearchProcessor(InvertedIndex index, boolean partial) {
        this.index = index;
        this.partial = partial;

        stemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);
        searchResults = new TreeMap<>();
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
                search(line);
            }
        }
    }

    /**
     * Processes a single line of text by stemming and searching for the resultant terms.
     *
     * @param line The line of text to process and search.
     */
    public void search(String line) {
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
    public void search(TreeSet<String> queries) {
        String queryWords = String.join(" ", queries);

        if (partial) {
            searchResults.put(queryWords, index.partialSearch(queries));
        } else {
            searchResults.put(queryWords, index.exactSearch(queries));
        }
    }

    /**
     * Saves the search results to the specified file path.
     * This method delegates to the {@code searchResult} object's
     * {@code saveToOutput} method to write the results in a structured format,
     * typically JSON, to the file system.
     *
     * @param output The {@code Path} object representing the file path
     *               where the search results will be saved. If the file
     *               already exists, it will be overwritten.
     * @throws IOException If an I/O error occurs writing to the file path.
     */
    public void saveResult(Path output) throws IOException {
        Map<String, Collection<Map<String, Object>>> elements = new TreeMap<>();
        for (Map.Entry<String, List<QueryResult>> entry: searchResults.entrySet()) {
            List<Map<String, Object>> list = new ArrayList<>();
            elements.put(entry.getKey(), list);
            for (QueryResult result : entry.getValue()) {
                list.add(result.toMap());
            }
        }
        JsonWriter.writeObjectArrayObject(elements, output);
    }
}
