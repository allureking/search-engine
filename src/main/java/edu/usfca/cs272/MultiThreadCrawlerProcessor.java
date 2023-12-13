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
     * total number of page which has been crawled
     */
    private int totalProcessed;

    /**
     * pending url queue to be processed
     */
    private final Queue<URL> pendingQueue;


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
        pendingQueue = new LinkedList<>();
    }

    /**
     * crawl one url and process inverted index
     * @param url seed url to crawl
     */
    public void crawl(URL url) {
        pendingQueue.add(url);

        while (true) {
            if (totalProcessed >= totalCrawl || pendingQueue.isEmpty()) {
                break;
            }

            int restNum = totalCrawl - totalProcessed;
            while (!pendingQueue.isEmpty() && restNum > 0) {
                URL pendingUrl = pendingQueue.poll();
                if (urlSet.contains(pendingUrl)) {
                    continue;
                }

                urlSet.add(pendingUrl);
                crawlOneUrl(pendingUrl);
                restNum--;
            }

            workQueue.finish();
        }
    }

    /**
     * crawl one url through one task
     * @param url to be processed
     */
    private void crawlOneUrl(URL url) {
        workQueue.execute(() -> {
            HtmlFetcher.HtmlFetchResult htmlFetchResult = fetchUrlContent(url);
            String htmlContent = htmlFetchResult.getContent();

            if (htmlFetchResult.isHasHeader()) {
                synchronized (urlSet) {
                    totalProcessed++;
                }

                if (htmlContent != null) {
                    String nonBlockHtml = HtmlCleaner.stripBlockElements(htmlContent);
                    String clearHtml = HtmlCleaner.stripTags(nonBlockHtml);
                    clearHtml = HtmlCleaner.stripEntities(clearHtml);

                    String[] lines = clearHtml.split("\n");

                    InvertedIndex local = new InvertedIndex();
                    InvertedIndexProcessor.processLines(url.toString(), lines, local);
                    invertedIndex.mergeDistinct(local);

                    synchronized (urlSet) {
                        if (totalProcessed >= totalCrawl) {
                            return;
                        }
                    }

                    List<URL> links = LinkFinder.listUrls(url, nonBlockHtml);
                    if (!links.isEmpty()) {
                        synchronized (pendingQueue) {
                            pendingQueue.addAll(links);
                        }
                    }
                }
            }
        });
    }
}
