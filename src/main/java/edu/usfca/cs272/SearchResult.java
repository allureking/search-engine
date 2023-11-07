package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
	private final TreeMap<String, List<QueryResult>> searchResults;

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
        searchResults.get(query).add(new QueryResult(count, score, location));
    }

    /**
     * Sorts the values of the search result for a given query.
     *
     * @param query The query string.
     */
    public void sortValues(String query) {
        if (searchResults.containsKey(query)) {
            Collections.sort(searchResults.get(query));
        }
    }

    /**
     * Saves the search results to an output file.
     *
     * @param outputPath The path to the output file.
     * @throws IOException If there's an issue writing to the output file.
     */
    public void saveToOutput(Path outputPath) throws IOException {
        Map<String, Collection<Map<String, Object>>> elements = new TreeMap<>();
        for (Map.Entry<String, List<QueryResult>> entry: searchResults.entrySet()) {
            List<Map<String, Object>> list = new ArrayList<>();
            elements.put(entry.getKey(), list);
            for (QueryResult result : entry.getValue()) {
                list.add(result.toMap());
            }
        }
        JsonWriter.writeObjectArrayObject(elements, outputPath);
    }
}
