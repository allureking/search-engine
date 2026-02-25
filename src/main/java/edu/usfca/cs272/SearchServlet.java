package edu.usfca.cs272;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.text.StringEscapeUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet that handles search requests, search history, crawling, and index downloads
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
     * Work queue for multithreaded crawling, or null for single-threaded mode.
     */
    private final WorkQueue workQueue;

    /**
     * List to store the history of search queries.
     */
    private final List<String> searchHistory = new ArrayList<>();

    /**
     * Records the last time the servlet was visited.
     */
    private Date lastVisited;

    /**
     * The crawler currently in progress, or null if idle.
     */
    private volatile CrawlerProcessorInterface activeCrawler;

    /**
     * The background thread running the current crawl, or null if idle.
     */
    private volatile Thread crawlThread;

    /**
     * The time the server started, used for uptime calculation.
     */
    private final Instant startTime;

    /**
     * Counter for total search queries served.
     */
    private final AtomicInteger totalQueries;

    /**
     * Constructs a SearchServlet with specified search processors, an inverted index,
     * and an optional work queue for crawling support.
     *
     * @param searchProcessor The search processor for exact searches.
     * @param partialSearchProcessor The search processor for partial searches.
     * @param invertedIndex The inverted index used for searching.
     * @param workQueue The work queue for multithreaded crawling, or null.
     */
    public SearchServlet(SearchProcessorInterface searchProcessor, SearchProcessorInterface partialSearchProcessor,
                         InvertedIndex invertedIndex, WorkQueue workQueue) {
        this.exactSearchProcessor = searchProcessor;
        this.partialSearchProcessor = partialSearchProcessor;
        this.invertedIndex = invertedIndex;
        this.workQueue = workQueue;
        lastVisited = new Date();
        startTime = Instant.now();
        totalQueries = new AtomicInteger(0);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Determine the action to take based on the request parameter
        String action = request.getParameter("action");
        synchronized (this){
            lastVisited = new Date(); // Update the last visited time
        }
        if ("search".equals(action)) {
            search(request, response);
        } else if ("lucky".equals(action)) {
            lucky(request, response);
        } else if ("crawl".equals(action)) {
            crawl(request, response);
        } else if ("crawlStatus".equals(action)) {
            crawlStatus(response);
        } else if ("stats".equals(action)) {
            serverStats(response);
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
        totalQueries.incrementAndGet();
        long startTime = System.currentTimeMillis(); // Start timing the search operation
        response.setContentType("text/html;charset=utf-8"); // Set response content type
        Path base = Path.of("src", "main", "resources");

        // Retrieve the search query from the HTML form
        String query = request.getParameter("query");
        String exact = request.getParameter("exact");

        if (query == null) {
            query = "";
        }
        // Escape HTML special characters in the query
        query = StringEscapeUtils.escapeHtml4(query);

        synchronized (searchHistory) {
            searchHistory.add(query);
        }
        List<InvertedIndex.QueryResult> results = new ArrayList<>();
        if (exact != null) {
            synchronized (exactSearchProcessor) {
                exactSearchProcessor.search(query);
                results = exactSearchProcessor.getSearchResult(query);
            }
        } else {
            synchronized (partialSearchProcessor) {
                partialSearchProcessor.search(query);
                results = partialSearchProcessor.getSearchResult(query);
            }
        }
        PrintWriter out = response.getWriter();

        // Build the search results HTML using the template
        String templateString = Files.readString(base.resolve("index.html"), StandardCharsets.UTF_8);
        StringBuilder searchResults = new StringBuilder();
        for (InvertedIndex.QueryResult result : results) {

            String searchResultTemplate = """
                        <div class="search-result">
                            <h3><a href="%s" target="_blank">%s</a></h3>
                            <p class="score"><span class="pill">Score: %s</span><span class="pill">Count: %s</span></p>
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
        //<!-- Indexed Pages -->
        templateString = templateString.replace("<!-- Indexed Pages -->", String.valueOf(invertedIndex.viewCount().size()));
        //<!-- crawl-active -->
        templateString = templateString.replace("<!-- crawl-active -->", activeCrawler != null ? "true" : "false");
        // Send the response to the client
        out.println(templateString);
    }

    /**
     * Handles a crawl request from the web interface. Accepts a seed URL, optional
     * page count, and thread count. For multithreaded mode, crawls asynchronously
     * with progress tracking via a per-crawl WorkQueue.
     *
     * @param request The HttpServletRequest object.
     * @param response The HttpServletResponse object.
     * @throws IOException If an IO error occurs.
     */
    private void crawl(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        PrintWriter out = response.getWriter();

        if (activeCrawler != null) {
            out.print("{\"status\":\"busy\"}");
            return;
        }

        String seedUrl = request.getParameter("url");
        String countStr = request.getParameter("crawl");
        String threadStr = request.getParameter("threads");

        if (seedUrl == null || seedUrl.isBlank()) {
            out.print("{\"status\":\"error\",\"message\":\"No URL provided\"}");
            return;
        }

        int crawlCount = 50;
        if (countStr != null && !countStr.isBlank()) {
            try {
                crawlCount = Integer.parseInt(countStr);
                crawlCount = Math.max(1, Math.min(200, crawlCount));
            } catch (NumberFormatException e) {
                crawlCount = 50;
            }
        }

        int threadCount = 5;
        if (threadStr != null && !threadStr.isBlank()) {
            try {
                threadCount = Integer.parseInt(threadStr);
                threadCount = Math.max(1, Math.min(10, threadCount));
            } catch (NumberFormatException e) {
                threadCount = 5;
            }
        }

        URI uri = LinkFinder.makeUri(seedUrl);
        if (uri == null) {
            out.print("{\"status\":\"error\",\"message\":\"Invalid URL\"}");
            return;
        }

        URL url;
        try {
            url = LinkFinder.cleanUri(uri).toURL();
        } catch (MalformedURLException e) {
            out.print("{\"status\":\"error\",\"message\":\"Invalid URL\"}");
            return;
        }

        if (invertedIndex instanceof ThreadSafeInvertedIndex threadSafeIndex) {
            WorkQueue crawlQueue = new WorkQueue(threadCount);
            MultiThreadCrawlerProcessor crawler = new MultiThreadCrawlerProcessor(
                    crawlQueue, threadSafeIndex, crawlCount);
            activeCrawler = crawler;

            final URL seedURL = url;
            crawlThread = new Thread(() -> {
                try {
                    crawler.crawl(seedURL);
                    crawlQueue.join();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    activeCrawler = null;
                    crawlThread = null;
                }
            }, "CrawlThread");
            crawlThread.setDaemon(true);
            crawlThread.start();
            out.print("{\"status\":\"started\"}");
        } else {
            CrawlerProcessor crawler = new CrawlerProcessor(invertedIndex, crawlCount);
            crawler.crawl(url);
            out.print("{\"status\":\"done\"}");
        }
    }

    /**
     * Returns JSON with current crawl progress status.
     *
     * @param response The HttpServletResponse object.
     * @throws IOException If an IO error occurs.
     */
    private void crawlStatus(HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        PrintWriter out = response.getWriter();

        CrawlerProcessorInterface crawler = activeCrawler;
        if (crawler != null) {
            int processed = crawler.getTotalProcessed();
            int total = crawler.getTotalCrawl();
            out.printf("{\"status\":\"crawling\",\"processed\":%d,\"total\":%d}", processed, total);
        } else {
            out.print("{\"status\":\"idle\"}");
        }
    }

    /**
     * Performs a "I'm Feeling Lucky" search — redirects directly to the top result.
     * Falls back to the normal search page if no results are found.
     *
     * @param request The HttpServletRequest object.
     * @param response The HttpServletResponse object.
     * @throws IOException If an IO error occurs.
     */
    private void lucky(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String query = request.getParameter("query");
        if (query == null || query.isBlank()) {
            search(request, response);
            return;
        }

        query = StringEscapeUtils.escapeHtml4(query);
        String exact = request.getParameter("exact");
        List<InvertedIndex.QueryResult> results;

        if (exact != null) {
            synchronized (exactSearchProcessor) {
                exactSearchProcessor.search(query);
                results = exactSearchProcessor.getSearchResult(query);
            }
        } else {
            synchronized (partialSearchProcessor) {
                partialSearchProcessor.search(query);
                results = partialSearchProcessor.getSearchResult(query);
            }
        }

        if (!results.isEmpty()) {
            response.setContentType("text/html;charset=utf-8");
            PrintWriter out = response.getWriter();
            String url = StringEscapeUtils.escapeHtml4(results.get(0).getLocation());
            out.println("<!DOCTYPE html><html><head><meta http-equiv=\"refresh\" content=\"0;url="
                    + url + "\"></head><body></body></html>");
        } else {
            search(request, response);
        }
    }

    /**
     * Returns a server statistics page showing uptime, index size, and query count.
     *
     * @param response The HttpServletResponse object.
     * @throws IOException If an IO error occurs.
     */
    private void serverStats(HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();

        long uptimeSeconds = java.time.Duration.between(startTime, Instant.now()).getSeconds();
        long hours = uptimeSeconds / 3600;
        long minutes = (uptimeSeconds % 3600) / 60;
        long seconds = uptimeSeconds % 60;
        String uptime = String.format("%dh %dm %ds", hours, minutes, seconds);

        int indexedPages = invertedIndex.viewCount().size();
        int totalWords = invertedIndex.viewWords().size();
        int queries = totalQueries.get();

        out.println("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>Server Stats | WebDepth</title>
              <link rel="preconnect" href="https://fonts.googleapis.com">
              <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
              <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500&display=swap" rel="stylesheet">
              <style>
                *{margin:0;padding:0;box-sizing:border-box}
                :root{--bg:#09090b;--surface:#131316;--border:#1e1e24;--text:#e4e4e7;--text-dim:#9295a0;--text-muted:#55576a;--accent:#818cf8;--accent-dim:rgba(129,140,248,0.08);--white:#f4f4f5;--font:'Inter',sans-serif;--mono:'JetBrains Mono',monospace}
                body{font-family:var(--font);background:var(--bg);color:var(--text-dim);min-height:100vh;-webkit-font-smoothing:antialiased}
                body::before{content:'';position:fixed;inset:0;background-image:radial-gradient(circle,var(--border) 1px,transparent 1px);background-size:32px 32px;opacity:0.35;pointer-events:none;z-index:0}
                .page{max-width:600px;margin:0 auto;padding:80px 24px;position:relative;z-index:1}
                h1{font-size:24px;font-weight:700;color:var(--white);margin-bottom:32px;letter-spacing:-0.02em}
                .grid{display:grid;grid-template-columns:repeat(2,1fr);gap:12px;margin-bottom:32px}
                .card{background:var(--surface);border:1px solid var(--border);border-radius:10px;padding:20px;transition:border-color 0.2s}
                .card:hover{border-color:var(--accent)}
                .card-label{font-family:var(--mono);font-size:11px;color:var(--text-muted);text-transform:uppercase;letter-spacing:0.04em;margin-bottom:8px}
                .card-value{font-family:var(--mono);font-size:22px;font-weight:700;color:var(--accent)}
                .actions{display:flex;gap:16px;margin-top:24px}
                .actions a{font-family:var(--mono);font-size:13px;color:var(--accent);padding:8px 16px;border:1px solid var(--border);border-radius:6px;text-decoration:none;transition:all 0.2s}
                .actions a:hover{border-color:var(--accent);background:var(--accent-dim)}
              </style>
            </head>
            <body>
              <div class="page">
                <h1>Server Statistics</h1>
                <div class="grid">
            """);

        out.printf("""
                  <div class="card"><div class="card-label">Uptime</div><div class="card-value">%s</div></div>
                  <div class="card"><div class="card-label">Queries Served</div><div class="card-value">%d</div></div>
                  <div class="card"><div class="card-label">Indexed Pages</div><div class="card-value">%d</div></div>
                  <div class="card"><div class="card-label">Indexed Words</div><div class="card-value">%d</div></div>
            """, uptime, queries, indexedPages, totalWords);

        out.println("""
                </div>
                <div class="actions">
                  <a href="index.html">Back to Search</a>
                </div>
              </div>
            </body>
            </html>
            """);
    }

    /**
     * Displays the search history on the web page with styled dark theme.
     *
     * @param response The HttpServletResponse object.
     * @throws IOException If an IO error occurs.
     */
    private void showSearchHistory(HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();
        out.println("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>Search History | WebDepth</title>
              <link rel="preconnect" href="https://fonts.googleapis.com">
              <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
              <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500&display=swap" rel="stylesheet">
              <style>
                *{margin:0;padding:0;box-sizing:border-box}
                :root{--bg:#09090b;--surface:#131316;--border:#1e1e24;--text:#e4e4e7;--text-dim:#9295a0;--text-muted:#55576a;--accent:#818cf8;--accent-dim:rgba(129,140,248,0.08);--white:#f4f4f5;--font:'Inter',sans-serif;--mono:'JetBrains Mono',monospace}
                body{font-family:var(--font);background:var(--bg);color:var(--text-dim);min-height:100vh;-webkit-font-smoothing:antialiased}
                body::before{content:'';position:fixed;inset:0;background-image:radial-gradient(circle,var(--border) 1px,transparent 1px);background-size:32px 32px;opacity:0.35;pointer-events:none;z-index:0}
                .page{max-width:600px;margin:0 auto;padding:80px 24px;position:relative;z-index:1}
                h1{font-size:24px;font-weight:700;color:var(--white);margin-bottom:24px;letter-spacing:-0.02em}
                .entry{padding:12px 16px;background:var(--surface);border:1px solid var(--border);border-radius:8px;margin-bottom:8px;font-size:14px;color:var(--text);transition:border-color 0.2s}
                .entry:hover{border-color:var(--accent)}
                .entry.empty{color:var(--text-muted);font-style:italic}
                .actions{display:flex;gap:16px;margin-top:24px}
                .actions a{font-family:var(--mono);font-size:13px;color:var(--accent);padding:8px 16px;border:1px solid var(--border);border-radius:6px;text-decoration:none;transition:all 0.2s}
                .actions a:hover{border-color:var(--accent);background:var(--accent-dim)}
                .empty-msg{text-align:center;padding:48px 0;color:var(--text-muted);font-size:14px}
              </style>
            </head>
            <body>
              <div class="page">
                <h1>Search History</h1>
            """);

        synchronized (searchHistory) {
            if (searchHistory.isEmpty()) {
                out.println("<div class=\"empty-msg\">No searches yet.</div>");
            } else {
                for (int i = searchHistory.size() - 1; i >= 0; i--) {
                    String query = searchHistory.get(i);
                    if (query.isBlank()) {
                        out.println("<div class=\"entry empty\">(empty query)</div>");
                    } else {
                        out.println("<div class=\"entry\">" + query + "</div>");
                    }
                }
            }
        }

        out.println("""
                <div class="actions">
                  <a href="index.html?action=clearHistory">Clear History</a>
                  <a href="index.html">Back to Search</a>
                </div>
              </div>
            </body>
            </html>
            """);
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
        showSearchHistory(response);
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
