package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for word processing. It processes individual files or
 * directories and populates the provided inverted index with words found.
 *
 * @author Honghuai(King) Ke
 * @version Fall 2023
 */
public class WordProcessor {

    /**
     * Processes the input path, whether it's a directory or file, and populates
     * the provided inverted index.
     *
     * @param inputFile      Path to the input file or directory.
     * @param invertedIndex  The inverted index to populate.
     * @throws IOException   If any IO error occurs while processing or saving.
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
     * Processes a single file and populates the provided inverted index with words
     * found in the file.
     *
     * @param filePath       Path to the file to process.
     * @param invertedIndex  The inverted index to populate.
     * @throws IOException   If any IO error occurs while processing the file.
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
     * Processes a directory by traversing it and processing each path individually.
     * Populates the provided inverted index with words found in each file.
     *
     * @param dirPath        The directory path.
     * @param invertedIndex  The inverted index to populate.
     * @throws IOException   If any IO error occurs while processing the directory.
     */
    private void processDirectory(Path dirPath, InvertedIndex invertedIndex) throws IOException {
        List<Path> textFiles = FileFinder.listText(dirPath);
        for (Path textFile : textFiles) {
            processFile(textFile, invertedIndex);
        }
    }
}
