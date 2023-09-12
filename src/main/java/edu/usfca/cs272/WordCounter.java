package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordCounter {
    private final HashMap<String, Integer> wordCount;

    public WordCounter() {
        this.wordCount = new HashMap<>();
    }

    public void processFile(Path filePath) throws IOException {
        List<String> lines = Files.readAllLines(filePath);
        for (String line : lines) {
            ArrayList<String> words = FileStemmer.listStems(line);
            for (String word : words) {
                wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
            }
        }
    }

    public void processDirectory(Path dirPath) throws IOException {
        List<Path> textFiles = DirectoryScanner.listTextFiles(dirPath);
        for (Path textFile : textFiles) {
            processFile(textFile);
        }
    }

    public Map<String, Integer> getWordCount() {
        return wordCount;
    }

    public void saveWordCount(Path jsonFilePath) throws IOException {
        JsonWriter.writeObject(wordCount, jsonFilePath);
    }
}
