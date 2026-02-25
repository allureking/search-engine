package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A utility class for finding all text files in a directory using lambda
 * expressions and streams.
 *
 * @author Honghuai Ke
 */
public class FileFinder {
	/**
	 * A lambda expression that returns true if the path is a file that ends in a
	 * .txt or .text extension (case-insensitive). Useful for
	 * {@link Files#walk(Path, FileVisitOption...)}.
	 *
	 * @see Files#walk(Path, FileVisitOption...)
	 */
	public static final Predicate<Path> IS_TEXT = path -> {
    String lower = path.toString().toLowerCase();
    return Files.isRegularFile(path) &&
           (lower.endsWith(".txt") || lower.endsWith(".text"));
};

	/**
	 * Returns a stream of all paths within the starting path that match the
	 * provided filter. Follows any symbolic links encountered.
	 *
	 * @param start the initial path to start with
	 * @param keep function that determines whether to keep a path
	 * @return a stream of paths
	 * @throws IOException if an IO error occurs
	 */
	public static Stream<Path> find(Path start, Predicate<Path> keep) throws IOException {
	    return Files.walk(start, FileVisitOption.FOLLOW_LINKS)
	                .filter(keep);
	};

	/**
	 * Returns a stream of text files, following any symbolic links encountered.
	 *
	 * @param start the initial path to start with
	 * @return a stream of text files
	 * @throws IOException if an IO error occurs
	 */
	public static Stream<Path> findText(Path start) throws IOException {
	    return find(start, IS_TEXT);
	}

	/**
	 * Returns a list of text files using streams.
	 *
	 * @param start the initial path to search
	 * @return list of text files
	 * @throws IOException if an IO error occurs
	 */
	public static List<Path> listText(Path start) throws IOException {
	    return findText(start).toList();
	}

	/**
	 * Returns a list of text files using streams if the provided path is a valid
	 * directory, otherwise returns a list containing only the default path.
	 *
	 * @param start the starting path
	 * @param defaultPath the default to include if the starting path is not a valid
	 *   directory
	 * @return a list of paths
	 * @throws IOException if an IO error occurs
	 */
	public static List<Path> listText(Path start, Path defaultPath) throws IOException {
	    if (Files.isDirectory(start)) {
	        return listText(start);
	    } else {
	        return List.of(defaultPath);
	    }
	}
}