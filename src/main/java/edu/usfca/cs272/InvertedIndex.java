package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class InvertedIndex {
    private final TreeMap<String, TreeMap<String, List<Integer>>> wordIndex;
    private final TreeMap<String, Integer> wordCount;

    public InvertedIndex() {
        wordIndex = new TreeMap();
        wordCount = new TreeMap();
    }

    /**
     * add a new position from a file to word index
     * @param word
     * @param location
     * @param position
     */
    public void add(String word, String location, int position) {
        if (!wordIndex.containsKey(word)) {
            wordIndex.put(word, new TreeMap<>());
        }

        Map<String, List<Integer>> fileMap = wordIndex.get(word);
        if (!fileMap.containsKey(location)) {
            fileMap.put(location, new ArrayList<>());
        }

        fileMap.get(location).add(position);
    }

    /**
     * calculate word count from word index
     */
    public void countFromIndex() {
        for (Map.Entry<String, TreeMap<String, List<Integer>>> wordEntry: wordIndex.entrySet()) {
            for (Map.Entry<String, List<Integer>> fileEntry: wordEntry.getValue().entrySet()) {
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
            Map<String, Collection<? extends Number>> innerMap = new TreeMap<>();
            for (Map.Entry<String, List<Integer>> innerEntry : entry.getValue().entrySet()) {
                innerMap.put(innerEntry.getKey(), innerEntry.getValue());
            }
            convertedMap.put(entry.getKey(), innerMap);
        }

        return convertedMap;
    }

    /**
     * Saves the computed word index to the output file.
     *
     * @throws IOException If an I/O error occurs while writing to the file.
     */
    protected void saveIndex(String output) throws IOException {
        // Convert the original map to the required type before writing
        Map<String, Map<String, Collection<? extends Number>>> convertedMap = convertMap();
        JsonWriter.writeObjectObjects(convertedMap, Paths.get(output));
    }

    /**
     * Saves the computed word count to the output file.
     *
     * @throws IOException If an I/O error occurs while writing to the file.
     */
    public void saveCount(String output) throws IOException {
        JsonWriter.writeObject(wordCount, Paths.get(output));
    }
}
