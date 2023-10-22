package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Represents search results for queries.
 */
public class SearchResult {
    /**
     * Stores search results mapped by query strings.
     */
    private final TreeMap<String, Collection<Map<String, Object>>> searchResults; // TODO Combine this with the Search Processor

    /**
     *
     */
    public SearchResult() {
        searchResults = new TreeMap<>();
    }

    /**
     * Adds a new query to the search results.
     *
     * @param query The query to be added.
     */
    public void addQuery(String query) {
        searchResults.put(query, new ArrayList<>());
    }

    /**
     * Adds a key-value pair to the search result for a given query.
     *
     * @param query    The query string.
     * @param count    The count of the result.
     * @param score    The score of the result.
     * @param location The location of the result.
     */
    public void addKeyValue(String query, int count, double score, String location) {
    	// TODO Create a class for this with count, score, and where members/variables
        TreeMap<String, Object> map = new TreeMap<>();
        map.put("count", count);
        map.put("score", String.format("%.8f", score));
        map.put("where", "\"" + location + "\"");

        searchResults.get(query).add(map);
    }

    /**
     * Sorts the values of the search result for a given query.
     *
     * @param query The query string.
     */
    public void sortValues(String query) {
        if (searchResults.containsKey(query)) {
            Collections.sort((List<Map<String, Object>>) searchResults.get(query), new Comparator<Map<String, Object>>() {
                @Override
                public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                    int res = ((String) o2.get("score")).compareTo((String) o1.get("score"));
                    if (res == 0) {
                        res = ((Integer) o2.get("count")).compareTo((Integer) o1.get("count"));
                        if (res == 0) {
                            return ((String) o1.get("where")).compareTo((String) o2.get("where"));
                        }
                    }
                    return res;
                }
            });
        }
    }

    /**
     * Saves the search results to an output file.
     *
     * @param outputPath The path to the output file.
     * @throws IOException If there's an issue writing to the output file.
     */
    public void saveToOutput(Path outputPath) throws IOException {
        JsonWriter.writeObjectArrayObject(searchResults, outputPath);
    }
}
