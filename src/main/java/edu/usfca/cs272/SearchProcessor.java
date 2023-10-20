package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Processor for searching words from a file.
 */
public class SearchProcessor { // TODO Take a non-static approach
	
	
    /**
     * Executes a search.
     *
     * @param queryFile The path to the query file.
     * @param searchResult Object to store the search result.
     * @param index The inverted index.
     * @param partial Flag indicating whether search is partial or not.
     * @throws IOException If there is an error reading the query file.
     */
    public static void search(Path queryFile, SearchResult searchResult, InvertedIndex index, boolean partial) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(queryFile)) {
            String line;
            Stemmer stemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH); // TODO Make this a member of this class so we can easily reuse it
            while ((line = reader.readLine()) != null) {
            	// TODO Call search(...) on the line
                if (line.isEmpty()) {
                    continue;
                }

                // TODO Move this into the other search method
                TreeSet<String> wordSet = new TreeSet<>();
                wordSet.addAll(FileStemmer.listStems(line, stemmer)); // TODO uniqueStems
                if (wordSet.isEmpty()) {
                    continue;
                }

                search(searchResult, index, wordSet, partial);
            }
        }
    }

    /**
     * Executes an exact search.
     *
     * @param searchResult Object to store the search result.
     * @param index The inverted index.
     * @param wordSet The set of words to search.
     * @param partial Flag indicating whether search is partial or not.
     */
    private static void search(SearchResult searchResult, InvertedIndex index, TreeSet<String> wordSet, boolean partial) { // TODO TreeSet<String> wordSet --> String queryLine
        String queryWords = String.join(" ", wordSet);
        searchResult.addQuery(queryWords);

        Map<String, Integer> locationCountMap = new TreeMap<>();

        if (partial) {
            for (String word: wordSet) {
                for (String indexWord: index.viewWords()) {
                    if (indexWord.startsWith(word)) {
                        exactSearchOneWord(locationCountMap, index, indexWord);
                    }
                }
            }
        } else {
            for (String word : wordSet) { // TODO String query : queries
                exactSearchOneWord(locationCountMap, index, word);
            }
        }

        saveToSearchResult(queryWords, locationCountMap, searchResult, index);
    }

    /**
     * Executes an exact search for a single word.
     *
     * @param locationCountMap Map to keep track of locations and counts.
     * @param index The inverted index.
     * @param word The word to search.
     */
    private static void exactSearchOneWord(Map<String, Integer> locationCountMap, InvertedIndex index, String word) {
        Set<String> locations = index.viewLocations(word);
        for (String location : locations) {
            int count = index.viewPositions(word, location).size();
            locationCountMap.put(location, locationCountMap.getOrDefault(location, 0) + count);
        }
    }

    /**
     * Saves location search map to search results.
     *
     * @param queryWords The processed words of the query.
     * @param locationCountMap Map of location and counts.
     * @param searchResult Object to store the search result.
     * @param index The inverted index.
     */
    private static void saveToSearchResult(String queryWords, Map<String, Integer> locationCountMap, SearchResult searchResult, InvertedIndex index) {
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
