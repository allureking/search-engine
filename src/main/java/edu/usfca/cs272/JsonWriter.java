package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

/**
 * Outputs several simple data structures in "pretty" JSON format where newlines
 * are used to separate elements and nested elements are indented using spaces.
 * <p>
 * Warning: This class is not thread-safe. If multiple threads access this class
 * concurrently, access must be synchronized externally.
 * </p>
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2023
 */
public class JsonWriter {
    /**
     * Indents the writer by the specified number of times. Does nothing if the
     * indentation level is 0 or less.
     *
     * @param writer the writer to use
     * @param indent the number of times to indent
     * @throws IOException if an IO error occurs
     */
    public static void writeIndent(Writer writer, int indent) throws IOException {
        while (indent-- > 0) {
            writer.write("  ");
        }
    }

    /**
     * Indents and then writes the String element.
     *
     * @param element the element to write
     * @param writer  the writer to use
     * @param indent  the number of times to indent
     * @throws IOException if an IO error occurs
     */
    public static void writeIndent(String element, Writer writer, int indent) throws IOException {
        writeIndent(writer, indent);
        writer.write(element);
    }

    /**
     * Indents and then writes the text element surrounded by {@code " "} quotation
     * marks.
     *
     * @param element the element to write
     * @param writer  the writer to use
     * @param indent  the number of times to indent
     * @throws IOException if an IO error occurs
     */
    public static void writeQuote(String element, Writer writer, int indent) throws IOException {
        writeIndent(writer, indent);
        writer.write('"');
        writer.write(element);
        writer.write('"');
    }

    /**
     * Writes an individual entry of the given map to the provided writer in JSON format.
     * The key is written as a JSON string, followed by a colon, and then the corresponding
     * values are written as a JSON array.
     *
     * @param entry  The map entry containing a key and a collection of numbers.
     * @param writer The writer to which the JSON formatted data is written.
     * @param indent The level of indentation to use for formatting the JSON output.
     * @throws IOException If an I/O error occurs while writing to the writer.
     */
    private static void writeEntry(Map.Entry<String, ? extends Collection<? extends Number>> entry, Writer writer, int indent) throws IOException {
        writer.write('\n');
        writeQuote(entry.getKey(), writer, indent + 1);
        writer.write(": ");
        writeArray(entry.getValue(), writer, indent + 1);
    }

    /**
     * Writes a key-value pair for the provided entry where the value is expected
     * to be a JSON object containing nested arrays. This method is intended to
     * assist in generating nested JSON structures for the {@link #writeObjectObjects} method.
     *
     * @param entry   The map entry to be written, where the key is a String and
     *                the value is a Map with String keys and Collection values.
     * @param writer  The writer to which the output will be written.
     * @param indent  The number of times to indent the current line.
     * @throws IOException If an IO error occurs during writing.
     */
    private static void writeNestedEntry(Map.Entry<String, ? extends Map<String, ? extends Collection<? extends Number>>> entry, Writer writer, int indent) throws IOException {
        writer.write('\n');
        writeQuote(entry.getKey(), writer, indent + 1);
        writer.write(": ");
        writeObjectArrays(entry.getValue(), writer, indent + 1);
    }

    /**
     * Writes the elements as a pretty JSON array.
     *
     * @param elements the elements to write
     * @param writer   the writer to use
     * @param indent   the initial indent level; the first bracket is not indented,
     *                 inner elements are indented by one, and the last bracket is indented at the
     *                 initial indentation level
     * @throws IOException if an IO error occurs
     * @see Writer#write(String)
     * @see #writeIndent(Writer, int)
     * @see #writeIndent(String, Writer, int)
     */
    public static void writeArray(Collection<? extends Number> elements, Writer writer, int indent) throws IOException {

        writer.write('[');

        var iterator = elements.iterator();

        if (iterator.hasNext()) {
            writer.write('\n');
            writeIndent(iterator.next().toString(), writer, indent + 1);

            while (iterator.hasNext()) {
                writer.write(",\n");
                writeIndent(iterator.next().toString(), writer, indent + 1);
            }
        }

        writer.write('\n');
        writeIndent("]", writer, indent);

    }

    /**
     * Writes the elements as a pretty JSON array to file.
     *
     * @param elements the elements to write
     * @param path     the file path to use
     * @throws IOException if an IO error occurs
     * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
     * @see StandardCharsets#UTF_8
     * @see #writeArray(Collection, Writer, int)
     */
    public static void writeArray(Collection<? extends Number> elements, Path path) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
            writeArray(elements, writer, 0);
        }
    }

    /**
     * Returns the elements as a pretty JSON array.
     *
     * @param elements the elements to use
     * @return a {@link String} containing the elements in pretty JSON format
     * @see StringWriter
     * @see #writeArray(Collection, Writer, int)
     */
    public static String writeArray(Collection<? extends Number> elements) {
        try {
            StringWriter writer = new StringWriter();
            writeArray(elements, writer, 0);
            return writer.toString();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Writes the elements as a pretty JSON object.
     *
     * @param elements the elements to write
     * @param writer   the writer to use
     * @param indent   the initial indent level; the first bracket is not indented,
     *                 inner elements are indented by one, and the last bracket is indented at the
     *                 initial indentation level
     * @throws IOException if an IO error occurs
     * @see Writer#write(String)
     * @see #writeIndent(Writer, int)
     * @see #writeIndent(String, Writer, int)
     */
    public static void writeObject(Map<String, ? extends Number> elements, Writer writer, int indent) throws IOException {
        writer.write('{');
        var iterator = elements.entrySet().iterator();

        if (iterator.hasNext()) {
            writer.write('\n');

            var entry = iterator.next();
            writeQuote(entry.getKey(), writer, indent + 1);
            writer.write(": ");
            writer.write(entry.getValue().toString());

            while (iterator.hasNext()) {
                writer.write(",\n");
                entry = iterator.next();
                writeQuote(entry.getKey(), writer, indent + 1);
                writer.write(": ");
                writer.write(entry.getValue().toString());
            }
        }

        writer.write('\n');
        writeIndent("}", writer, indent);
    }

    /**
     * Writes the elements as a pretty JSON object to file.
     *
     * @param elements the elements to write
     * @param path     the file path to use
     * @throws IOException if an IO error occurs
     * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
     * @see StandardCharsets#UTF_8
     * @see #writeObject(Map, Writer, int)
     */
    public static void writeObject(Map<String, ? extends Number> elements, Path path) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
            writeObject(elements, writer, 0);
        }
    }

    /**
     * Returns the elements as a pretty JSON object.
     *
     * @param elements the elements to use
     * @return a {@link String} containing the elements in pretty JSON format
     * @see StringWriter
     * @see #writeObject(Map, Writer, int)
     */
    public static String writeObject(Map<String, ? extends Number> elements) {
        try {
            StringWriter writer = new StringWriter();
            writeObject(elements, writer, 0);
            return writer.toString();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Writes the elements as a pretty JSON object with nested arrays. The generic
     * notation used allows this method to be used for any type of map with any type
     * of nested collection of number objects.
     *
     * @param elements the elements to write
     * @param writer   the writer to use
     * @param indent   the initial indent level; the first bracket is not indented,
     *                 inner elements are indented by one, and the last bracket is indented at the
     *                 initial indentation level
     * @throws IOException if an IO error occurs
     * @see Writer#write(String)
     * @see #writeIndent(Writer, int)
     * @see #writeIndent(String, Writer, int)
     * @see #writeArray(Collection)
     */
    public static void writeObjectArrays(Map<String, ? extends Collection<? extends Number>> elements, Writer writer, int indent) throws IOException {
        writer.write('{');
        var iterator = elements.entrySet().iterator();

        if (iterator.hasNext()) {
            writeEntry(iterator.next(), writer, indent);

            while (iterator.hasNext()) {
                writer.write(",");
                writeEntry(iterator.next(), writer, indent);
            }
        }

        writer.write('\n');
        writeIndent("}", writer, indent);
    }

    /**
     * Writes the elements as a pretty JSON object with nested arrays. The generic
     * notation used allows this method to be used for any type of map with any type
     * of nested collection of number objects.
     *
     * @param elements the elements to write.
     *                 Inner elements are indented by one, and the last bracket is indented at the
     *                 initial indentation level.
     * @return A string containing the JSON object representation.
     * @throws IOException if an IO error occurs while writing the JSON object.
     * @see Writer#write(String)
     * @see #writeIndent(Writer, int)
     * @see #writeIndent(String, Writer, int)
     * @see #writeArray(Collection)
     */
    public static String writeObjectObjects(Map<String, ? extends Map<String, ? extends Collection<? extends Number>>> elements) throws IOException {
        StringWriter writer = new StringWriter();
        writeObjectObjects(elements, writer, 0);
        return writer.toString();
    }

    /**
     * Writes the elements as a pretty JSON object with nested arrays. The generic
     * notation used allows this method to be used for any type of map with any type
     * of nested collection of number objects.
     *
     * @param elements the elements to write
     * @param path     the path to write
     * @throws IOException if an IO error occurs
     * @see Writer#write(String)
     * @see #writeIndent(Writer, int)
     * @see #writeIndent(String, Writer, int)
     * @see #writeArray(Collection)
     */
    public static void writeObjectObjects(Map<String, ? extends Map<String, ? extends Collection<? extends Number>>> elements, Path path) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
            writeObjectObjects(elements, writer, 0);
        }
    }

    /**
     * Writes the elements as a pretty JSON object with nested arrays. The generic
     * notation used allows this method to be used for any type of map with any type
     * of nested collection of number objects.
     *
     * @param elements the elements to write
     * @param writer   the writer to use
     * @param indent   the initial indent level; the first bracket is not indented,
     *                 inner elements are indented by one, and the last bracket is indented at the
     *                 initial indentation level
     * @throws IOException if an IO error occurs
     * @see Writer#write(String)
     * @see #writeIndent(Writer, int)
     * @see #writeIndent(String, Writer, int)
     * @see #writeArray(Collection)
     */
    public static void writeObjectObjects(Map<String, ? extends Map<String, ? extends Collection<? extends Number>>> elements, Writer writer, int indent) throws IOException {
        writer.write('{');
        var iterator = elements.entrySet().iterator();

        if (iterator.hasNext()) {
            writeNestedEntry(iterator.next(), writer, indent);

            while (iterator.hasNext()) {
                writer.write(",");
                writeNestedEntry(iterator.next(), writer, indent);
            }
        }

        writer.write('\n');
        writeIndent("}", writer, indent);
    }

    /**
     * Writes the elements as a pretty JSON object with nested arrays to file.
     *
     * @param elements the elements to write
     * @param path     the file path to use
     * @throws IOException if an IO error occurs
     * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
     * @see StandardCharsets#UTF_8
     * @see #writeObjectArrays(Map, Writer, int)
     */
    public static void writeObjectArrays(Map<String, ? extends Collection<? extends Number>> elements, Path path) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
            writeObjectArrays(elements, writer, 0);
        }
    }

    /**
     * Returns the elements as a pretty JSON object with nested arrays.
     *
     * @param elements the elements to use
     * @return a {@link String} containing the elements in pretty JSON format
     * @see StringWriter
     * @see #writeObjectArrays(Map, Writer, int)
     */
    public static String writeObjectArrays(Map<String, ? extends Collection<? extends Number>> elements) {
        try {
            StringWriter writer = new StringWriter();
            writeObjectArrays(elements, writer, 0);
            return writer.toString();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Writes the elements as a pretty JSON array with nested objects. The generic
     * notation used allows this method to be used for any type of collection with
     * any type of nested map of String keys to number objects.
     *
     * @param elements the elements to write
     * @param writer   the writer to use
     * @param indent   the initial indent level; the first bracket is not indented,
     *                 inner elements are indented by one, and the last bracket is indented at the
     *                 initial indentation level
     * @throws IOException if an IO error occurs
     * @see Writer#write(String)
     * @see #writeIndent(Writer, int)
     * @see #writeIndent(String, Writer, int)
     * @see #writeObject(Map)
     */
    public static void writeArrayObjects(Collection<? extends Map<String, ? extends Number>> elements, Writer writer, int indent) throws IOException {
        writer.write('[');
        var iterator = elements.iterator();

        if (iterator.hasNext()) {
            writer.write('\n');
            writeIndent(writer, indent + 1);
            writeObject(iterator.next(), writer, indent + 1);

            while (iterator.hasNext()) {
                writer.write(",\n");
                writeIndent(writer, indent + 1);
                writeObject(iterator.next(), writer, indent + 1);
            }
        }

        writer.write('\n');
        writeIndent("]", writer, indent);
    }

    /**
     * Writes the elements as a pretty JSON array with nested objects to file.
     *
     * @param elements the elements to write
     * @param path     the file path to use
     * @throws IOException if an IO error occurs
     * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
     * @see StandardCharsets#UTF_8
     * @see #writeArrayObjects(Collection)
     */
    public static void writeArrayObjects(Collection<? extends Map<String, ? extends Number>> elements, Path path) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
            writeArrayObjects(elements, writer, 0);
        }
    }

    /**
     * Returns the elements as a pretty JSON array with nested objects.
     *
     * @param elements the elements to use
     * @return a {@link String} containing the elements in pretty JSON format
     * @see StringWriter
     * @see #writeArrayObjects(Collection)
     */
    public static String writeArrayObjects(Collection<? extends Map<String, ? extends Number>> elements) {
        try {
            StringWriter writer = new StringWriter();
            writeArrayObjects(elements, writer, 0);
            return writer.toString();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Writes an individual entry of the given map to the provided writer in JSON format.
     * The key is written as a JSON string, followed by a colon, and then the corresponding
     * values are written as a JSON array.
     *
     * @param elements  The map entry containing a key and a collection of numbers.
     * @param path     the file path to use
     * @throws IOException If an I/O error occurs while writing to the writer.
     */
    public static void writeObjectArrayObject(Map<String, Collection<Map<String, Object>>> elements, Path path) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
            writeObjectArrayObject(elements, writer, 0);
        }
    }

    /**
     * Writes an individual entry of the given map to the provided writer in JSON format.
     * The key is written as a JSON string, followed by a colon, and then the corresponding
     * values are written as a JSON array.
     *
     * @param elements  The map entry containing a key and a collection of numbers.
     * @param writer The writer to which the JSON formatted data is written.
     * @param indent The level of indentation to use for formatting the JSON output.
     * @throws IOException If an I/O error occurs while writing to the writer.
     */
    public static void writeObjectArrayObject(Map<String, Collection<Map<String, Object>>> elements, Writer writer, int indent) throws IOException {
        writer.write('{');
        var iterator = elements.entrySet().iterator();

        if (iterator.hasNext()) {
            writeCollectionEntry(iterator.next(), writer, indent);

            while (iterator.hasNext()) {
                writer.write(",");
                writeCollectionEntry(iterator.next(), writer, indent);
            }
        }

        writer.write('\n');
        writeIndent("}", writer, indent);
    }

    /**
     * Writes an individual entry of the given map to the provided writer in JSON format.
     * The key is written as a JSON string, followed by a colon, and then the corresponding
     * values, which are {@link JsonObject} instances, are written as a JSON array.
     *
     * @param entry  The map entry containing a key and a collection of {@link JsonObject} instances.
     * @param writer The writer to which the JSON formatted data is written.
     * @param indent The level of indentation to use for formatting the JSON output.
     * @throws IOException If an I/O error occurs while writing to the writer.
     */
    private static void writeCollectionEntry(Map.Entry<String, ? extends Collection<? extends JsonObject>> entry, Writer writer, int indent) throws IOException {
        writer.write('\n');
        writeQuote(entry.getKey(), writer, indent + 1);
        writer.write(": ");
        writeMapArray(entry.getValue(), writer, indent + 1);
    }

    /**
     * Writes a collection of {@link JsonObject} elements as a JSON array.
     *
     * @param elements the collection of {@link JsonObject} elements to write
     * @param writer   the {@link Writer} to use for output
     * @param indent   the initial indentation level; the first bracket is at this level,
     *                 elements are indented by one level more, and the closing bracket is at the initial level
     * @throws IOException if an IO error occurs during writing
     */
    public static void writeMapArray(Collection<JsonObject> elements, Writer writer, int indent) throws IOException {
        writer.write("[\n");

        var iterator = elements.iterator();

        if (iterator.hasNext()) {
            iterator.next().toJson(writer, indent + 1);
        }

        while (iterator.hasNext()) {
            writer.write(",\n");
            iterator.next().toJson(writer, indent + 1);
        }

        writer.write("\n");
        writeIndent("]", writer, indent);
    }

    /**
     * Writes a map as a JSON object, where each value is a {@link JsonObject}.
     *
     * @param map    the map with keys as {@link String} and values as {@link JsonObject}
     * @param writer the {@link Writer} to use for output
     * @param indent the indentation level at which the JSON object should start
     * @throws IOException if an IO error occurs during writing
     */
    public static void writeMap(Map<String, ? extends JsonObject> map, Writer writer, int indent) throws IOException {
        writer.write("{\n");

        var iterator = map.entrySet().iterator();

        if (iterator.hasNext()) {
            Map.Entry<String, ? extends JsonObject> entry = iterator.next();
            writeQuote(entry.getKey(), writer, indent + 1);
            writer.write(": ");
            entry.getValue().toJson(writer, indent + 2);

            while (iterator.hasNext()) {
                writer.write(",\n");
                entry = iterator.next();
                writeQuote(entry.getKey(), writer, indent + 1);
                writer.write(": ");
                entry.getValue().toJson(writer, indent + 2);
            }
        }

        writer.write("\n");
        writeIndent("}", writer, indent);
    }

    /**
     * An interface representing a JSON object capable of converting itself into a map
     * and writing its JSON representation to a writer.
     */
    public interface JsonObject {
        /**
         * Converts this object into a map representation.
         *
         * @return a map representing this object
         */
        Map<String, Object> toMap();

        /**
         * Writes this object as a JSON string using the provided writer and indentation level.
         *
         * @param writer the writer to write the JSON string
         * @param indent the indentation level for the JSON string
         * @throws IOException if an IO error occurs during writing
         */
        void toJson(Writer writer, int indent) throws IOException;
    }

    /**
     * Writes a {@link JsonObject} element as a pretty JSON object.
     *
     * @param element the {@link JsonObject} to write
     * @param writer  the {@link Writer} to use for output
     * @param indent  the indentation level at which the JSON object should start
     * @throws IOException if an IO error occurs during writing
     */
    public static void writeJsonObject(JsonObject element, Writer writer, int indent) throws IOException {
        element.toJson(writer, indent);
    }
}
