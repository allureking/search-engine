package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
    public static void process(Path inputFile, InvertedIndex invertedIndex, WorkQueue workQueue) throws IOException {
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
    public static void processDirectory(Path dirPath, InvertedIndex invertedIndex, WorkQueue workQueue) throws IOException {
        List<Path> textFiles = FileFinder.listText(dirPath);
        List<InvertedIndex> indexList = new ArrayList<>(); // TODO Remove (文档内见v3.1修改序号1）
        for (Path textFile : textFiles) {
            InvertedIndex tmpInvertedIndex = new InvertedIndex(); // TODO Remove (文档内见v3.1修改序号1）
            workQueue.execute(() -> {
                try {
                    log.debug("start process file {}", textFile);
                    InvertedIndexProcessor.processFile(textFile, tmpInvertedIndex);

                    /* TODO (文档内见v3.1修改序号1）
                    InvertedIndex local = new InvertedIndex();
                    processFile(textFile, local);
                    invertedIndex.merge(local);
                    	*/

                } catch (IOException e) {
                    log.error("Unable to process file", e.getMessage());
                }
            });
            indexList.add(tmpInvertedIndex);
        }

        workQueue.finish();
        for (InvertedIndex tmpInvertedIndex : indexList) { // TODO Remove (文档内见v3.1修改序号1）
            invertedIndex.merge(tmpInvertedIndex);
        }
    }
}
