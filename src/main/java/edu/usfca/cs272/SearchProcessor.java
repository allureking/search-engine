package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
	 * SearchResult object that will hold the results of the search operations.
	 */
	private SearchResult searchResult;

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

        searchResult = new SearchResult();
        stemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);
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
    private void search(String line) {
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
    private void search(TreeSet<String> queries) {
        String queryWords = String.join(" ", queries);
        searchResult.addQuery(queryWords);

        Map<String, Integer> locationCountMap = new TreeMap<>();

        if (partial) {
            index.partialSearch(queries, locationCountMap);
        } else {
            index.exactSearch(queries, locationCountMap);
        }

        saveToSearchResult(queryWords, locationCountMap);
    }

    /**
     * Saves the results of a search to the SearchResult object.
     *
     * @param queryWords The concatenated query words.
     * @param locationCountMap A map containing locations and their associated hit counts.
     */
    private void saveToSearchResult(String queryWords, Map<String, Integer> locationCountMap) {
        for (Map.Entry<String, Integer> entry: locationCountMap.entrySet()) {
            String location = entry.getKey();
            int count = entry.getValue();
            int total = index.totalCount(location);
            double score = total == 0 ? 0.0 : count / (double) total;

            searchResult.addKeyValue(queryWords, count, score, location);
        }

        searchResult.sortValues(queryWords);
    }
}
