package edu.usfca.cs272;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Finds HTTP(S) URLs from the anchor tags within HTML code.
 *
 * @author Honghuai Ke
 */
public class LinkFinder {
	/**
	 * Finds all the valid HTTP(S) links in the HREF attribute of the anchor tags in
	 * the provided HTML. The links will be converted to an absolute URL using the
	 * base URL and normalized (removing fragments and encoding special characters
	 * as necessary).
	 *
	 * Any links that do not use the HTTP/S protocol or are unable to be properly
	 * parsed (throwing an {@link MalformedURLException}) will not be included.
	 *
	 * @param base the base URL used to convert relative links to absolute URLs
	 * @param html the raw HTML associated with the base URL
	 * @param links the data structure to store found HTTP(S) links
	 */
	public static void findLinks(URL base, String html, Collection<URL> links) {
		String regex = "(?i)(<a[^>]+href\\s*=\\s*\")([^\"]+)(\")";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(html);

		while (matcher.find()) {
			try {
				URL myUrl = new URL(base, matcher.group(2));
				if (isHttp(myUrl)) {
					links.add(normalize(myUrl));
				}
			} catch (MalformedURLException | URISyntaxException e) {
				System.err.println("Invalid html");
			}
		}
	}

	/**
	 * Returns a list of all the valid HTTP(S) URLs found in the HREF attribute of
	 * the anchor tags in the provided HTML.
	 *
	 * @param base the base URL used to convert relative links to absolute URLs
	 * @param html the raw HTML associated with the base URL
	 * @return list of all valid HTTP(S) links in the order they were found
	 */
	public static ArrayList<URL> listUrls(URL base, String html) {
		ArrayList<URL> urls = new ArrayList<>();
		findLinks(base, html, urls);
		return urls;
	}

	/**
	 * Returns a set of all the unique valid HTTP(S) URLs found in the HREF
	 * attribute of the anchor tags in the provided HTML.
	 *
	 * @param base the base URL used to convert relative URLs to absolute3
	 * @param html the raw HTML associated with the base URL
	 * @return set of all valid and unique HTTP(S) links found
	 */
	public static HashSet<URL> uniqueUrls(URL base, String html) {
		HashSet<URL> urls = new HashSet<>();
		findLinks(base, html, urls);
		return urls;
	}

	/**
	 * Determines whether the URL provided uses the HTTP or HTTPS protocol.
	 *
	 * @param url the URL to check
	 * @return true if the URL uses the HTTP or HTTPS protocol
	 */
	public static boolean isHttp(URL url) {
		return url != null && url.getProtocol().matches("(?i)https?");
	}

	/**
	 * Attempts to create a normalized absolute URL from the provided base URL and
	 * link text. If fails for any reason, will return null.
	 *
	 * @param base the base URL the link text was found on
	 * @param href the link text (usually from an anchor tag href attribute)
	 * @return the normalized absolute URL or {@code null}
	 */
	public static URL convertUrl(URL base, String href) {
		URI uri = makeUri(href);
		URL url = null;

		if (uri != null) {
			try {
				if (!uri.isOpaque() && !uri.isAbsolute()) {
					URI parent = cleanUri(base.toURI());
					uri = parent.resolve(uri);
				}

				url = cleanUri(uri).toURL();
			}
			catch (URISyntaxException | MalformedURLException | IllegalArgumentException e) {
				url = null;
			}
		}

		return url;
	}

	/**
	 * Attempts to remove the fragment component (if present) from a URI and then
	 * return the normalized URI. Also ensures that a hierarchical URI has the
	 * default path "/" if one is missing.
	 *
	 * @param uri the URI to clean and normalize
	 * @return cleaned (if possible) and normalized URI
	 */
	public static URI cleanUri(URI uri) {
		URI normal = uri.normalize();
		URI clean = null;

		String fragment = normal.getFragment();
		String path = normal.normalize().getPath();

		try {
			if (!normal.isOpaque() && (path == null || path.isBlank())) {
				clean = new URI(normal.getScheme(), normal.getAuthority(), "/", normal.getQuery(), null);
			}
			else if (fragment != null) {
				clean = new URI(uri.getScheme(), uri.getSchemeSpecificPart(), null);
			}
		}
		catch (URISyntaxException e) {
			clean = null;
		}

		// return cleaned version if created, otherwise normalized version
		return clean != null ? clean : normal;
	}

	/**
	 * Attempts to create a URI from the provided text, treating the text as a
	 * relative path component if the process initially fails.
	 *
	 * @param link the text value to convert to URI
	 * @return the URI or {@code null} if unable to create
	 */
	public static URI makeUri(String link) {
		try {
			return new URI(link);
		}
		catch (URISyntaxException e) {
			int hash = link.lastIndexOf("#"); // find fragment
			link = hash > 0 ? link.substring(0, hash) : link; // remove fragment

			try {
				return new URI(null, link, null); //convert as relative
			}
			catch (URISyntaxException f) {
				return null;
			}
		}
	}

	/**
	 * Normalizes a URL by cleaning its fragment, encoding its components properly,
	 * and ensuring that it has a default path if the path is empty.
	 *
	 * This method removes any fragment component from the URL, encodes special characters,
	 * and if the path is empty, it sets it to "/". This is particularly useful for
	 * ensuring consistency in URLs where the absence or presence of a trailing slash
	 * should not result in different URLs.
	 *
	 * @param url The URL to normalize.
	 * @return The normalized URL.
	 * @throws MalformedURLException If an error occurs while creating the normalized URL.
	 * @throws URISyntaxException If an error occurs while creating the URI for normalization.
	 */
	public static URL normalize(URL url) throws MalformedURLException, URISyntaxException {
		// Create a URI from the URL to normalize it
		URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(),
				url.getPath(), url.getQuery(), null).normalize();

		// Check if the path is empty or "/", and set it to "/" if so
		String path = uri.getPath();
		if (path == null || path.isEmpty()) {
			uri = new URI(uri.getScheme(), uri.getAuthority(), "/", uri.getQuery(), null);
		}

		// Return the URL form of the normalized URI
		return uri.toURL();
	}
}