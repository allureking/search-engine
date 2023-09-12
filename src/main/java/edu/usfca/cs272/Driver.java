package edu.usfca.cs272;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;

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
        String countsFilePath = null;  // No default file, as it depends on whether -counts is supplied

        // Parse command-line arguments
        for (int i = 0; i < args.length; i++) {
            if ("-text".equals(args[i])) {
                if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                    textFilePath = args[++i];
                } else {
                    // Handle the case where -text is provided but no path follows
                    System.out.println("No file or directory path provided for -text. Proceeding without text processing.");
                }
            } else if ("-counts".equals(args[i])) {
                if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                    countsFilePath = args[++i];
                } else {
                    countsFilePath = "counts.json";  // Default output file name
                }
            }
        }
        
        // Initialize a HashMap to store the word count for each file
        HashMap<String, Integer> wordCounts = new HashMap<>();

        // TODO: Implement word counting logic based on `textFilePath`
        // This would involve reading the file or traversing the directory
        // Update wordCounts with the filename as key and its word count as value

        // Output word counts to JSON if required
        if (countsFilePath != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(countsFilePath))) {
                // TODO: Serialize wordCounts HashMap to JSON format and write to the file
                writer.write("TODO: Implement JSON serialization");
            } catch (IOException e) {
                System.out.println("An error occurred while writing to the counts file: " + e.getMessage());
            }
        }

        // Calculate time elapsed and output
        long elapsed = Duration.between(start, Instant.now()).toMillis();
        double seconds = (double) elapsed / Duration.ofSeconds(1).toMillis();
        System.out.printf("Elapsed: %f seconds%n", seconds);
    }

	/*
	 * Generally, "Driver" classes are responsible for setting up and calling other
	 * classes, usually from a main() method that parses command-line parameters.
	 * Generalized reusable code are usually placed outside of the Driver class.
	 * They are sometimes called "Main" classes too, since they usually include the
	 * main() method.
	 *
	 * If the driver were only responsible for a single class, we use that class
	 * name. For example, "TaxiDriver" is what we would name a driver class that
	 * just sets up and calls the "Taxi" class.
	 *
	 * The starter code (calculating elapsed time) is not necessary. It can be
	 * removed from the main method.
	 *
	 * TODO Delete this after reading.
	 */
}
