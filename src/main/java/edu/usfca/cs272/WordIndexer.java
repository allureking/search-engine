package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * An implementation of a word index in each file.
 * This class extends {@code WordProcessor} and maintains an index of words and their positions in the processed files.
 */
public class WordIndexer extends WordProcessor {

    /**
     * The main data structure for storing the word index. 
     * The outer map's key is the word, and the value is another map.
     * The inner map's key is the filename, and the value is a list of positions where the word appears.
     */
    protected final Map<String, Map<String, List<Integer>>> wordIndex;  

    /**
     * Constructor for initializing WordIndexer.
     *
     * @param inputFile  The input file to be processed.
     * @param outputFile The output file where the word index should be saved.
     */
    public WordIndexer(String inputFile, String outputFile) {
        super(inputFile, outputFile);
        this.wordIndex = new TreeMap<>();
    }

    /**
     * Processes a single file and updates the word index.
     * 
     * @param filePath  The path of the file to be processed.
     * @throws IOException If an I/O error occurs while reading the file.
     */
    protected void processFile(Path filePath) throws IOException {
        List<String> lines = Files.readAllLines(filePath);
        int index = 1;
        for (String line : lines) {
            ArrayList<String> words = FileStemmer.listStems(line);
            for (String word : words) {
                if (!wordIndex.containsKey(word)) {
                    wordIndex.put(word, new TreeMap<>());
                }

                Map<String, List<Integer>> fileMap = wordIndex.get(word);
                String filename = filePath.toString();
                if (!fileMap.containsKey(filename)) {
                    fileMap.put(filename, new ArrayList<>());
                }

                fileMap.get(filename).add(index++);
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
        
        for (Map.Entry<String, Map<String, List<Integer>>> entry : wordIndex.entrySet()) {
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
    protected void saveToOutput() throws IOException {
        // Convert the original map to the required type before writing
        Map<String, Map<String, Collection<? extends Number>>> convertedMap = convertMap();
        JsonWriter.writeObjectObjects(convertedMap, Paths.get(outputFile));
    }
}
