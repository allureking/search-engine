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
 * An implementation of word index in each file.
 */
public class WordIndexer extends WordProcessor {
    protected final Map<String, Map<String, List<Integer>>> wordIndex;  // Original data structure

    /**
     * Constructor
     * 
     * @param inputFile
     * @param outputFile
     */
    public WordIndexer(String inputFile, String outputFile) {
        super(inputFile, outputFile);
        this.wordIndex = new TreeMap<>();
    }

    /**
     * Process a single file.
     * 
     * @param filePath
     * @throws IOException
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
     * Converts the internal {@code wordIndex} map to a format compatible with the JsonWriter class.
     * <p>
     * The original map uses {@code List<Integer>} as the inner value type, which is converted to
     * {@code Collection<? extends Number>} to align with the JsonWriter's method signatures.
     * </p>
     *
     * @return A new map with keys and values conforming to the types expected by JsonWriter methods.
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
     * Save to output.
     * 
     * @throws IOException
     */
    protected void saveToOutput() throws IOException {
        // Convert the original map to the required type before writing
        Map<String, Map<String, Collection<? extends Number>>> convertedMap = convertMap();
        JsonWriter.writeObjectObjects(convertedMap, Paths.get(outputFile));
    }
}
