package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
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

    /*
     * TODO
     * toString
     *
     * addAll(List<String> words, String location, int start)
     *
     * has, num, and view methods...
     *
     * hasCount(String location) --> wordCount.containsKey(location)
     * hasWord(String word) --> wordIndex.containsKey(word)
     * hasLocation(String word, String location)
     * hasPosition(String word, String location, Integer position)
     */

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
     * Calculates word counts from the word index and stores them in the wordCount map.
     */
    public void countFromIndex() { // TODO Remove
      for (Map.Entry<String, TreeMap<String, TreeSet<Integer>>> wordEntry : wordIndex.entrySet()) {
          for (Map.Entry<String, TreeSet<Integer>> fileEntry : wordEntry.getValue().entrySet()) {
              wordCount.put(fileEntry.getKey(), wordCount.getOrDefault(fileEntry.getKey(), 0) + fileEntry.getValue().size());
          }
      }
  }

    // TODO Remove
    /**
     * Converts the original word index map to a format that can be written by the JsonWriter class.
     *
     * @return A new map in the format compatible with JsonWriter's writeObjectObjects() method.
     */
    private Map<String, Map<String, Collection<? extends Number>>> convertMap() {
      Map<String, Map<String, Collection<? extends Number>>> convertedMap = new TreeMap<>();

      for (Map.Entry<String, TreeMap<String, TreeSet<Integer>>> entry : wordIndex.entrySet()) {
          Map<String, Collection<? extends Number>> innerMap = new TreeMap<>(entry.getValue());
          convertedMap.put(entry.getKey(), innerMap);
      }

      return convertedMap;
  }

    /**
     * Saves the computed word index to the specified output file.
     *
     * @param output The path to the output file.
     * @throws IOException If an I/O error occurs while writing to the file.
     */
    protected void saveIndex(Path output) throws IOException {
      Map<String, Map<String, Collection<? extends Number>>> convertedMap = convertMap(); // TODO Remove
      JsonWriter.writeObjectObjects(convertedMap, output);
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
}
