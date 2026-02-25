package edu.usfca.cs272;

import java.nio.file.Path;
import java.util.HashMap;

/**
 * Parses and stores command-line arguments into simple flag/value pairs.
 *
 * @author Honghuai Ke
 */
public class ArgumentParser {

    /**
     * Stores command-line arguments in flag/value pairs.
     */
    private final HashMap<String, String> map;

    /**
     * Initializes this argument map.
     */
    public ArgumentParser() {
        this.map = new HashMap<>();
    }

	/**
	 * Initializes this argument map and then parsers the arguments into flag/value
	 * pairs where possible. Some flags may not have associated values. If a flag is
	 * repeated, its value is overwritten.
	 *
	 * @param args the command line arguments to parse
	 */
	public ArgumentParser(String[] args) {
		this();
		parse(args);
	}

	/**
	 * Determines whether the argument is a flag. The argument is considered a flag
	 * if it is a dash "-" character followed by any character that is not a digit
	 * or whitespace. For example, "-hello" and "-@world" are considered flags, but
	 * "-10" and "- hello" are not.
	 *
	 * @param arg the argument to test if its a flag
	 * @return {@code true} if the argument is a flag
	 */
	public static boolean isFlag(String arg) {
		if (arg == null || !arg.startsWith("-") || arg.length() <= 1) {
		    return false;
		}

		int codePoint = arg.codePointAt(1);
		return !Character.isDigit(codePoint) && !Character.isWhitespace(codePoint);
	}

	/**
	 * Determines whether the argument is a value. Anything that is not a flag is
	 * considered a value.
	 *
	 * @param arg the argument to test if its a value
	 * @return {@code true} if the argument is a value
	 */
	public static boolean isValue(String arg) {
		return !isFlag(arg);
	}

	/**
	 * Parses the arguments into flag/value pairs where possible. Some flags may not
	 * have associated values. If a flag is repeated, its value will be overwritten.
	 *
	 * @param args the command line arguments to parse
	 */
	public void parse(String[] args) {
		for (int i = 0; i < args.length; i++) {
		    if (isFlag(args[i])) {
		        if (i + 1 < args.length && isValue(args[i + 1])) {
		            map.put(args[i], args[++i]);
		        } else {
		            map.put(args[i], null);
		        }
		    }
		}
	}

	/**
	 * Returns the number of unique flags.
	 *
	 * @return number of unique flags
	 */
	public int numFlags() {
		return map.size();
	}

	/**
	 * Determines whether the specified flag exists.
	 *
	 * @param flag the flag check
	 * @return {@code true} if the flag exists
	 */
	public boolean hasFlag(String flag) {
		return map.containsKey(flag);
	}

	/**
	 * Determines whether the specified flag is mapped to a non-null value.
	 *
	 * @param flag the flag to find
	 * @return {@code true} if the flag is mapped to a non-null value
	 */
	public boolean hasValue(String flag) {
		return map.get(flag) != null;
	}

	/**
	 * Returns the value to which the specified flag is mapped as a {@link String}
	 * or the backup value if there is no mapping.
	 *
	 * @param flag the flag whose associated value is to be returned
	 * @param backup the backup value to return if there is no mapping
	 * @return the value to which the specified flag is mapped, or the backup value
	 *   if there is no mapping
	 */
	public String getString(String flag, String backup) {
		String value = map.get(flag);
		return value != null ? value : backup;
	}

	/**
	 * Returns the value to which the specified flag is mapped as a {@link String}
	 * or null if there is no mapping.
	 *
	 * @param flag the flag whose associated value is to be returned
	 * @return the value to which the specified flag is mapped or {@code null} if
	 *   there is no mapping
	 */
	public String getString(String flag) {
		return getString(flag, null);
	}

	/**
	 * Returns the value the specified flag is mapped as a {@link Path}, or the
	 * backup value if unable to retrieve this mapping (including being unable to
	 * convert the value to a {@link Path} or if no value exists).
	 *
	 * This method should not throw any exceptions!
	 *
	 * @param flag the flag whose associated value will be returned
	 * @param backup the backup value to return if there is no valid mapping
	 * @return the value the specified flag is mapped as a {@link Path}, or the
	 *   backup value if there is no valid mapping
	 */
	public Path getPath(String flag, Path backup) {
		if (hasFlag(flag) && hasValue(flag)) {
			return Path.of(getString(flag));
		}
		return backup;
	}

	/**
	 * Returns the value to which the specified flag is mapped as a {@link Path}, or
	 * {@code null} if unable to retrieve this mapping (including being unable to
	 * convert the value to a {@link Path} or no value exists).
	 *
	 * This method should not throw any exceptions!
	 *
	 * @param flag the flag whose associated value is to be returned
	 * @return the value to which the specified flag is mapped, or {@code null} if
	 *   unable to retrieve this mapping
	 */
	public Path getPath(String flag) {
		return getPath(flag, null);
	}

	/**
	 * Returns the value the specified flag is mapped as an int value, or the backup
	 * value if unable to retrieve this mapping (including being unable to convert
	 * the value to an int or if no value exists).
	 *
	 * @param flag the flag whose associated value will be returned
	 * @param backup the backup value to return if there is no valid mapping
	 * @return the value the specified flag is mapped as an int, or the backup value
	 *   if there is no valid mapping
	 */
	public int getInteger(String flag, int backup) {
		String value = getString(flag, null);
		if (value != null) {
		    try {
		        return Integer.parseInt(value);
		    } catch (NumberFormatException e) {
		        return backup;
		    }
		}
		return backup;
	}

	/**
	 * Returns the value the specified flag is mapped as an int value, or 0 if
	 * unable to retrieve this mapping (including being unable to convert the value
	 * to an int or if no value exists).
	 *
	 * @param flag the flag whose associated value will be returned
	 * @return the value the specified flag is mapped as an int, or 0 if there is no
	 *   valid mapping
	 */
	public int getInteger(String flag) {
		return getInteger(flag, 0);
	}

	@Override
	public String toString() {
		return this.map.toString();
	}
}
