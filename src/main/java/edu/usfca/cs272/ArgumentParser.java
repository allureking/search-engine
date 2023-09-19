package edu.usfca.cs272;

/**
 * This class is responsible for parsing command-line arguments.
 */
public class ArgumentParser {

    /**
     * The original array of arguments passed to the program.
     */
    private String[] args;

    /**
     * The path to the text input file.
     */
    private String inputPath;

    /**
     * The path to the counts JSON file.
     */
    private String countPath;

    /**
     * The path to the index JSON file.
     */
    private String indexPath;

    /**
     * Constructs a new ArgumentParser object.
     * @param args The original command-line arguments.
     */
    public ArgumentParser(String[] args) {
        this.args = args;
    }

    /**
     * Parses the command-line arguments.
     * Populates inputPath, countPath, and indexPath based on the arguments.
     */
    public void parse() {
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
            } else if ("-index".equals(args[i])) {
                if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                    indexPath = args[++i];
                } else {
                    indexPath = "index.json";
                }
            }
        }

        if (inputPath == null) {
            inputPath = ".";
        }
    }

    /**
     * Returns the path to the text input file.
     * @return The inputPath.
     */
    public String getInputPath() {
        return inputPath;
    }

    /**
     * Returns the path to the counts JSON file.
     * @return The countPath.
     */
    public String getCountPath() {
        return countPath;
    }

    /**
     * Returns the path to the index JSON file.
     * @return The indexPath.
     */
    public String getIndexPath() {
        return indexPath;
    }
}
