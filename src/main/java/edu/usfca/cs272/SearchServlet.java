package edu.usfca.cs272;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.text.StringEscapeUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet that handles search requests, search history, and index downloads
 * for the search engine web interface.
 *
 * @author Honghuai Ke
 */
public class SearchServlet extends HttpServlet {
    /** Class version for serialization, in [YEAR][TERM] format (unused). */
    private static final long serialVersionUID = 202308;

    /**
     * Processor for handling exact search queries.
     */
    private final SearchProcessorInterface exactSearchProcessor;

    /**
     * Processor for handling partial search queries.
     */
    private final SearchProcessorInterface partialSearchProcessor;

    /**
     * The inverted index used for searching.
     */
    private final InvertedIndex invertedIndex;

    /**
     * List to store the history of search queries.
     */
    private final List<String> searchHistory = new ArrayList<>();

    /**
     * Records the last time the servlet was visited.
     */
    private Date lastVisited;

    /**
     * Constructs a SearchServlet with specified search processors and an inverted index.
     *
     * @param searchProcessor The search processor for exact searches.
     * @param partialSearchProcessor The search processor for partial searches.
     * @param invertedIndex The inverted index used for searching.
     */
    public SearchServlet(SearchProcessorInterface searchProcessor, SearchProcessorInterface partialSearchProcessor, InvertedIndex invertedIndex) {
        this.exactSearchProcessor = searchProcessor;
        this.partialSearchProcessor = partialSearchProcessor;
        this.invertedIndex = invertedIndex;
        lastVisited = new Date();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Determine the action to take based on the request parameter
        String action = request.getParameter("action");
        synchronized (this){
            lastVisited = new Date(); // Update the last visited time
        }
        if ("search".equals(action)) {
            search(request, response); // Perform a search action
        } else if ("viewHistory".equals(action)) {
            showSearchHistory(response); // Show the search history
        } else if ("clearHistory".equals(action)) {
            clearSearchHistory(response); // Clear the search history
        } else if ("download".equals(action)) {
            download(response); // Download the inverted index
        } else {
            search(request, response); // Default to search action
        }

    }

    /**
     * Processes the search request and generates search results.
     *
     * @param request The HttpServletRequest object.
     * @param response The HttpServletResponse object.
     * @throws IOException If an IO error occurs.
     */
    private void search(HttpServletRequest request, HttpServletResponse response) throws IOException{
        long startTime = System.currentTimeMillis(); // Start timing the search operation
        response.setContentType("text/html;charset=utf-8"); // Set response content type
        Path base = Path.of("src", "main", "resources");

        // Retrieve the search query from the HTML form
        String query = request.getParameter("query");
        String partial = request.getParameter("partial");
        String reverse = request.getParameter("reverse");

        if (query == null) {
            query = "";
        }
        // Escape HTML special characters in the query
        query = StringEscapeUtils.escapeHtml4(query);

        synchronized (searchHistory) {
            searchHistory.add(query);
        }
        List<InvertedIndex.QueryResult> results = new ArrayList<>();
        if (partial != null){
            synchronized (partialSearchProcessor) {
                partialSearchProcessor.search(query);
                results = partialSearchProcessor.getSearchResult(query);
            }
        } else{
            synchronized (exactSearchProcessor) {
                exactSearchProcessor.search(query);
                results = exactSearchProcessor.getSearchResult(query);
            }
        }
        if (reverse != null){
            results = new ArrayList<>(results);
            Collections.reverse(results);
        }
        PrintWriter out = response.getWriter();

        // Build the search results HTML using the template
        String templateString = Files.readString(base.resolve("index.html"), StandardCharsets.UTF_8);
        StringBuilder searchResults = new StringBuilder();
        for (InvertedIndex.QueryResult result : results) {

            String searchResultTemplate = """
                        <div class="search-result">
                            <h3><a href="%s" target="_blank">%s</a></h3>
                            <p class="score">Score: %s  and  Count: %s</p>
                        </div>
                    """;
            // Escape HTML special characters in the search result data
            String location = StringEscapeUtils.escapeHtml4(result.getLocation());
            String score = StringEscapeUtils.escapeHtml4(String.format("%.4f", Math.max(result.getScore(), 0.0001)));
            String count = StringEscapeUtils.escapeHtml4(String.valueOf(result.getCount()));
            String resultString = String.format(searchResultTemplate, location, location, score, count);
            searchResults.append(resultString);
        }
        // Replace placeholders in the HTML template with actual search data
        templateString = templateString.replace("name=\"query\" value=\"\"", "name=\"query\" value=\""+query+"\"");
        // <!-- search-results -->
        templateString = templateString.replace("<!-- search-results -->", searchResults.toString());
        // <!-- Last Visited -->
        templateString = templateString.replace("<!-- Last Visited -->", lastVisited.toString());
        //<!-- Total Results -->
        templateString = templateString.replace("<!-- Total Results -->", String.valueOf(results.size()));
        //<!-- Time Taken -->
        long endTime = System.currentTimeMillis();
        templateString = templateString.replace("<!-- Time Taken -->", String.valueOf(endTime - startTime));
        // Send the response to the client
        out.println(templateString);
    }

    /**
     * Displays the search history on the web page.
     *
     * @param response The HttpServletResponse object.
     * @throws IOException If an IO error occurs.
     */
    private void showSearchHistory(HttpServletResponse response) throws IOException {
        response.getWriter().println("<html><head><title>Search History</title></head><body>");
        response.getWriter().println("<h2>Search History:</h2>");
        for (String query : searchHistory) {
            response.getWriter().println("<p>" + query + "</p>");
        }
        response.getWriter().println("<a href=\"/index.html?action=clearHistory\">Clear Search History</a>");
        response.getWriter().println("<a href=\"/index.html\">Back to Home</a>");
        response.getWriter().println("</body></html>");

    }

    /**
     * Clears the stored search history.
     *
     * @param response The HttpServletResponse object.
     * @throws IOException If an IO error occurs.
     */
    private void clearSearchHistory(HttpServletResponse response) throws IOException {
        synchronized (searchHistory){
            searchHistory.clear();
        }
        response.sendRedirect("/index.html?action=viewHistory");
    }

    /**
     * Allows the user to download the current state of the inverted index.
     *
     * @param response The HttpServletResponse object.
     * @throws IOException If an IO error occurs.
     */
    private void download(HttpServletResponse response) throws IOException {
        String filePath = "index.json";
        synchronized (invertedIndex) {
            invertedIndex.saveIndex(Path.of(filePath));
        }
        File file = new File(filePath);

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
        response.setContentLength((int) file.length());

        try (FileInputStream in = new FileInputStream(file); OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }
}