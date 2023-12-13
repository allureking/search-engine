package edu.usfca.cs272;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SearchServlet extends HttpServlet {
    // Assume that the inverted index is stored in some data structure
    // and the Driver.main method has populated it.

    private final SearchProcessorInterface searchProcessor;

    public SearchServlet(SearchProcessorInterface searchProcessor) {
        this.searchProcessor = searchProcessor;
    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=utf-8");
        Path BASE = Path.of("src", "main", "resources");
        String templateString = Files.readString(BASE.resolve("index.html"), StandardCharsets.UTF_8);

        // Retrieve the search query from the HTML form
        String query = request.getParameter("query");
        System.out.println("Query: " + query);
        searchProcessor.search(query);
        List<InvertedIndex.QueryResult> results = searchProcessor.getSearchResult(query);

        PrintWriter out = response.getWriter();
        out.println(templateString);
        // Display search results on a web page
        out.println("<h2>Search Results:</h2>");
        for (InvertedIndex.QueryResult result : results) {
            response.getWriter().println("<p><a href='" + result.getLocation() + "'>" + result.getLocation() + "</a></p>");
        }

    }


}

