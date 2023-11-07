package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
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
public class  InvertedIndex {

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
     * Exact search for a set of queries
     * @param queries
     * @param locationCountMap
     */
    public void exactSearch(Set<String> queries, Map<String, Integer> locationCountMap) {
        for (String query : queries) {
            exactSearch(locationCountMap, query);
        }
    }

    /**
     * Partial search for a set of queries
     * @param queries
     * @param locationCountMap
     */
    public void partialSearch(Set<String> queries, Map<String, Integer> locationCountMap) {
        for (String query: queries) {
            for (String indexWord: viewWords()) {
                if (indexWord.startsWith(query)) {
                    exactSearch(locationCountMap, indexWord);
                }
            }
        }
    }

    /**
     * Executes an exact search for a single word.
     *
     * @param locationCountMap Map to keep track of locations and counts.
     * @param word The word to search.
     */
    private void exactSearch(Map<String, Integer> locationCountMap, String word) {
        Set<String> locations = viewLocations(word);
        for (String location : locations) {
            int count = viewPositions(word, location).size();
            locationCountMap.put(location, locationCountMap.getOrDefault(location, 0) + count);
        }
    }
}
