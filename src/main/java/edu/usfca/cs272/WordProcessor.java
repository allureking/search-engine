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
 */
public class WordProcessor { // TODO InvertedIndexProcessor or InvertedIndexTextProcessor
	// TODO Make all of the methods in here static

    /**
     * Processes the input path, whether it's a directory or file, and populates
     * the provided inverted index.
     *
     * @param inputFile      Path to the input file or directory.
     * @param invertedIndex  The inverted index to populate.
     * @throws IOException   If any IO error occurs while processing or saving.
     */
	// TODO public void process(Path inputFile, InvertedIndex invertedIndex) throws IOException {
    public void process(String inputFile, InvertedIndex invertedIndex) throws IOException {
        Path path = Paths.get(inputFile); // TODO Path.of
        if (Files.isDirectory(path)) {
            processDirectory(path, invertedIndex);
        } else {
            processFile(path, invertedIndex);
        }
    }

    // TODO public void processFile
    /**
     * Processes a single file and populates the provided inverted index with words
     * found in the file.
     *
     * @param filePath       Path to the file to process.
     * @param invertedIndex  The inverted index to populate.
     * @throws IOException   If any IO error occurs while processing the file.
     */
    private void processFile(Path filePath, InvertedIndex invertedIndex) throws IOException {
    	// TODO Use a buffered reader, read line by line, immediately process the line 
    	// TODO parse, stem, add directly to the index (never to a list)
        List<String> lines = Files.readAllLines(filePath);
        int index = 1;
        for (String line : lines) {
            ArrayList<String> words = FileStemmer.listStems(line);
            for (String word : words) {
                invertedIndex.add(word, filePath.toString(), index++);
            }
        }
        
        // TODO use index here as the word count, then that doesn't need to be a separate step
    }

    // TODO public
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
