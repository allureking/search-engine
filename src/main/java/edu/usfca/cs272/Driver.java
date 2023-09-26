package edu.usfca.cs272;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

//TODO Have some unused import warnings... configure Eclipse to automatically remove unused imports

//TODO Use the @Override annotation (configure Eclipse to add for you automatically) 

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
        // TO DO fix Parser Logic
        /*
         * if (argumentParser.hasFlag(-text)) {
         *    Path path = argumentParser.getPath(-text);
         *    
         *    try {
         *       1 or 2 lines of code
         *    }
         *    catch ( ) {
         *       Unable to build the inverted index from path: + path
         *    }
         * }
         * 
         * if (argumentParser.hasFlag(-counts)) {
         *    ....
         * }
         */
        
        // Output word counts to JSON if required
        if (argumentParser.hasFlag("-counts")) {
            // Initialize WordFileCounter
            String countPath = argumentParser.getString("-counts", "counts.json");
            WordCounter wordCounter = new WordCounter(inputPath, countPath);
            wordCounter.processPathAndSave();
        }

        if (argumentParser.hasFlag("-index")) {
            String indexPath = argumentParser.getString("-index", "index.json");
            WordIndexer wordIndexer = new WordIndexer(inputPath, indexPath);
            wordIndexer.processPathAndSave();
        }

        // Calculate time elapsed and output
        long elapsed = Duration.between(start, Instant.now()).toMillis();
        double seconds = (double) elapsed / Duration.ofSeconds(1).toMillis();
        System.out.printf("Elapsed: %f seconds%n", seconds);
    }
}