package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Abstract class responsible for word processing.
 */
public class WordProcessor {

    /**
     * Process the input path, whether it's a directory or file, and save the result.
     * @throws IOException If any IO error occurs while processing or saving.
     */
    public void process(String inputFile, InvertedIndex invertedIndex) throws IOException {
        Path path = Paths.get(inputFile);
        if (Files.isDirectory(path)) {
            processDirectory(path, invertedIndex);
        } else {
            processFile(path, invertedIndex);
        }
    }

    /**
     * Abstract method to process a single file.
     *
     * @param filePath Path to the file to process.
     * @throws IOException If any IO error occurs while processing the file.
     */
    private void processFile(Path filePath, InvertedIndex invertedIndex) throws IOException {
        List<String> lines = Files.readAllLines(filePath);
        int index = 1;
        for (String line : lines) {
            ArrayList<String> words = FileStemmer.listStems(line);
            for (String word : words) {
                invertedIndex.add(word, filePath.toString(), index++);
            }
        }
    }

    /**
     * Process a directory by traversing it and processing each path individually.
     *
     * @param dirPath The directory path.
     * @throws IOException If any IO error occurs while processing the directory.
     */
    private void processDirectory(Path dirPath, InvertedIndex invertedIndex) throws IOException {
        List<Path> textFiles = FileFinder.listText(dirPath);
        for (Path textFile : textFiles) {
            processFile(textFile, invertedIndex);
        }
    }
}
