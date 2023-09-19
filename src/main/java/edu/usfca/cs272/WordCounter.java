package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * An implementation of the word count for each file.
 */
public class WordCounter extends WordProcessor {
    /**
     * A TreeMap containing the count of words for each file.
     */
    protected final TreeMap<String, Integer> wordCount;

    /**
     * Constructor for the WordCounter class.
     *
     * @param inputFile  The path to the input file.
     * @param outputFile The path to the output file.
     */
    public WordCounter(String inputFile, String outputFile) {
        super(inputFile, outputFile);
        this.wordCount = new TreeMap<>();
    }

    /**
     * Processes a single file and updates the word count.
     *
     * @param filePath The path to the file that needs to be processed.
     * @throws IOException If an IO error occurs while reading the file.
     */
    protected void processFile(Path filePath) throws IOException {
        List<String> lines = Files.readAllLines(filePath);
        for (String line : lines) {
            ArrayList<String> words = FileStemmer.listStems(line);
            String filePathStr = filePath.toString();
            wordCount.put(filePathStr, wordCount.getOrDefault(filePathStr, 0) + words.size());
        }
    }

    /**
     * Saves the word count to the output file.
     *
     * @throws IOException If an IO error occurs while writing to the output file.
     */
    protected void saveToOutput() throws IOException {
        JsonWriter.writeObject(wordCount, Paths.get(outputFile));
    }
}
