package edu.usfca.cs272;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Implements a single-threaded web crawler. This class is responsible for crawling web pages,
 * extracting links, and processing the content to build an InvertedIndex.
 *
 * @author Honghuai Ke
 */
public class CrawlerProcessor implements CrawlerProcessorInterface {
    /**
     * The InvertedIndex used for storing crawled data.
     */
    private final InvertedIndex invertedIndex;

    /**
     * The set of URLs that have been crawled.
     */
    private final Set<URL> urlSet;

    /**
     * The maximum number of pages to crawl.
     */
    private final int totalCrawl;


    /**
     * Constructs a single-threaded crawler with a reference to an InvertedIndex and a specified
     * maximum number of pages to crawl.
     *
     * @param invertedIndex The InvertedIndex to store crawled data.
     * @param totalCrawl    The maximum number of pages to crawl.
     */
    public CrawlerProcessor(InvertedIndex invertedIndex, int totalCrawl) {
        this.invertedIndex = invertedIndex;
        this.totalCrawl = totalCrawl;
        urlSet = new HashSet<>();
    }

    /**
     * Crawls a single URL and processes it to update the InvertedIndex. It fetches the content
     * of the URL, extracts and processes the textual content, and then finds and crawls
     * additional links from this content. The process stops when either the URL has been crawled
     * before, the total number of pages to crawl has been reached, or the URL is null.
     *
     * @param url The URL to be crawled and processed.
     */
    @Override
    public void crawl(URL url) {
        if (url == null || urlSet.contains(url) || urlSet.size() >= totalCrawl) {
            return;
        }

        HtmlFetcher.HtmlFetchResult htmlFetchResult = fetchUrlContent(url);
        if (htmlFetchResult.hasHeader()) {
            urlSet.add(url);
        }

        String htmlContent = htmlFetchResult.content();
        if (htmlContent != null) {
            String clearHtml = HtmlCleaner.stripHtml(htmlContent);
            String[] lines = clearHtml.split("\n");

            InvertedIndexProcessor.processLines(url.toString(), lines, invertedIndex);
            if (urlSet.size() < totalCrawl) {
                HashSet<URL> links = LinkFinder.uniqueUrls(url, htmlContent);
                for (URL link : links) {
                    if (!urlSet.contains(link)) {
                        crawl(link);
                    }
                }
            }
        }
    }

}
