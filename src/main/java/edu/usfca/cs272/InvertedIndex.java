package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Class responsible for maintaining an inverted index. This class will store word
 * positions across multiple files and can also track the total word counts for each file.
 *
 * @author Honghuai(King) Ke
 */
public class InvertedIndex {

	/**
	 * A nested TreeMap structure to store the word positions for each word in each file.
	 * The outer key is the word, and the inner key is the file location.
	 * The inner value is a TreeSet of positions where the word was found in that file.
	 */
	private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> wordIndex;

	/**
	 * A TreeMap structure to store the total count of each word across all files.
	 * The key is the word and the value is the count of that word.
	 */
	private final TreeMap<String, Integer> wordCount;

    /**
     * Initializes the InvertedIndex with empty index and count maps.
     */
    public InvertedIndex() {
        wordIndex = new TreeMap<>();
        wordCount = new TreeMap<>();
    }

    @Override
    public String toString() {
        return wordIndex.toString();
    }

    /**
     * Adds a new position from a file to the word index.
     *
     * @param word     The word to add.
     * @param location The file location.
     * @param position The position of the word in the file.
     */
    public void add(String word, String location, int position) {
        wordIndex.computeIfAbsent(word, k -> new TreeMap<>())
           .computeIfAbsent(location, k -> new TreeSet<>())
           .add(position);

        wordCount.put(location, wordCount.getOrDefault(location, 0) + 1);
    }

    /**
     * Adds multiple word positions from a list of words to the word index.
     *
     * @param words The list of words to add.
     * @param location The file location.
     * @param start The starting position of the words in the file.
     */
    public void addAll(List<String> words, String location, int start) {
        for (String word : words) {
            add(word, location, start++);
        }
    }

    /**
     * Saves the computed word index to the specified output file.
     *
     * @param output The path to the output file.
     * @throws IOException If an I/O error occurs while writing to the file.
     */
    public void saveIndex(Path output) throws IOException {
      JsonWriter.writeObjectObjects(wordIndex, output);
    }

    /**
     * Saves the computed word count to the specified output file.
     *
     * @param output The path to the output file.
     * @throws IOException If an I/O error occurs while writing to the file.
     */
    public void saveCount(Path output) throws IOException {
      JsonWriter.writeObject(wordCount, output);
    }

    /**
     * Determines whether the word count contains the specified location.
     *
     * @param location The location to check.
     * @return true if the location exists in the word count; false otherwise.
     */
    public boolean hasCount(String location) {
        return wordCount.containsKey(location);
    }

    /**
     * Determines whether the word index contains the specified word.
     *
     * @param word The word to check.
     * @return true if the word exists in the word index; false otherwise.
     */
    public boolean hasWord(String word) {
        return wordIndex.containsKey(word);
    }

    /**
     * Determines whether the word index contains the specified word and location.
     *
     * @param word The word to check.
     * @param location The location to check.
     * @return true if the word and location exist in the word index; false otherwise.
     */
    public boolean hasLocation(String word, String location) {
      return viewLocations(word).contains(location);
    }

    /**
     * Determines whether the word index contains the specified word, location, and position.
     *
     * @param word The word to check.
     * @param location The location to check.
     * @param position The position to check.
     * @return true if the word, location, and position exist in the word index; false otherwise.
     */
    public boolean hasPosition(String word, String location, Integer position) {
      return viewPositions(word, location).contains(position);
    }

    /**
     * Returns the number of unique words in the index.
     *
     * @return Number of unique words.
     */
    public int numWords() {
      return viewWords().size();
    }

    /**
     * Returns the number of unique locations where a specific word is found.
     *
     * @param word The word to check.
     * @return Number of unique locations for the word.
     */
    public int numLocations(String word) {
      return viewLocations(word).size();
    }

    /**
     * Returns the number of times a specific word is found in a specific location.
     *
     * @param word     The word to check.
     * @param location The location to check.
     * @return Number of positions the word is found in the location.
     */
    public int numPositions(String word, String location) {
        return viewPositions(word, location).size();
    }

    /**
     * Returns the total count of a specific word across all files.
     *
     * @param location The location (typically a file) to check.
     * @return Total count of the word.
     */
    public int totalCount(String location) {
        return wordCount.getOrDefault(location, 0);
    }

    /**
     * Provides an unmodifiable view of the words in the index.
     *
     * @return An unmodifiable set of words.
     */
    public Set<String> viewWords() {
        return Collections.unmodifiableSet(wordIndex.keySet());
    }

    /**
     * Provides an unmodifiable view of the locations a specific word appears in.
     *
     * @param word The word to check.
     * @return An unmodifiable set of locations for the given word.
     */
    public Set<String> viewLocations(String word) {
        if (wordIndex.containsKey(word)) {
            return Collections.unmodifiableSet(wordIndex.get(word).keySet());
        }
        return Collections.emptySet();
    }

    /**
     * Provides an unmodifiable view of the positions a specific word appears at in a specific location.
     *
     * @param word     The word to check.
     * @param location The location to check.
     * @return An unmodifiable set of positions for the given word in the given location.
     */
    public Set<Integer> viewPositions(String word, String location) {
        if (wordIndex.containsKey(word) && wordIndex.get(word).containsKey(location)) {
            return Collections.unmodifiableSet(wordIndex.get(word).get(location));
        }
        return Collections.emptySet();
    }


    /**
     * Provides a direct view of the word count map.
     *
     * @return An unmodifiable view of the internal word count structure.
     */
    public Map<String, Integer> viewCount() {
        return Collections.unmodifiableMap(wordCount);
    }

    /**
     * Performs an exact search for each query in the provided set.
     * This method looks for exact matches of the query words in the index and
     * aggregates the results into a sorted list of {@link QueryResult} objects.
     * The sorting is based on the relevance score, occurrence count, and location.
     *
     * @param queries A set of queries to search for.
     * @return A sorted list of {@link QueryResult} objects representing the search results.
     */
    public List<QueryResult> exactSearch(Set<String> queries) {
        Map<String, QueryResult> matches = new HashMap<>();

        for (String query : queries) {
            if (wordIndex.containsKey(query)) {
                var innerMap = wordIndex.get(query);

                for (var entry : innerMap.entrySet()) {
                    updateMatches(matches, entry.getKey(), entry.getValue().size());
                }
            }
        }

        List<QueryResult> results = new ArrayList<>(matches.values());
        Collections.sort(results);
        return results;
    }

    /**
     * Performs a partial search for each query in the provided set.
     * A partial search considers any index word that starts with the given query string.
     * For each matching index word, the method aggregates the search results,
     * ultimately returning them as a sorted list of {@link QueryResult} objects.
     * The sorting is based on the relevance score, occurrence count, and location.
     *
     * @param queries A set of queries to search for.
     * @return A sorted list of {@link QueryResult} objects representing the search results.
     */
    public List<QueryResult> partialSearch(Set<String> queries) {
        Map<String, QueryResult> matches = new TreeMap<>();

        for (String query : queries) {
            for (var entry : wordIndex.tailMap(query).entrySet()) {
                String indexWord = entry.getKey();
                if (!indexWord.startsWith(query)) {
                    break;
                }

                var locations = entry.getValue();
                for (var locationEntry : locations.entrySet()) {
                    updateMatches(matches, locationEntry.getKey(), locationEntry.getValue().size());
                }
            }
        }

        List<QueryResult> resultList = new ArrayList<>(matches.values());
        Collections.sort(resultList);
        return resultList;
    }

    /**
     * Updates or adds a QueryResult object in the provided map based on the location and count.
     * If a QueryResult for the given location already exists in the map, its count is updated.
     * Otherwise, a new QueryResult object is created and added to the map.
     * This method is a helper for both exact and partial search methods.
     *
     * @param matches The map of QueryResult objects keyed by their locations.
     * @param location The location (typically a file path) of the search results.
     * @param count The number of occurrences of the query in the given location.
     */
    private void updateMatches(Map<String, QueryResult> matches, String location, int count) {
        QueryResult result = matches.get(location);
        if (result == null) {
            result = new QueryResult(location);
            matches.put(location, result);
        }
        result.updateCount(totalCount(location), count);
    }

    /**
     * Represents a query result, encapsulating the count of occurrences,
     * a relevance score, and the location where the result was found.
     */
    public static class QueryResult implements Comparable<QueryResult>, JsonWriter.JsonObject {
        /**
         * The occurrenceCount is the number of times a query term appears. This is used
         * to measure how often a term is encountered within a particular dataset or document.
         */
        private int count;

        /**
         * The relevanceScore is a metric that quantifies how relevant a query result is
         * to the search query. Typically, higher scores indicate more relevance.
         */
        private double score;

        /**
         * The location is a string identifier for where the query result was found.
         * This could represent a URL, a file path, or any other location identifier.
         */
        private final String location;


        /**
         * Constructs a QueryResult with the specified location.
         *
         * @param location    the location associated with the query result
         */
        public QueryResult(String location) {
            this.location = location;
        }

        /**
         * Updates the occurrence count and recalculates the relevance score for this query result.
         * The relevance score is calculated as the ratio of this query result's count to the total count.
         *
         * @param total The total occurrence count across all query results.
         * @param count The additional occurrence count to add to this query result.
         */
        public void updateCount(int total, int count) {
            this.count += count;
            score = total == 0 ? 0.0 : this.count / (double) total;
        }

        /**
         * Returns the occurrence count for this query result.
         *
         * @return the occurrence count
         */
        public int getCount() {
            return count;
        }

        /**
         * Returns the relevance score for this query result.
         *
         * @return the relevance score
         */
        public double getScore() {
            return score;
        }

        /**
         * Returns the location for this query result.
         *
         * @return the location
         */
        public String getLocation() {
            return location;
        }

        /**
         * Converts the QueryResult to a Map representation for JSON serialization.
         * The relevance score is formatted to 8 decimal places. This method is used
         * for converting the QueryResult object to a format suitable for JSON serialization.
         *
         * @return a TreeMap containing the properties of this QueryResult, sorted by key
         */
        @Override
		public Map<String, Object> toMap() {
            TreeMap<String, Object> map = new TreeMap<>();
            map.put("count", count);
            map.put("score", String.format("%.8f", score));
            map.put("where", "\"" + location + "\"");

            return map;
        }

        /**
         * Compares this QueryResult with another QueryResult for order.
         * Ordering is primarily by relevance score, then by occurrence count, and finally by location.
         *
         * @param other the QueryResult to be compared
         * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object
         */
        @Override
        public int compareTo(QueryResult other) {
            int result = Double.compare(other.score, score);
            if (result == 0) {
                result = Integer.compare(other.count, count);
                if (result == 0) {
                    result = location.compareTo(other.location);
                }
            }
            return result;
        }
    }
}
