package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;

/**
 * Class responsible for scanning directories and listing all text files in them.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2023
 */
public class DirectoryScanner {

    /**
     * Lists all text files in the given directory and its subdirectories.
     *
     * @param startDir the directory to start the scan
     * @return a list of all text files in the directory and its subdirectories
     * @throws IOException if an I/O error occurs while accessing the directory
     */
    public static ArrayList<Path> listTextFiles(Path startDir) throws IOException {
        ArrayList<Path> textFiles = new ArrayList<>();
        
        Files.walkFileTree(startDir, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE,
                new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        String filename = file.toString().toLowerCase();
                        if (filename.endsWith(".txt") || filename.endsWith(".text")) {
                            textFiles.add(file);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                    
                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                });
        
        return textFiles;
    }
    
    /**
     * Demonstrates this class.
     *
     * @param args unused
     * @throws IOException if an I/O error occurs
     */
    public static void main(String[] args) throws IOException {
        Path startDir = Path.of("src", "main", "resources");
        ArrayList<Path> textFiles = listTextFiles(startDir);
        
        System.out.println("Text files found:");
        for (Path textFile : textFiles) {
            System.out.println(textFile);
        }
    }
}
