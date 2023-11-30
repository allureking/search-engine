package edu.usfca.cs272;

import java.io.IOException;
import java.io.Writer;
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
     * Merges the contents of another InvertedIndex into a single one.
     * This method iterates through each word, location, and position in the specified index,
     * and adds them to the current index. This is useful in multi-threaded scenarios where
     * each thread processes a part of the data and their results are combined.
     *
     * @param index The InvertedIndex to be merged into the current index.
     */
    public void merge(InvertedIndex index) {
        for (String word : index.viewWords()) {
            for (String location : index.viewLocations(word)) {
                for (int position: index.viewPositions(word, location)) {
                    add(word, location, position);
                }
            }
        }
        
        /* TODO 
        for (var otherEntry : index.wordIndex.entrySet()) {
        	String otherWord = otherEntry.getKey();
        	var otherInnerMap = otherEntry.getValue();
        	var thisInnerMap = this.wordIndex.get(otherWord);
        	
        	if (thisInnerMap == null) {
        		this.wordIndex.put(otherWord, otherInnerMap);
        	}
        	else {
        		loop and do something similar at a different level of nesting
        	}
        }
        
        need a separate loop to update the word counts
        */
        
    }

    /**
     * Adds a new position from a file to the word index.
     *
     * @param word     The word to add.
     * @param location The file location.
     * @param position The position of the word in the file.
     */
    public void add(String word, String location, int position) {
        boolean modified = wordIndex.computeIfAbsent(word, k -> new TreeMap<>())
                                    .computeIfAbsent(location, k -> new TreeSet<>())
                                    .add(position);

        if(modified) {
            wordCount.put(location, wordCount.getOrDefault(location, 0) + 1);
        }

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
        List<QueryResult> results = new ArrayList<>();

        for (String query : queries) {
            if (wordIndex.containsKey(query)) {
                var locations = wordIndex.get(query);
                updateMatches(matches, results, locations);
            }
        }

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
        Map<String, QueryResult> matches = new HashMap<>();
        List<QueryResult> results = new ArrayList<>();

        for (String query : queries) {
            for (var entry : wordIndex.tailMap(query).entrySet()) {
                String indexWord = entry.getKey();
                if (!indexWord.startsWith(query)) {
                    break;
                }
                updateMatches(matches, results, entry.getValue());
            }
        }

        Collections.sort(results);
        return results;
    }

    /**
     * Updates or adds QueryResult objects in the provided map and list based on the locations and counts.
     * For each location in the given map, this method either updates the count of an existing QueryResult
     * or creates and adds a new QueryResult to the map and list.
     * This method is a helper for both exact and partial search methods.
     *
     * @param matches The map of QueryResult objects keyed by their locations.
     * @param results The list to which new QueryResults are added.
     * @param locations The map of locations and their respective counts.
     */
    private void updateMatches(Map<String, QueryResult> matches, List<QueryResult> results, Map<String, TreeSet<Integer>> locations) {
        for (var locationEntry : locations.entrySet()) {
            String location = locationEntry.getKey();
            int count = locationEntry.getValue().size(); // The count is the size of the TreeSet

            QueryResult result = matches.get(location);
            if (result == null) {
                result = new QueryResult(location);
                matches.put(location, result);
                results.add(result);
            }
            result.updateCount(count);
        }
    }

    /**
     * Represents a query result, encapsulating the count of occurrences,
     * a relevance score, and the location where the result was found.
     */
    public class QueryResult implements Comparable<QueryResult>, JsonWriter.JsonObject {
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
         * @param count The additional occurrence count to add to this query result.
         */
        private void updateCount(int count) {
            this.count += count;
            int total = wordCount.get(location);
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
         * Writes the JSON representation of this object to the provided writer.
         * This method converts the object into a map and then iterates through each entry
         * in the map, writing the key-value pairs as JSON. It handles the formatting
         * and indentation of the JSON output according to the specified indentation level.
         * The keys are written as JSON strings, and the values are converted to their
         * string representations. This method assumes that all values in the map can be
         * sensibly converted to a string representation.
         *
         * @param writer the writer to which the JSON representation is written
         * @param indent the indentation level for the JSON output
         * @throws IOException if an I/O error occurs while writing to the writer
         */
        @Override
        public void toJson(Writer writer, int indent) throws IOException {
            Map<String, Object> map = toMap();
            var entryIterator = map.entrySet().iterator();
            if (entryIterator.hasNext()) {
                JsonWriter.writeIndent("{\n", writer, indent + 1);
            }
            while (entryIterator.hasNext()) {
                Map.Entry<String, Object> entry = entryIterator.next();
                JsonWriter.writeQuote(entry.getKey(), writer, indent + 2);
                writer.write(": ");
                writer.write(entry.getValue().toString());
                if (entryIterator.hasNext()) {
                    writer.write(",");
                }
                writer.write("\n");
            }
            JsonWriter.writeIndent("}", writer, indent + 1);
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
