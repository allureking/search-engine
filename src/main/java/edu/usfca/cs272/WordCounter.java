package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * An implementation of the word count in each file.
 * This class extends the {@code WordProcessor} and aims to count the words in the processed files.
 */
public class WordCounter extends WordProcessor {
    
    /**
     * TreeMap to store the word count against each file path.
     */
    protected final TreeMap<String, Integer> wordCount;

    /**
     * Constructor for WordCounter.
     *
     * @param inputFile  The input file to be processed.
     * @param outputFile The output file where the word count should be saved.
     */
    public WordCounter(String inputFile, String outputFile) {
        super(inputFile, outputFile);
        this.wordCount = new TreeMap<>();
    }

    /**
     * Processes a single file and updates the word count.
     *
     * @param filePath  The path of the file to be processed.
     * @throws IOException If an I/O error occurs while reading the file.
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
     * Saves the computed word count to the output file.
     *
     * @throws IOException If an I/O error occurs while writing to the file.
     */
    protected void saveToOutput() throws IOException {
        JsonWriter.writeObject(wordCount, Paths.get(outputFile));
    }
}
