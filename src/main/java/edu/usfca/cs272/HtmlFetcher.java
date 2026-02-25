package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * A specialized version of {@link HttpsFetcher} that follows redirects and
 * returns HTML content if possible.
 *
 * @see HttpsFetcher
 *
 * @author Honghuai Ke
 */
public class HtmlFetcher {
	/**
	 * Returns {@code true} if and only if there is a "Content-Type" header and the
	 * first value of that header starts with the value "text/html"
	 * (case-insensitive).
	 *
	 * @param headers the HTTP/1.1 headers to parse
	 * @return {@code true} if the headers indicate the content type is HTML
	 */
	public static boolean isHtml(Map<String, List<String>> headers) {
		if (headers != null) {
			for (String key: headers.keySet()) {
				if (key != null && key.equalsIgnoreCase("content-type")) {
					List<String> values = headers.get(key);
					return !values.isEmpty() && values.get(0).contains("text/html");
				}
			}
		}

		return false;
	}

	/**
	 * Parses the HTTP status code from the provided HTTP headers, assuming the
	 * status line is stored under the {@code null} key.
	 *
	 * @param headers the HTTP/1.1 headers to parse
	 * @return the HTTP status code or -1 if unable to parse for any reasons
	 */
	public static int getStatusCode(Map<String, List<String>> headers) {
		if (headers == null) {
			return -1;
		}

		List<String> values = headers.get(null);
		if (values == null || values.isEmpty()) {
			return -1;
		}

		String[] tmp = values.get(0).split(" ");
		if (tmp.length >= 3) {
			try {
				return Integer.parseInt(tmp[1]);
			} catch (NumberFormatException e) {}
		}

		return -1;
	}

	/**
	 * If the HTTP status code is between 300 and 399 (inclusive) indicating a
	 * redirect, returns the first redirect location if it is provided. Otherwise
	 * returns {@code null}.
	 *
	 * @param headers the HTTP/1.1 headers to parse
	 * @return the first redirected location if the headers indicate a redirect
	 */
	public static String getRedirect(Map<String, List<String>> headers) {
		if (headers != null) {
			for (String key: headers.keySet()) {
				if (key != null && key.equalsIgnoreCase("Location")) {
					List<String> values = headers.get(key);
					return values.isEmpty() ? null : values.get(0);
				}
			}
		}

		return null;
	}

	/**
	 * Fetches the resource at the URL using HTTP/1.1 and sockets. If the status
	 * code is 200 and the content type is HTML, returns the HTML as a single
	 * string. If the status code is a valid redirect, will follow that redirect if
	 * the number of redirects is greater than 0. Otherwise, returns {@code null}.
	 *
	 * @param url the url to fetch
	 * @param redirects the number of times to follow redirects
	 * @return the html or {@code null} if unable to fetch the resource or the
	 *   resource is not html
	 */
	public static String fetch(URL url, int redirects) {
		return fetchHtml(url, redirects).content();
	}

	/**
	 * Fetches the resource at the URL using HTTP/1.1 and sockets. If the status
	 * code is 200 and the content type is HTML, returns the HTML as a single
	 * string. If the status code is a valid redirect, will follow that redirect if
	 * the number of redirects is greater than 0. Otherwise, returns {@code null}.
	 *
	 * @param url the url to fetch
	 * @param redirects the number of times to follow redirects
	 * @return the html or {@code null} if unable to fetch the resource or the
	 *   resource is not html
	 */
	public static HtmlFetchResult fetchHtml(URL url, int redirects) {
		String content = null;
		boolean getHeader = false;

		try (
				Socket socket = HttpsFetcher.openConnection(url);
				PrintWriter request = new PrintWriter(socket.getOutputStream());
				InputStreamReader input = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
				BufferedReader response = new BufferedReader(input);
		) {
			socket.setSoTimeout(30000);
			HttpsFetcher.printHeadRequest(request, url);
			Map<String, List<String>> headers = HttpsFetcher.getHeaderFields(response);
			getHeader = true;

			int statusCode = getStatusCode(headers);
			if (statusCode == 200 && isHtml(headers)) {
				List<String> contentList = HttpsFetcher.fetchUrl(url).get("Content");
				content = contentList == null ? null : String.join("\n", contentList);
			} else if (statusCode >= 300 && statusCode <= 399 && redirects > 0) {
				String redirectUrl = getRedirect(headers);
				if (redirectUrl != null) {
					return fetchHtml(new URL(redirectUrl), redirects - 1);
				}
			}
		}
		catch (IOException e) {
			System.err.println("Unable to fetch URL: " + url);
		}

		return new HtmlFetchResult(getHeader, content);
	}

	/**
	 * Converts the {@link String} url into a {@link URL} object and then calls
	 * {@link #fetch(URL, int)}.
	 *
	 * @param url the url to fetch
	 * @param redirects the number of times to follow redirects
	 * @return the html or {@code null} if unable to fetch the resource or the
	 *   resource is not html
	 */
	public static String fetch(String url, int redirects) {
		try {
			return fetch(new URL(url), redirects);
		}
		catch (MalformedURLException e) {
			return null;
		}
	}

	/**
	 * Converts the {@link String} url into a {@link URL} object and then calls
	 * {@link #fetch(URL, int)} with 0 redirects.
	 *
	 * @param url the url to fetch
	 * @return the html or {@code null} if unable to fetch the resource or the
	 *   resource is not html
	 */
	public static String fetch(String url) {
		return fetch(url, 0);
	}

	/**
	 * Calls {@link #fetch(URL, int)} with 0 redirects.
	 *
	 * @param url the url to fetch
	 * @return the html or {@code null} if unable to fetch the resource or the
	 *   resource is not html
	 */
	public static String fetch(URL url) {
		return fetch(url, 0);
	}

	/**
	 * Represents the result of an HTML fetch operation.
	 *
	 * @param hasHeader whether the HTTP header was successfully retrieved
	 * @param content the HTML content, or null if unavailable
	 */
	public record HtmlFetchResult(boolean hasHeader, String content) {}
}
