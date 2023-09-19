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
 * an implementation of word index in each file
 */
public class WordIndexer extends WordProcessor {
    protected final Map<String, Map<String, Collection<? extends Number>>> wordIndex;

    /**
     * Constructor
     * @param inputFile
     * @param outputFile
     */
    public WordIndexer(String inputFile, String outputFile) {
        super(inputFile, outputFile);
        this.wordIndex = new TreeMap<>();
    }

    /**
     * process a single file
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

                Map<String, Collection<? extends Number>> fileMap = wordIndex.get(word);
                String filename = filePath.toString();
                if (!fileMap.containsKey(filename)) {
                    fileMap.put(filename, new ArrayList());
                }

                ((List<Integer>) fileMap.get(filename)).add(index++);
            }
        }
    }

    /**
     * save to output
     * @throws IOException
     */
    protected void saveToOutput() throws IOException {
        JsonWriter.writeObjectObjects(wordIndex, Paths.get(outputFile));
    }
}
