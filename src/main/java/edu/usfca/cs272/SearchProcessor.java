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

	private SearchResult searchResult;
    private Path queryFile;
    private InvertedIndex index;
    private boolean partial;
    private Stemmer stemmer;

    /**
     * @param index
     * @param partial
     */
    public SearchProcessor(InvertedIndex index, boolean partial) {
        this.index = index;
        this.partial = partial;

        searchResult = new SearchResult();
        stemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);
    }

    /**
     * Executes a search.
     * @param queryFile
     * @throws IOException
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
     * Executes an exact search.
     * @param line
     *
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
     * Executes an exact search.
     *
     * @param queries The set of words to search.
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
     * Saves location search map to search results.
     *
     * @param queryWords The processed words of the query.
     * @param locationCountMap Map of location and counts.
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
