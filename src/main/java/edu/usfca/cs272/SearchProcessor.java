package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Processor for searching words from a file.
 */
public class SearchProcessor implements SearchProcessorInterface {
    /**
     * Stores search results mapped by query strings.
     */
    private final TreeMap<String, List<InvertedIndex.QueryResult>> searchResults;

    /**
     * A functional interface representing the search operation. It takes a set of query terms
     * and returns a collection of {@link InvertedIndex.QueryResult} objects representing the search results.
     * This function abstracts the search logic, allowing for different search implementations
     * (e.g., exact or partial) to be used interchangeably.
     */
    private final Function<Set<String>, List<InvertedIndex.QueryResult>> searchFunction;

    /**
     * Stemmer instance used for normalizing words during the search process.
     */
    private final Stemmer stemmer;

    /**
     * Constructs a SearchProcessor with a reference to an InvertedIndex and a flag indicating
     * whether to perform partial search. Initializes the stemmer for word normalization and
     * sets the appropriate search function based on the search type.
     *
     * @param index The InvertedIndex to use for searching.
     * @param partial True to perform partial search, false for exact search.
     */
    public SearchProcessor(InvertedIndex index, boolean partial) {
        searchResults = new TreeMap<>();
        stemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);

        if (partial) {
            searchFunction = index::partialSearch;
        } else {
            searchFunction = index::exactSearch;
        }
    }

    @Override
    public String toString() {
        return "SearchProcessor{" +
                "searchResults=" + searchResults +
                ", stemmer=" + stemmer +
                '}';
    }

    @Override
    public void search(String line, Stemmer stemmer) {
        if (line.isEmpty()) {
            return;
        }

        TreeSet<String> queries = FileStemmer.uniqueStems(line, stemmer);
        if (queries.isEmpty()) {
            return;
        }

        search(queries);
    }

    @Override
    public void search(String line) {
    	search(line, this.stemmer);
    }

    @Override
    public void search(Set<String> queries) {
        String queryWords = String.join(" ", queries);

        if (searchResults.containsKey(queryWords)) {
            return;
        }
        searchResults.put(queryWords, searchFunction.apply(queries));
    }

    @Override
    public void saveResult(Path output) throws IOException {
        JsonWriter.writeObjectArrayObject(searchResults, output);
    }

    @Override
    public List<InvertedIndex.QueryResult> getSearchResult(String query) {
        TreeSet<String> normalizedQuery = FileStemmer.uniqueStems(query, this.stemmer);
        String normalizedKey = String.join(" ", normalizedQuery);

        if (searchResults.containsKey(normalizedKey)) {
            return Collections.unmodifiableList(searchResults.get(normalizedKey));
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Set<String> getAllQueries() {
        return Collections.unmodifiableSet(searchResults.keySet());
    }
}
