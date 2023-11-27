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

    /**
     * Merges the contents of another InvertedIndex into a single one.
     * This method iterates through each word, location, and position in the specified index,
     * and adds them to the current index. This is useful in multi-threaded scenarios where
     * each thread processes a part of the data and their results are combined.
     *
     * @param index The InvertedIndex to be merged into the current index.
     */
    @Override
    public void merge(InvertedIndex index) {
        lock.writeLock().lock();

        super.merge(index);

        lock.writeLock().unlock();
    }

    /**
     * Adds a new position from a file to the word index.
     *
     * @param word     The word to add.
     * @param location The file location.
     * @param position The position of the word in the file.
     */
    @Override
    public void add(String word, String location, int position) {
        lock.writeLock().lock();
        super.add(word, location, position);

        lock.writeLock().unlock();
    }

    /**
     * Saves the computed word index to the specified output file.
     *
     * @param output The path to the output file.
     * @throws IOException If an I/O error occurs while writing to the file.
     */
    @Override
    public void saveIndex(Path output) throws IOException {
        lock.readLock().lock();
        try {
            super.saveIndex(output);
        } catch (IOException e) {
            throw e;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Saves the computed word count to the specified output file.
     *
     * @param output The path to the output file.
     * @throws IOException If an I/O error occurs while writing to the file.
     */
    @Override
    public void saveCount(Path output) throws IOException {
        try {
            super.saveCount(output);
        } catch (IOException e) {
            throw e;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Determines whether the word count contains the specified location.
     *
     * @param location The location to check.
     * @return true if the location exists in the word count; false otherwise.
     */
    @Override
    public boolean hasCount(String location) {
        lock.readLock().lock();
        boolean res = super.hasCount(location);
        lock.readLock().unlock();

        return res;
    }

    /**
     * Determines whether the word index contains the specified word.
     *
     * @param word The word to check.
     * @return true if the word exists in the word index; false otherwise.
     */
    @Override
    public boolean hasWord(String word) {
        lock.readLock().lock();
        boolean res = super.hasWord(word);
        lock.readLock().unlock();

        return res;
    }

    /**
     * Returns the total count of a specific word across all files.
     *
     * @param location The location (typically a file) to check.
     * @return Total count of the word.
     */
    @Override
    public int totalCount(String location) {
        lock.readLock().lock();
        int count = super.totalCount(location);
        lock.readLock().unlock();

        return count;
    }

    /**
     * Provides an unmodifiable view of the words in the index.
     *
     * @return An unmodifiable set of words.
     */
    @Override
    public Set<String> viewWords() {
        Set<String> words;
        lock.readLock().lock();
        words = super.viewWords();
        lock.readLock().unlock();

        return words;
    }

    /**
     * Provides an unmodifiable view of the locations a specific word appears in.
     *
     * @param word The word to check.
     * @return An unmodifiable set of locations for the given word.
     */
    @Override
    public Set<String> viewLocations(String word) {
        Set<String> res;

        lock.readLock().lock();
        res = viewLocations(word);
        lock.readLock().unlock();

        return res;
    }

    /**
     * Provides an unmodifiable view of the positions a specific word appears at in a specific location.
     *
     * @param word     The word to check.
     * @param location The location to check.
     * @return An unmodifiable set of positions for the given word in the given location.
     */
    @Override
    public Set<Integer> viewPositions(String word, String location) {
        Set<Integer> res;

        lock.readLock().lock();
        res = super.viewPositions(word, location);
        lock.readLock().unlock();

        return res;
    }


    /**
     * Provides a direct view of the word count map.
     *
     * @return An unmodifiable view of the internal word count structure.
     */
    @Override
    public Map<String, Integer> viewCount() {
        lock.readLock().lock();
        Map<String, Integer> map = super.viewCount();
        lock.readLock().unlock();

        return map;
    }

    /**
     * Performs an exact search for each query in the provided set.
     * This method looks for exact matches of the query words in the index and
     * aggregates the results into a sorted list of QueryResult objects.
     * The sorting is based on the relevance score, occurrence count, and location.
     *
     * @param queries A set of queries to search for.
     * @return A sorted list of QueryResult objects representing the search results.
     */
    @Override
    public List<QueryResult> exactSearch(Set<String> queries) {
        lock.readLock().lock();
        List<QueryResult> results = super.exactSearch(queries);
        lock.readLock().unlock();

        return results;
    }

    /**
     * Performs a partial search for each query in the provided set.
     * A partial search considers any index word that starts with the given query string.
     * For each matching index word, the method aggregates the search results,
     * ultimately returning them as a sorted list of QueryResult objects.
     * The sorting is based on the relevance score, occurrence count, and location.
     *
     * @param queries A set of queries to search for.
     * @return A sorted list of QueryResult objects representing the search results.
     */
    @Override
    public List<QueryResult> partialSearch(Set<String> queries) {
        lock.readLock().lock();
        List<QueryResult> results = super.partialSearch(queries);
        lock.readLock().unlock();

        return results;
    }
}
