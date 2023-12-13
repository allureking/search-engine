package edu.usfca.cs272;

import java.net.URL;

/**
 * Defines the interface for a web crawler. This interface specifies methods for
 * fetching and cleaning URLs, and for performing the crawl operation.
 */
public interface CrawlerProcessorInterface {
    /**
     * The maximum number of redirects to follow when fetching HTML content.
     */
    int MAX_REDIRECTS = 3;

    /**
     * Fetches the HTML content of the specified URL. This method includes handling
     * of redirects up to the maximum specified limit.
     *
     * @param url The URL from which to fetch HTML content.
     * @return An HtmlFetchResult object containing the fetched content and a flag indicating
     *         whether the header was successfully retrieved.
     */
    default HtmlFetcher.HtmlFetchResult fetchUrlContent(URL url) {
        return HtmlFetcher.fetchHtml(url, MAX_REDIRECTS);
    }

    /**
     * Cleans the given URL by normalizing and resolving any relative components.
     * This method is designed to return a well-formed, clean URL object.
     *
     * @param url The original URL to be processed.
     * @return A cleaned URL, or null if an error occurs during processing.
     */
    default URL cleanUrl(URL url) {
        try {
            return LinkFinder.cleanUri(url.toURI()).toURL();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Initiates the crawl process for the given URL. This method is responsible for
     * the core crawling operations like fetching HTML content, extracting links,
     * and processing the content.
     *
     * @param url The URL to be crawled.
     */
    void crawl(URL url);
}