package edu.usfca.cs272;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

/**
 * Class responsible for running this project based on the provided command-line
 * arguments. See the README for details.
 *
 * @author TODO Add Name Here
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
		// store initial start time
		Instant start = Instant.now();

		// TODO Fill in and modify as needed
		System.out.println(Arrays.toString(args));

		// calculate time elapsed and output
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
