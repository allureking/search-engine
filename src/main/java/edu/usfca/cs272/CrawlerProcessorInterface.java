package edu.usfca.cs272;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;

/**
 * an interface of crawler
 */
public interface CrawlerProcessorInterface {
    /**
     * maximum number of redirect to follow
     */
    int MAX_REDIRECTS = 3;

    /**
     * fetch html content of given url
     * @param url to fetch
     * @return html fetch result, include content and if header get success
     */
    default HtmlFetcher.HtmlFetchResult fetchUrlContent(URL url) {
        return HtmlFetcher.fetchHtml(url, MAX_REDIRECTS);
    }

    /**
     * clean given url
     * @param url original url to processed
     * @return clean url
     */
    default URL cleanUrl(URL url) {
        try {
            return LinkFinder.cleanUri(url.toURI()).toURL();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * crawl the given URL
     * @param url to be crawled.
     */
    void crawl(URL url);
}
