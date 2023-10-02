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
     * arguments. This includes (but is not limited to) building or searching an
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

        System.out.println(argumentParser); // TODO Remove

        InvertedIndex invertedIndex = new InvertedIndex();
        WordProcessor wordProcessor = new WordProcessor();
        
        // TODO if (argumetnParser.hasFlag(-text) {
        // TODO Create the inputPath inside of this if statements
        try {
            wordProcessor.process(inputPath, invertedIndex);
        } catch (IOException e) {
            System.out.println("Unable to process: " + e.getMessage());
        }

        // Output word counts to JSON if required
        if (argumentParser.hasFlag("-counts")) {
            String countPath = argumentParser.getString("-counts", "counts.json");
            try {
                invertedIndex.countFromIndex(); // TODO Remove (counts should always be calculated if the -text flag is present as part of the build process)
                invertedIndex.saveCount(countPath);
            } catch (IOException e) {
                System.out.println("Unable to save word counts: " + e.getMessage());
            }
        }

        if (argumentParser.hasFlag("-index")) {
            String indexPath = argumentParser.getString("-index", "index.json");
            try {
                invertedIndex.saveIndex(indexPath);
            } catch (IOException e) {
                System.out.println("Unable to process word index: " + e.getMessage());
            }
        }

        // Calculate time elapsed and output
        long elapsed = Duration.between(start, Instant.now()).toMillis();
        double seconds = (double) elapsed / Duration.ofSeconds(1).toMillis();
        System.out.printf("Elapsed: %f seconds%n", seconds);
    }
}
