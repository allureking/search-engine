package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Abstract class responsible for word processing.
 */
public abstract class WordProcessor {
    /**
     * Path to the input file.
     */
    protected String inputFile;

    /**
     * Path to the output file.
     */
    protected String outputFile;

    /**
     * Constructor for initializing WordProcessor.
     *
     * @param inputFile  Path to the input file.
     * @param outputFile Path to the output file.
     */
    public WordProcessor(String inputFile, String outputFile) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
    }

    /**
     * Process the input path, whether it's a directory or file, and save the result.
     */
    public void processPathAndSave() {
        Path path = Paths.get(inputFile);
        try {
            if (Files.isDirectory(path)) {
                processDirectory(path);
            } else {
                processFile(path);
            }

            saveToOutput();
        } catch (IOException e) {
            System.out.println("An error occurred while processing files: " + e.getMessage());
        }
    }

    /**
     * Abstract method to process a single file.
     *
     * @param filePath Path to the file to process.
     * @throws IOException If any IO error occurs while processing the file.
     */
    protected abstract void processFile(Path filePath) throws IOException;

    /**
     * Process a directory by traversing it and processing each path individually.
     *
     * @param dirPath The directory path.
     * @throws IOException If any IO error occurs while processing the directory.
     */
    private void processDirectory(Path dirPath) throws IOException {
        List<Path> textFiles = DirectoryScanner.listTextFiles(dirPath);
        for (Path textFile : textFiles) {
            processFile(textFile);
        }
    }

    /**
     * Abstract method to save the processed data to the output path.
     *
     * @throws IOException If any IO error occurs while saving the data.
     */
    protected abstract void saveToOutput() throws IOException;
}
