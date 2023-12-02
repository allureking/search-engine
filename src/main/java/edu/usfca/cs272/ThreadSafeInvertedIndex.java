package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class is a thread-safe version of InvertedIndex.
 * It uses a MultiReaderLock to manage concurrent access to the underlying InvertedIndex data structures.
 *
 * @author Honghuai(King) Ke
 */
public class ThreadSafeInvertedIndex extends InvertedIndex {

	// TODO Need to override more methods to make thread-safe

    /**
     * Lock used for managing concurrent read/write access.
     */
    private final MultiReaderLock lock;

    /**
     * Initializes the ThreadSafeInvertedIndex with empty index and count maps and a new MultiReaderLock.
     */
    public ThreadSafeInvertedIndex() {
        super();
        lock = new MultiReaderLock();
    }

    @Override
    public void merge(InvertedIndex other) {
        lock.writeLock().lock();
        try {
            super.merge(other);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void add(String word, String location, int position) {
        lock.writeLock().lock();
        try {
            super.add(word, location, position);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void addAll(List<String> words, String location, int start) {
        lock.writeLock().lock();
        try {
            super.addAll(words, location, start);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Set<String> viewWords() {
        lock.readLock().lock();
        try {
            return super.viewWords();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Set<String> viewLocations(String word) {
        lock.readLock().lock();
        try {
            return super.viewLocations(word);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Set<Integer> viewPositions(String word, String location) {
        lock.readLock().lock();
        try {
            return super.viewPositions(word, location);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Map<String, Integer> viewCount() {
        lock.readLock().lock();
        try {
            return super.viewCount();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean hasWord(String word) {
        lock.readLock().lock();
        try {
            return super.hasWord(word);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean hasLocation(String word, String location) {
        lock.readLock().lock();
        try {
            return super.hasLocation(word, location);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean hasPosition(String word, String location, Integer position) {
        lock.readLock().lock();
        try {
            return super.hasPosition(word, location, position);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean hasCount(String location) {
        lock.readLock().lock();
        try {
            return super.hasCount(location);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public int totalCount(String location) {
        lock.readLock().lock();
        try {
            return super.totalCount(location);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public int numWords() {
        lock.readLock().lock();
        try {
            return super.numWords();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public int numLocations(String word) {
        lock.readLock().lock();
        try {
            return super.numLocations(word);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public int numPositions(String word, String location) {
        lock.readLock().lock();
        try {
            return super.numPositions(word, location);
        } finally {
            lock.readLock().unlock();
        }
    }

    // Override methods that save data to files

    @Override
    public void saveIndex(Path output) throws IOException {
        lock.readLock().lock();
        try {
            super.saveIndex(output);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void saveCount(Path output) throws IOException {
        lock.readLock().lock();
        try {
            super.saveCount(output);
        } finally {
            lock.readLock().unlock();
        }
    }

    // Override methods for searching

    @Override
    public List<QueryResult> exactSearch(Set<String> queries) {
        lock.readLock().lock();
        try {
            return super.exactSearch(queries);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<QueryResult> partialSearch(Set<String> queries) {
        lock.readLock().lock();
        try {
            return super.partialSearch(queries);
        } finally {
            lock.readLock().unlock();
        }
    }
}
