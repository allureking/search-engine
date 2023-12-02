package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class responsible for word processing. It processes individual files or
 * directories and populates the provided inverted index with words found.
 *
 * @author Honghuai(King) Ke
 */
public class MultiThreadInvertedIndexProcessor {
    /**
     * Logger for the InvertedIndexProcessor class.
     * This logger is used to log important information, warnings, and errors encountered
     * during the operation of the InvertedIndexProcessor. It aids in debugging and monitoring
     * the runtime behavior of the class.
     */
    private static final Logger log = LogManager.getLogger(MultiThreadInvertedIndexProcessor.class);

    /**
     * Processes the input path, whether it's a directory or file, and populates
     * the provided inverted index.
     *
     * @param inputFile      Path to the input file or directory.
     * @param invertedIndex  The inverted index to populate.
     * @param workQueue multi thread queue to execute
     * @throws IOException   If any IO error occurs while processing or saving.
     */
    public static void process(Path inputFile, InvertedIndex invertedIndex, WorkQueue workQueue) throws IOException { // TODO ThreadSafeInvertedIndex
        if (Files.isDirectory(inputFile)) {
            processDirectory(inputFile, invertedIndex, workQueue);
        } else {
            InvertedIndexProcessor.processFile(inputFile, invertedIndex);
        }
    }

    /**
     * Processes a directory by traversing it and processing each path individually.
     * Populates the provided inverted index with words found in each file.
     *
     * @param dirPath        The directory path.
     * @param invertedIndex  The inverted index to populate.
     * @param workQueue multi thread queue to execute
     * @throws IOException   If any IO error occurs while processing the directory.
     */
    public static void processDirectory(Path dirPath, InvertedIndex invertedIndex, WorkQueue workQueue) throws IOException { // TODO ThreadSafeInvertedIndex
        List<Path> textFiles = FileFinder.listText(dirPath);
        for (Path textFile : textFiles) {
            workQueue.execute(() -> {
                try {
                    log.debug("start process file {}", textFile);

                    InvertedIndex local = new InvertedIndex();
                    InvertedIndexProcessor.processFile(textFile, local);
                    synchronized (invertedIndex) { // TODO Remove
                        invertedIndex.mergeDistinct(local);
                    }
                } catch (IOException e) {
                    log.error("Unable to process file", e.getMessage());
                }
            });
        }

        workQueue.finish();
    }
}
