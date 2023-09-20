package edu.usfca.cs272;

/**
 * Class responsible for parsing command-line arguments.
 */
public class ArgumentParser {
	// TODO Integrate the homework
	
    /**
     * Array to hold the command-line arguments passed.
     */
    private String[] args;

    /**
     * Path to the input text file or directory.
     */
    private String inputPath;

    /**
     * Path to save the counts.
     */
    private String countPath;

    /**
     * Constructor to initialize the ArgumentParser.
     *
     * @param args Command-line arguments passed to the application.
     */
    public ArgumentParser(String[] args) {
        this.args = args;
    }

    /**
     * Parses the command-line arguments to populate the inputPath and countPath.
     */
    public void parse() {
        // Parse command-line arguments
        for (int i = 0; i < args.length; i++) {
            if ("-text".equals(args[i])) {
                if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                    inputPath = args[++i];
                } else {
                    System.out.println("No file or directory path provided for -text. Proceeding without text processing.");
                }
            } else if ("-counts".equals(args[i])) {
                if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                    countPath = args[++i];
                } else {
                    countPath = "counts.json";
                }
            }
        }

        if (inputPath == null) {
            inputPath = ".";
        }
    }

    /**
     * Gets the input path.
     *
     * @return the input path as a String.
     */
    public String getInputPath() {
        return inputPath;
    }

    /**
     * Gets the count path.
     *
     * @return the count path as a String.
     */
    public String getCountPath() {
        return countPath;
    }
}
