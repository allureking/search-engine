package edu.usfca.cs272;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * processor to search words from file
 */
public class SearchProcessor {
    /**
     * execute search
     * @param queryFile
     * @param searchResult
     * @param index
     * @param partial
     * @throws IOException
     */
    public static void search(Path queryFile, SearchResult searchResult, InvertedIndex index, boolean partial) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(queryFile)) {
            String line;
            Stemmer stemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }

                TreeSet<String> wordSet = new TreeSet();
                wordSet.addAll(FileStemmer.listStems(line, stemmer));
                if (wordSet.isEmpty()) {
                    continue;
                }

                search(searchResult, index, wordSet, partial);
            }
        }
    }

    /**
     * execute exact search
     * @param searchResult
     * @param index
     * @param wordSet
     */
    private static void search(SearchResult searchResult, InvertedIndex index, TreeSet<String> wordSet, boolean partial) {
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
            for (String word : wordSet) {
                exactSearchOneWord(locationCountMap, index, word);
            }
        }

        saveToSearchResult(queryWords, locationCountMap, searchResult, index);
    }

    /**
     * execute exact search for one word
     * @param locationCountMap
     * @param index
     * @param word
     */
    private static void exactSearchOneWord(Map<String, Integer> locationCountMap, InvertedIndex index, String word) {
        Set<String> locations = index.viewLocations(word);
        for (String location : locations) {
            int count = index.viewPositions(word, location).size();
            locationCountMap.put(location, locationCountMap.getOrDefault(location, 0) + count);
        }
    }

    /**
     * save location search map to search results
     * @param queryWords
     * @param locationCountMap
     * @param searchResult
     * @param index
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
