package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
	 * The inner value is a list of positions where the word was found in that file.
	 */
	private final TreeMap<String, TreeMap<String, List<Integer>>> wordIndex;

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

    /**
     * Adds a new position from a file to the word index.
     *
     * @param word     The word to add.
     * @param location The file location.
     * @param position The position of the word in the file.
     */
    public void add(String word, String location, int position) {
        wordIndex.computeIfAbsent(word, k -> new TreeMap<>())
                 .computeIfAbsent(location, k -> new ArrayList<>())
                 .add(position);
    }

    /**
     * Calculates word counts from the word index and stores them in the wordCount map.
     */
    public void countFromIndex() {
        for (Map.Entry<String, TreeMap<String, List<Integer>>> wordEntry : wordIndex.entrySet()) {
            for (Map.Entry<String, List<Integer>> fileEntry : wordEntry.getValue().entrySet()) {
                wordCount.put(fileEntry.getKey(), wordCount.getOrDefault(fileEntry.getKey(), 0) + fileEntry.getValue().size());
            }
        }
    }

    /**
     * Converts the original word index map to a format that can be written by the JsonWriter class.
     *
     * @return A new map in the format compatible with JsonWriter's writeObjectObjects() method.
     */
    private Map<String, Map<String, Collection<? extends Number>>> convertMap() {
        Map<String, Map<String, Collection<? extends Number>>> convertedMap = new TreeMap<>();

        for (Map.Entry<String, TreeMap<String, List<Integer>>> entry : wordIndex.entrySet()) {
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
    protected void saveIndex(String output) throws IOException {
        Map<String, Map<String, Collection<? extends Number>>> convertedMap = convertMap();
        JsonWriter.writeObjectObjects(convertedMap, Paths.get(output));
    }

    /**
     * Saves the computed word count to the specified output file.
     *
     * @param output The path to the output file.
     * @throws IOException If an I/O error occurs while writing to the file.
     */
    public void saveCount(String output) throws IOException {
        JsonWriter.writeObject(wordCount, Paths.get(output));
    }
}
