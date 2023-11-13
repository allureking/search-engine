package edu.usfca.cs272;

import java.util.Map;
import java.util.TreeMap;

/**
 * Represents a query result, encapsulating the count of occurrences,
 * a relevance score, and the location where the result was found.
 */
public class QueryResult implements Comparable<QueryResult> { // TODO Nested inside of inverted index... decide whether to use the static keyword!
	/**
	 * The occurrenceCount is the number of times a query term appears. This is used
	 * to measure how often a term is encountered within a particular dataset or document.
	 */
	private int count;

	/**
	 * The relevanceScore is a metric that quantifies how relevant a query result is
	 * to the search query. Typically, higher scores indicate more relevance.
	 */
	private double score;

	/**
	 * The location is a string identifier for where the query result was found.
	 * This could represent a URL, a file path, or any other location identifier.
	 */
	private final String location;


    /**
     * Constructs a QueryResult with the specified location.
     *
     * @param location    the location associated with the query result
     */
    public QueryResult(String location) {
        this.location = location;
    }

    /**
     * Updates the occurrence count and recalculates the relevance score for this query result.
     * The relevance score is calculated as the ratio of this query result's count to the total count.
     *
     * @param total The total occurrence count across all query results.
     * @param count The additional occurrence count to add to this query result.
     */
    public void updateCount(int total, int count) {
        this.count += count;
        score = total == 0 ? 0.0 : this.count / (double) total;
    }

    /**
     * Returns the occurrence count for this query result.
     *
     * @return the occurrence count
     */
    public int getCount() {
        return count;
    }

    /**
     * Returns the relevance score for this query result.
     *
     * @return the relevance score
     */
    public double getScore() {
        return score;
    }

    /**
     * Returns the location for this query result.
     *
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Converts the QueryResult to a Map representation for JSON serialization.
     * The relevance score is formatted to 8 decimal places.
     *
     * @return a TreeMap containing the properties of this QueryResult
     */
    public Map<String, Object> toMap() {
        TreeMap<String, Object> map = new TreeMap<>();
        map.put("count", count);
        map.put("score", String.format("%.8f", score));
        map.put("where", "\"" + location + "\"");

        return map;
    }

    /**
     * Compares this QueryResult with another QueryResult for order.
     * Ordering is primarily by relevance score, then by occurrence count, and finally by location.
     *
     * @param other the QueryResult to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object
     */
    @Override
    public int compareTo(QueryResult other) {
        int result = Double.compare(other.score, score);
        if (result == 0) {
            result = Integer.compare(other.count, count);
            if (result == 0) {
                result = location.compareTo(other.location);
            }
        }
        return result;
    }
}
