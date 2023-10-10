package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for word processing. It processes individual files or
 * directories and populates the provided inverted index with words found.
 *
 * @author Honghuai(King) Ke
 */
public class InvertedIndexProcessor {

    /**
     * Processes the input path, whether it's a directory or file, and populates
     * the provided inverted index.
     *
     * @param inputFile      Path to the input file or directory.
     * @param invertedIndex  The inverted index to populate.
     * @throws IOException   If any IO error occurs while processing or saving.
     */
    public static void process(Path inputFile, InvertedIndex invertedIndex) throws IOException {
        if (Files.isDirectory(inputFile)) {
            processDirectory(inputFile, invertedIndex);
        } else {
            processFile(inputFile, invertedIndex);
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
    public static void processFile(Path filePath, InvertedIndex invertedIndex) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
        			// TODO Use a buffered reader, read line by line, immediately process the line, then parse, stem, add directly to the index (never to a list)
            String line;
            int index = 1;
            while ((line = reader.readLine()) != null) {
                ArrayList<String> words = FileStemmer.listStems(line);
                for (String word : words) {
                    invertedIndex.add(word, filePath.toString(), index++);
                }
            }
        }
        // TODO use index here as the word count, then that doesn't need to be a separate step
    }

    /**
     * Processes a directory by traversing it and processing each path individually.
     * Populates the provided inverted index with words found in each file.
     *
     * @param dirPath        The directory path.
     * @param invertedIndex  The inverted index to populate.
     * @throws IOException   If any IO error occurs while processing the directory.
     */
    public static void processDirectory(Path dirPath, InvertedIndex invertedIndex) throws IOException {
        List<Path> textFiles = FileFinder.listText(dirPath);
        for (Path textFile : textFiles) {
            processFile(textFile, invertedIndex);
        }
    }
}
