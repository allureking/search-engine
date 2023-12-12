package edu.usfca.cs272;

import java.net.URL;
import java.util.*;

/**
 * multi thread crawler implementation.
 */
public class MultiThreadCrawlerProcessor implements CrawlerProcessorInterface {
    /**
     * The work queue used to execute tasks in multiple threads.
     */
    private final WorkQueue workQueue;
    /**
     * the inverted index to be used.
     */
    private ThreadSafeInvertedIndex invertedIndex;
    /**
     * the url set which has been crawled
     */
    private final Set<URL> urlSet;

    /**
     * total number of page to crawl
     */
    private final int totalCrawl;

    /**
     * create a multi thread crawler with a reference to an InvertedIndex and total page to crawl.
     * @param workQueue work queue to execute tasks in multiple threads.
     * @param invertedIndex inverted index to be used.
     * @param totalCrawl total number of page to crawl.
     */
    public MultiThreadCrawlerProcessor(WorkQueue workQueue, ThreadSafeInvertedIndex invertedIndex, int totalCrawl) {
        this.workQueue = workQueue;
        this.invertedIndex = invertedIndex;
        this.totalCrawl = totalCrawl;

        urlSet = new HashSet<>();
    }

    /**
     * crawl one url and process inverted index
     * @param url
     */
    public void crawl(URL url) {
        final URL targetUrl = url;
        workQueue.execute(() -> {
            URL cleanedUrl = cleanUrl(targetUrl);
            if (cleanedUrl == null) {
                return;
            }

            synchronized (urlSet) {
                if (urlSet.contains(cleanedUrl) || urlSet.size() >= totalCrawl) {
                    return;
                }
            }

            HtmlFetcher.HtmlFetchResult htmlFetchResult = fetchUrlContent(url);
            String htmlContent = htmlFetchResult.getContent();
            if (htmlFetchResult.isHasHeader()) {
                synchronized (urlSet) {
                    if (urlSet.size() >= totalCrawl) {
                        return;
                    }

                    urlSet.add(cleanedUrl);
                }
            }

//            System.out.println(Thread.currentThread().getName() + " crawl " + cleanedUrl + " content: " + (htmlContent != null) + " url num : " + urlSet.size());
            if (htmlContent != null) {
                String clearHtml = HtmlCleaner.stripHtml(htmlContent);
                String[] lines = clearHtml.split("\n");

                InvertedIndexProcessor.processLines(cleanedUrl.toString(), lines, invertedIndex);
                List<URL> links = LinkFinder.listUrls(url, htmlContent);
                for (URL link : links) {
                    if (urlSet.contains(link)) {
                        continue;
                    }

                    crawl(link);
                }
            }
        });
    }

    public Set<URL> getUrlSet() {
        return urlSet;
    }
}
