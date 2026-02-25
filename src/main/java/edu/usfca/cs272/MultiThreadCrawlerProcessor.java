package edu.usfca.cs272;

import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Implements a multi-threaded web crawler. This class extends the crawling
 * capabilities to handle multiple URLs concurrently using a WorkQueue.
 *
 * @author Honghuai Ke
 */
public class MultiThreadCrawlerProcessor implements CrawlerProcessorInterface {
    /**
     * The work queue used for executing crawling tasks in multiple threads.
     */
    private final WorkQueue workQueue;

    /**
     * The ThreadSafeInvertedIndex used for storing and managing crawled data.
     */
    private final ThreadSafeInvertedIndex invertedIndex;

    /**
     * A set to keep track of URLs that have already been crawled.
     */
    private final Set<URL> urlSet;

    /**
     * The total number of pages to crawl.
     */
    private final int totalCrawl;

    /**
     * The total number of pages that have been processed.
     */
    private int totalProcessed;

    /**
     * A queue of URLs pending to be processed.
     */
    private final Queue<URL> pendingQueue;

    /**
     * Lock object for synchronizing access to shared crawl state.
     */
    private final Object crawlLock;

    /**
     * Constructs a multi-threaded crawler with a specified work queue, inverted index, and a limit on the number of pages to crawl.
     *
     * @param workQueue     The work queue used for managing concurrent tasks.
     * @param invertedIndex The ThreadSafeInvertedIndex to store crawled data.
     * @param totalCrawl    The maximum number of pages to crawl.
     */
    public MultiThreadCrawlerProcessor(WorkQueue workQueue, ThreadSafeInvertedIndex invertedIndex, int totalCrawl) {
        this.workQueue = workQueue;
        this.invertedIndex = invertedIndex;
        this.totalCrawl = totalCrawl;

        urlSet = new HashSet<>();
        pendingQueue = new LinkedList<>();
        crawlLock = new Object();
    }

    /**
     * Starts the crawling process from a seed URL. Manages the concurrent processing
     * of URLs and ensures the specified crawl limit is not exceeded.
     *
     * @param url The seed URL to start crawling from.
     */
    @Override
    public void crawl(URL url) {
        synchronized (crawlLock) {
            pendingQueue.add(url);
        }

        while (true) {
            synchronized (crawlLock) {
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
            }

            workQueue.finish();
        }
    }

    /**
     * Submits a single URL crawl task to the work queue. The task fetches HTML content,
     * processes it, and extracts new links to be crawled.
     *
     * @param url The URL to be processed in this task.
     */
    private void crawlOneUrl(URL url) {
        workQueue.execute(() -> {
            HtmlFetcher.HtmlFetchResult htmlFetchResult = fetchUrlContent(url);
            String htmlContent = htmlFetchResult.content();

            if (htmlFetchResult.hasHeader()) {
                synchronized (crawlLock) {
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

                    synchronized (crawlLock) {
                        if (totalProcessed >= totalCrawl) {
                            return;
                        }
                    }

                    List<URL> links = LinkFinder.listUrls(url, nonBlockHtml);
                    if (!links.isEmpty()) {
                        synchronized (crawlLock) {
                            pendingQueue.addAll(links);
                        }
                    }
                }
            }
        });
    }
}
