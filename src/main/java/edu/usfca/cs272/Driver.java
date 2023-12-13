package edu.usfca.cs272;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
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

        boolean partial = argumentParser.hasFlag("-partial");

        int threadNum = 1;

        if (argumentParser.hasFlag("-threads")) {
            threadNum = argumentParser.getInteger("-threads");
            threadNum = threadNum < 1 ? 5 : threadNum;
        }

        int totalCrawl = argumentParser.getInteger("-crawl", 1);
        if (totalCrawl <= 0) {
            totalCrawl = 1;
        }

        WorkQueue workQueue = threadNum > 1 ? new WorkQueue(threadNum) : null;
        InvertedIndex invertedIndex = null;
        ThreadSafeInvertedIndex threadSafeInvertedIndex = null;
        SearchProcessorInterface searchProcessor;
        CrawlerProcessorInterface crawlerProcessor;

        if (workQueue == null) {
            invertedIndex = new InvertedIndex();
            searchProcessor = new SearchProcessor(invertedIndex, partial);
            crawlerProcessor = new CrawlerProcessor(invertedIndex, totalCrawl);
        } else {
            threadSafeInvertedIndex = new ThreadSafeInvertedIndex();
            invertedIndex = threadSafeInvertedIndex;
            searchProcessor = new MultiThreadSearchProcessor(threadSafeInvertedIndex, partial, workQueue);
            crawlerProcessor = new MultiThreadCrawlerProcessor(workQueue, threadSafeInvertedIndex, totalCrawl);
        }

        boolean processHtml = false;
        if (argumentParser.hasFlag("-html")) {
            String seed = argumentParser.getString("-html");
            if (seed == null) {
                System.out.println("Empty url parameter");
            } else {
                URI uri = LinkFinder.makeUri(seed);
                if (uri != null) {
                    try {
                        URL url = LinkFinder.cleanUri(uri).toURL();
                        crawlerProcessor.crawl(url);
                        processHtml = true;
                        if (workQueue != null) {
                            workQueue.finish();
                        }
                    } catch (MalformedURLException e) {
                        System.out.println("Invalid URL: " + seed);
                    }
                } else {
                    System.out.println("Invalid url: " + seed);
                }
            }
        }

        if (argumentParser.hasFlag("-text")) {
            Path inputPath = null;
            if (processHtml) {
                String inputPathStr = argumentParser.getString("-text");
                if (inputPathStr != null) {
                    inputPath = Path.of(inputPathStr);
                }
            } else {
                inputPath = Path.of(argumentParser.getString("-text", "./"));
            }

            if (inputPath != null) {
                try {
                    if (workQueue != null) {
                        MultiThreadInvertedIndexProcessor.process(inputPath, threadSafeInvertedIndex, workQueue);
                        workQueue.finish();
                    } else {
                        InvertedIndexProcessor.process(inputPath, invertedIndex);
                    }
                } catch (IOException e) {
                    System.out.println("Unable to process: " + e.getMessage());
                }
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