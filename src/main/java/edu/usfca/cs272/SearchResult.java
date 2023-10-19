package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * class to represent search results for queries
 */
public class SearchResult {
    private final TreeMap<String, Collection<Map<String, Object>>> searchResults;

    public SearchResult() {
        searchResults = new TreeMap<>();
    }

    /**
     * add a new query
     * @param query
     */
    public void addQuery(String query) {
        searchResults.put(query, new ArrayList());
    }

    /**
     * add a key value pair to the search result
     * @param query
     * @param count
     * @param score
     * @param location
     */
    public void addKeyValue(String query, int count, double score, String location) {
        TreeMap<String, Object> map = new TreeMap();
        map.put("count", count);
        map.put("score", String.format("%.8f", score));
        map.put("where", "\"" + location + "\"");

        searchResults.get(query).add(map);
    }

    /**
     * sort values
     * @param query
     */
    public void sortValues(String query) {
        if (searchResults.containsKey(query)) {
            Collections.sort((List) searchResults.get(query), new Comparator<Map<String, Object>>() {
                @Override
                public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                    int res = ((String)o2.get("score")).compareTo((String) o1.get("score"));
                    if (res == 0) {
                        res = ((Integer)o2.get("count")).compareTo((Integer)o1.get("count"));
                        if (res == 0) {
                            return ((String)o1.get("where")).compareTo((String) o2.get("where"));
                        }
                    }

                    return res;
                }
            });
        }
    }

    /**
     * save results
     * @param outputPath
     * @throws IOException
     */
    public void saveToOutput(Path outputPath) throws IOException {
        JsonWriter.writeObjectArrayObject(searchResults, outputPath);
    }
}
