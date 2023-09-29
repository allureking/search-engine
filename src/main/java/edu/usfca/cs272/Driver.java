package edu.usfca.cs272;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;



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
     * arguments. This includes (but is not limited to) how to build or search an
     * inverted index.
     *
     * @param args flag/value pairs used to start this program
     */
    public static void main(String[] args) {
        // Store initial start time
        Instant start = Instant.now();

        // Output command-line arguments for debugging
        System.out.println(Arrays.toString(args));

        ArgumentParser argumentParser = new ArgumentParser(args);
        String inputPath = argumentParser.getString("-text", "./");

        System.out.println(argumentParser);

        // Output word counts to JSON if required
        if (argumentParser.hasFlag("-counts")) {
            // Initialize WordFileCounter
            String countPath = argumentParser.getString("-counts", "counts.json");
            WordCounter wordCounter = new WordCounter(inputPath, countPath);
            try {
                wordCounter.processPathAndSave();
            } catch (IOException e) {
                System.out.println("Unable to process word counts: " + e.getMessage());
            }
        }

        if (argumentParser.hasFlag("-index")) {
            String indexPath = argumentParser.getString("-index", "index.json");
            WordIndexer wordIndexer = new WordIndexer(inputPath, indexPath);
            try {
                wordIndexer.processPathAndSave();
            } catch (IOException e) {
                System.out.println("Unable to process word index: " + e.getMessage());
            }
        }

        // Calculate time elapsed and output
        long elapsed = Duration.between(start, Instant.now()).toMillis();
        double seconds = (double) elapsed / Duration.ofSeconds(1).toMillis();
        System.out.printf("Elapsed: %f seconds%n", seconds);
    }
    
    /*
     * TODO 
     * Create separate data structure/data storage and data processing/building classes
     * 
     * Create an InvertedIndex class
     * 
     * private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> wordIndex;
     * private final TreeMap<String, Integer> wordCount;
     * 
     * other methods to add to wordIndex, and add to wordCount
     * and safely access wordIndex, and wordCount
     * 
     * Create a separate WordProcessor class that does NOT have the data, but keeps the file and directory processing
     * Keep the processFile and processDirectory methods in here
     */
}
