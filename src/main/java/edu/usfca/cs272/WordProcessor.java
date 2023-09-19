package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Abstract word processor
 */
public abstract class WordProcessor {
    protected String inputFile;
    protected String outputFile;

    /**
     * Constructor
     * @param inputFile
     * @param outputFile
     */
    public WordProcessor(String inputFile, String outputFile) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
    }

    /**
     * process input path, no matter it's directory or file
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
     * process a single file
     * @param filePath
     * @throws IOException
     */
    protected abstract void processFile(Path filePath) throws IOException;

    /**
     * process a directory, traverse the directory and process each path individually
     * @param dirPath
     * @throws IOException
     */
    private void processDirectory(Path dirPath) throws IOException {
        List<Path> textFiles = DirectoryScanner.listTextFiles(dirPath);
        for (Path textFile : textFiles) {
            processFile(textFile);
        }
    }

    /**
     * save to output
     * @throws IOException
     */
    protected abstract void saveToOutput() throws IOException;
}
