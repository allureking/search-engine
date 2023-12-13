package edu.usfca.cs272;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class SearchServlet extends HttpServlet {
    // Assume that the inverted index is stored in some data structure
    // and the Driver.main method has populated it.

    private final SearchProcessorInterface searchProcessor;
    private List<String> searchHistory = new ArrayList<>();
    private List<String> visitedResults = new ArrayList<>();
    private List<String> favoriteResults = new ArrayList<>();

    private Date lastVisited;
    private String searchResultTemplate = """
                <div class="search-result">
                    <h3><a href="%s" target="_blank">%s</a></h3>
                    <p class="score">Score: %s  and  Count: %s</p>
                </div>
            """;

    public SearchServlet(SearchProcessorInterface searchProcessor) {
        this.searchProcessor = searchProcessor;
        lastVisited = new Date();
    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String action = request.getParameter("action");
        lastVisited = new Date();
        if ("search".equals(action)) {
            serach(request, response);
        } else if ("viewHistory".equals(action)) {
            showSearchHistory(response);
        } else if ("viewVisited".equals(action)) {
            showVisitedResults(response);
        } else if ("viewFavorites".equals(action)) {
            showFavoriteResults(response);
        } else {
            serach(request, response);
        }

    }

    private void serach(HttpServletRequest request, HttpServletResponse response) throws IOException{
        long startTime = System.currentTimeMillis();
        response.setContentType("text/html;charset=utf-8");
        Path BASE = Path.of("src", "main", "resources");

        // Retrieve the search query from the HTML form
        String query = request.getParameter("query");
        searchHistory.add(query);
        System.out.println("Query: " + query);
        searchProcessor.search(query);
        List<InvertedIndex.QueryResult> results = searchProcessor.getSearchResult(query);

        PrintWriter out = response.getWriter();

        String templateString = Files.readString(BASE.resolve("index.html"), StandardCharsets.UTF_8);
        StringBuilder searchResults = new StringBuilder();
        for (InvertedIndex.QueryResult result : results) {
            String resultString = String.format(searchResultTemplate, result.getLocation(), result.getLocation(), result.getScore(), result.getCount());
            searchResults.append(resultString);
        }
        templateString = templateString.replace("<!-- search-results -->", searchResults.toString());
        templateString = templateString.replace("<!-- Last Visited -->", lastVisited.toString());
        //<!-- Total Results -->
        templateString = templateString.replace("<!-- Total Results -->", String.valueOf(results.size()));
        //<!-- Time Taken -->
        long endTime = System.currentTimeMillis();
        templateString = templateString.replace("<!-- Time Taken -->", String.valueOf(endTime - startTime));
        out.println(templateString);
    }

    private void showSearchHistory(HttpServletResponse response) throws IOException {
        response.getWriter().println("<html><head><title>Search History</title></head><body>");
        response.getWriter().println("<h2>Search History:</h2>");
        for (String query : searchHistory) {
            response.getWriter().println("<p>" + query + "</p>");
        }
        response.getWriter().println("</body></html>");
    }

    private void showVisitedResults(HttpServletResponse response) throws IOException {
        response.getWriter().println("<html><head><title>Visited Results</title></head><body>");
        response.getWriter().println("<h2>Visited Results:</h2>");
        for (String result : visitedResults) {
            response.getWriter().println("<p>" + result + "</p>");
        }
        response.getWriter().println("</body></html>");
    }

    private void showFavoriteResults(HttpServletResponse response) throws IOException {
        response.getWriter().println("<html><head><title>Favorite Results</title></head><body>");
        response.getWriter().println("<h2>Favorite Results:</h2>");
        for (String result : favoriteResults) {
            response.getWriter().println("<p>" + result + "</p>");
        }
        response.getWriter().println("</body></html>");
    }
}

