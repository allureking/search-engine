package edu.usfca.cs272;

import java.net.URL;
import java.util.*;

/**
 * single thread crawler implementation.
 */
public class CrawlerProcessor implements CrawlerProcessorInterface {
    /**
     * The InvertedIndex to use
     */
    private final InvertedIndex invertedIndex;
    /**
     * the url set which has been crawled
     */
    private final Set<URL> urlSet;
    /**
     * total number of page to crawl
     */
    private final int totalCrawl;

    /**
     * create a single thread crawler with a reference to an InvertedIndex and total page to crawl.
     * @param invertedIndex The InvertedIndex to use
     * @param totalCrawl maximum number of pages to crawl
     */
    public CrawlerProcessor(InvertedIndex invertedIndex, int totalCrawl) {
        this.invertedIndex = invertedIndex;
        this.totalCrawl = totalCrawl;

        urlSet = new HashSet<>();
    }

    /**
     * crawl one url and process inverted index
     * @param url
     */
    public void crawl(URL url) {
        url = cleanUrl(url);
        if (url == null || urlSet.contains(url) || urlSet.size() >= totalCrawl) {
            return;
        }

        HtmlFetcher.HtmlFetchResult htmlFetchResult = fetchUrlContent(url);
        if (htmlFetchResult.isHasHeader()) {
            urlSet.add(url);
        }

        String htmlContent = htmlFetchResult.getContent();
        if (htmlContent != null) {
            String clearHtml = HtmlCleaner.stripHtml(htmlContent);
            String[] lines = clearHtml.split("\n");

            InvertedIndexProcessor.processLines(url.toString(), lines, invertedIndex);
            if (urlSet.size() < totalCrawl) {
                HashSet<URL> links = LinkFinder.uniqueUrls(url, htmlContent);
                for (URL link : links) {
                    if (urlSet.contains(link)) {
                        continue;
                    }

                    crawl(link);
                }
            }
        }
    }

}
