package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM;

/**
 * Class responsible for word processing. It processes individual files or
 * directories and populates the provided inverted index with words found.
 *
 * @author Honghuai Ke
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
            String line;
            int index = 1;
            Stemmer stemmer = new SnowballStemmer(ALGORITHM.ENGLISH);
            String location = filePath.toString();
            while ((line = reader.readLine()) != null) {
                String[] words = FileStemmer.parse(line);
                for (String word : words) {
                    String stemmedWord = stemmer.stem(word).toString();
                    invertedIndex.add(stemmedWord, location, index++);
                }
            }
        }
    }

    /**
     * Processes content of a file and populates the provided inverted index with words
     * found in the file.
     *
     * @param location   the location of the file
     * @param lines       content of url to process.
     * @param invertedIndex  The inverted index to populate.
     */
    public static void processLines(String location, String[] lines, InvertedIndex invertedIndex) {
        Stemmer stemmer = new SnowballStemmer(ALGORITHM.ENGLISH);
        int index = 1;
        for (String line: lines) {
            String[] words = FileStemmer.parse(line);
            for (String word : words) {
                String stemmedWord = stemmer.stem(word).toString();
                invertedIndex.add(stemmedWord, location, index++);
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
    public static void processDirectory(Path dirPath, InvertedIndex invertedIndex) throws IOException {
        List<Path> textFiles = FileFinder.listText(dirPath);
        for (Path textFile : textFiles) {
            processFile(textFile, invertedIndex);
        }
    }
}