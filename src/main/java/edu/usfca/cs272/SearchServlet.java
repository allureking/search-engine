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

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class SearchServlet extends HttpServlet {
    // Assume that the inverted index is stored in some data structure
    // and the Driver.main method has populated it.

    private final SearchProcessorInterface exactSearchProcessor;
    private final SearchProcessorInterface partialSearchProcessor;

    private final InvertedIndex invertedIndex;
    private List<String> searchHistory = new ArrayList<>();

    private Date lastVisited;
    private String searchResultTemplate = """
                <div class="search-result">
                    <h3><a href="%s" target="_blank">%s</a></h3>
                    <p class="score">Score: %s  and  Count: %s</p>
                </div>
            """;

    public SearchServlet(SearchProcessorInterface searchProcessor, SearchProcessorInterface partialSearchProcessor, InvertedIndex invertedIndex) {
        this.exactSearchProcessor = searchProcessor;
        this.partialSearchProcessor = partialSearchProcessor;
        this.invertedIndex = invertedIndex;
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
        } else if ("clearHistory".equals(action)) {
            clearSearchHistory(response);
        } else if ("download".equals(action)) {
            download(response);
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
        String partial = request.getParameter("partial");
        String reverse = request.getParameter("reverse");
        if (query == null) {
            query = " ";
        }
        searchHistory.add(query);
        System.out.println("Query: " + query + " partial: " + partial + " reverse: " + reverse);
        List<InvertedIndex.QueryResult> results = new ArrayList<>();
        if (partial != null){
            partialSearchProcessor.search(query);
            results = partialSearchProcessor.getSearchResult(query);
        } else{
            exactSearchProcessor.search(query);
            results = exactSearchProcessor.getSearchResult(query);
        }
        if (reverse != null){
            results = new ArrayList<>(results);
           Collections.reverse(results);
        }
        PrintWriter out = response.getWriter();

        String templateString = Files.readString(BASE.resolve("index.html"), StandardCharsets.UTF_8);
        StringBuilder searchResults = new StringBuilder();
        for (InvertedIndex.QueryResult result : results) {
            String resultString = String.format(searchResultTemplate, result.getLocation(), result.getLocation(), result.getScore(), result.getCount());
            searchResults.append(resultString);
        }
        templateString = templateString.replace("name=\"query\" value=\"\"", "name=\"query\" value=\""+query+"\"");
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
        response.getWriter().println("<a href=\"/index.html?action=clearHistory\">Clear Search History</a>");
        response.getWriter().println("<a href=\"/index.html\">Back to Home</a>");
        response.getWriter().println("</body></html>");

    }

    private void clearSearchHistory(HttpServletResponse response) throws IOException {
        searchHistory.clear();
        response.sendRedirect("/index.html?action=viewHistory");
    }

    private void download(HttpServletResponse response) throws IOException {
        String filePath = "index.json";
        invertedIndex.saveIndex(Path.of(filePath));
        File file = new File(filePath);

        // 设置响应头
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

