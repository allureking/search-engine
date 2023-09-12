package edu.usfca.cs272;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
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
        
        // Output command-line arguments (step 1)
        System.out.println(Arrays.toString(args));
        
        // Initialize variables to hold the paths for the text and counts files
        String textFilePath = null;
        String countsFilePath = "counts.txt";  // Default output file name

        // Parse command-line arguments (part of step 3 and new for step 5)
        for (int i = 0; i < args.length; i++) {
            if ("-text".equals(args[i])) {
                textFilePath = args[++i];
            } else if ("-counts".equals(args[i])) {
                countsFilePath = args[++i];
            }
        }

        // Initialize word count variable
        int wordCount = 0;

        // Open and read the text file, then output its contents to the console (part of step 3)
        if (textFilePath != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(textFilePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);

                    // Split the line into words and update word count (step 4)
                    String[] words = line.split("\\s+");
                    wordCount += words.length;
                }
                
                // Output the word count to the console (step 4)
                System.out.println("Word Count: " + wordCount);

            } catch (IOException e) {
                System.out.println("An error occurred while reading the text file: " + e.getMessage());
            }
        }

        // Write the word count to the output file in JSON format (step 6)
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(countsFilePath))) {
            writer.write("{\n  \"WordCount\": " + wordCount + "\n}");
        } catch (IOException e) {
            System.out.println("An error occurred while writing to the counts file: " + e.getMessage());
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
