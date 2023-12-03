package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

/**
 * Class responsible for running this project based on the provided command-line
 * arguments. See the README for details.
 *
 * @author Honghuai Ke
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2023
 */
public class Driver {

    /**
     * Initializes the classes necessary based on the provided command-line
     * arguments. This includes (but is not limited to) building or searching an
     * inverted index.
     *
     * @param args flag/value pairs used to start this program
     */
    public static void main(String[] args) {
        // Store initial start time
        Instant start = Instant.now();

        ArgumentParser argumentParser = new ArgumentParser(args);

        ThreadSafeInvertedIndex threadSafeInvertedIndex = null;
        /* TODO
        ThreadSafeInvertedIndex threadSafe = null;
        InvertedIndex invertedIndex = null;
        SearchProcessorInterface searchProcessor = null;
        WorkQueue workQueue = null;

        if (argumentParser.hasFlag("-threads")) {
        	 threadNum = argumentParser.getInteger("-threads");
           threadNum = threadNum < 1 ? 5 : threadNum;

        	threadSafe = new ThreadSafeInvertedIndex();
        	invertedIndex = threadSafe;
        	etc.
        }
        else {
        	invertedIndex = new InvertedIndex();
        	etc.
        }
        */

        InvertedIndex invertedIndex = new InvertedIndex();

        boolean partial = argumentParser.hasFlag("-partial");

        int threadNum = 1;

        if (argumentParser.hasFlag("-threads")) {
            threadNum = argumentParser.getInteger("-threads");
            threadNum = threadNum < 1 ? 5 : threadNum;
        }

        WorkQueue workQueue = threadNum > 1 ? new WorkQueue(threadNum) : null;

        SearchProcessor searchProcessor = workQueue == null ?
                new SearchProcessor(invertedIndex, partial) : new MultiThreadSearchProcessor(invertedIndex, partial, workQueue);

        if (argumentParser.hasFlag("-text")) {
            Path inputPath = Path.of(argumentParser.getString("-text", "./"));
            try {
                if (workQueue != null) {
                    System.out.println("run with " + threadNum + " threads");
                    MultiThreadInvertedIndexProcessor.process(inputPath, threadSafeInvertedIndex, workQueue);
                    workQueue.finish();
                } else {
                    System.out.println("run with single thread");
                    InvertedIndexProcessor.process(inputPath, invertedIndex);

                }
            } catch (IOException e) {
                System.out.println("Unable to process: " + e.getMessage());
            }
          }

        if (argumentParser.hasFlag("-query")) {
            Path queryPath = Path.of(argumentParser.getString("-query", "queries.txt"));
            try {
                searchProcessor.search(queryPath);
            } catch (IOException e) {
                System.out.println("Unable to process query file: " + e.getMessage());
            }
        }

        if (workQueue != null) {
            workQueue.shutdown();
        }

        if (argumentParser.hasFlag("-counts")) {
            Path countPath = Path.of(argumentParser.getString("-counts", "counts.json"));
            try {
                invertedIndex.saveCount(countPath);
            } catch (IOException e) {
                System.out.println("Unable to save word counts: " + e.getMessage());
            }
        }

        if (argumentParser.hasFlag("-index")) {
            Path indexPath = Path.of(argumentParser.getString("-index", "index.json"));
            try {
                invertedIndex.saveIndex(indexPath);
            } catch (IOException e) {
                System.out.println("Unable to process word index: " + e.getMessage());
            }
        }

        if (argumentParser.hasFlag("-results")) {
            Path resultPath = Path.of(argumentParser.getString("-results", "results.json"));
            try {
                searchProcessor.saveResult(resultPath);
            } catch (IOException e) {
                System.out.println("Unable to save search result: " + e.getMessage());
            }
        }

        // Calculate time elapsed and output
        long elapsed = Duration.between(start, Instant.now()).toMillis();
        double seconds = (double) elapsed / Duration.ofSeconds(1).toMillis();
        System.out.printf("Elapsed: %f seconds%n", seconds);
    }
}