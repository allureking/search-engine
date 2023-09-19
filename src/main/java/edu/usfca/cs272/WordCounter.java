package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * An implementation of the word count of each file.
 */
public class WordCounter extends WordProcessor {
    protected final TreeMap<String, Integer> wordCount;

    public WordCounter(String inputFile, String outputFile) {
        super(inputFile, outputFile);
        this.wordCount = new TreeMap<>();
    }

    /**
     * process a single file
     * @param filePath
     * @throws IOException
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
     * save output
     * @throws IOException
     */
    protected void saveToOutput() throws IOException {
        JsonWriter.writeObject(wordCount, Paths.get(outputFile));
    }
}
