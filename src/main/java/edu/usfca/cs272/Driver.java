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

        InvertedIndex invertedIndex = new InvertedIndex();

        if (argumentParser.hasFlag("-text")) {
          Path inputPath = Path.of(argumentParser.getString("-text", "./"));
          try {
              InvertedIndexProcessor.process(inputPath, invertedIndex);
          } catch (IOException e) {
              System.out.println("Unable to process: " + e.getMessage());
          }
        }

        // Output word counts to JSON if required
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

        SearchResult searchResult = new SearchResult();
        String queryArg = argumentParser.getString("-query");
        if (queryArg != null) {
            Path queryPath = Path.of(queryArg);
            try {
                searchProcessor.search(queryPath);
            } catch (IOException e) {
                System.out.println("Unable to process word query: " + e.getMessage());
            }
        }

        if (argumentParser.hasFlag("-results")) {
            Path resultPath = Path.of(argumentParser.getString("-results", "results.json"));
            try {
                searchResult.saveToOutput(resultPath);
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