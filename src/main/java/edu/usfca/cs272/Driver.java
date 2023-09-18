package edu.usfca.cs272;

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

        // Initialize variables to hold the paths for the text and counts files
        String textFilePath = null;
        String countsFilePath = null;
        String indexFilePath = null;

        // Parse command-line arguments
        for (int i = 0; i < args.length; i++) {
            if ("-text".equals(args[i])) {
                if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                    textFilePath = args[++i];
                } else {
                    System.out.println("No file or directory path provided for -text. Proceeding without text processing.");
                }
            } else if ("-counts".equals(args[i])) {
                if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                    countsFilePath = args[++i];
                } else {
                    countsFilePath = "counts.json";
                }
            } else if ("-index".equals(args[i])) {
                if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                    indexFilePath = args[++i];
                } else {
                    indexFilePath = "index.json";
                }
            }
        }

        textFilePath = textFilePath == null ? "./" : textFilePath;

        // Process Files or Directories
        if (textFilePath == null) {
            throw new IllegalArgumentException("textFilePath cannot be null");
        }
        
        // Output word counts to JSON if required
        if (countsFilePath != null) {
            // Initialize WordFileCounter
            WordCounter wordCounter = new WordCounter(textFilePath, countsFilePath);
            wordCounter.processPathAndSave();
        }

        if (indexFilePath != null) {
            WordIndexer wordIndexer = new WordIndexer(textFilePath, indexFilePath);
            wordIndexer.processPathAndSave();
        }

        // Calculate time elapsed and output
        long elapsed = Duration.between(start, Instant.now()).toMillis();
        double seconds = (double) elapsed / Duration.ofSeconds(1).toMillis();
        System.out.printf("Elapsed: %f seconds%n", seconds);
    }
}