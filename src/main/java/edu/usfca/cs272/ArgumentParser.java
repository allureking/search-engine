package edu.usfca.cs272;

/**
 * class responsible for argument parsing
 */
public class ArgumentParser {
    private String[] args;
    private String inputPath;
    private String countPath;
    private String indexPath;

    public ArgumentParser(String[] args) {
        this.args = args;
    }

    /**
     * parse the arguments
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

    public String getInputPath() {
        return inputPath;
    }

    public String getCountPath() {
        return countPath;
    }

    public String getIndexPath() {
        return indexPath;
    }
}
