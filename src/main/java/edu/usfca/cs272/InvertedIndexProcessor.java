package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM;

/**
 * Class responsible for word processing. It processes individual files or
 * directories and populates the provided inverted index with words found.
 *
 * @author Honghuai(King) Ke
 */
public class InvertedIndexProcessor {
    /**
     * Logger for the InvertedIndexProcessor class.
     * This logger is used to log important information, warnings, and errors encountered
     * during the operation of the InvertedIndexProcessor. It aids in debugging and monitoring
     * the runtime behavior of the class.
     */
    private static final Logger log = LogManager.getLogger(InvertedIndexProcessor.class);

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
     * Processes the input path, whether it's a directory or file, and populates
     * the provided inverted index.
     *
     * @param inputFile      Path to the input file or directory.
     * @param invertedIndex  The inverted index to populate.
     * @param threadNum      The number of concurrent.
     * @throws IOException   If any IO error occurs while processing or saving.
     */
    public static void process(Path inputFile, InvertedIndex invertedIndex, int threadNum) throws IOException {
        if (Files.isDirectory(inputFile)) {
            processDirectory(inputFile, invertedIndex, threadNum);
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

    /**
     * Processes a directory by traversing it and processing each path individually.
     * Populates the provided inverted index with words found in each file.
     *
     * @param dirPath        The directory path.
     * @param invertedIndex  The inverted index to populate.
     * @param threadNum      The number of concurrent.
     * @throws IOException   If any IO error occurs while processing the directory.
     */
    public static void processDirectory(Path dirPath, InvertedIndex invertedIndex, int threadNum) throws IOException {
        log.info("Processing directory with threads {}", threadNum);
        WorkQueue workQueue = new WorkQueue(threadNum);
        log.info("Build {} workers", threadNum);
        List<Path> textFiles = FileFinder.listText(dirPath);
        List<InvertedIndex> indexList = new ArrayList<>();
        for (Path textFile : textFiles) {
            InvertedIndex tmpInvertedIndex = new InvertedIndex();
            workQueue.execute(() -> {
                try {
                    log.debug("start process file {}", textFile);
                    processFile(textFile, tmpInvertedIndex);
                } catch (IOException e) {
                    log.error("Unable to process file", e.getMessage());
                }
            });
            indexList.add(tmpInvertedIndex);
        }

        workQueue.join();

        for (InvertedIndex tmpInvertedIndex: indexList) {
            invertedIndex.merge(tmpInvertedIndex);
        }
    }
}
