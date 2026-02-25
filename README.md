# WebDepth Search Engine

A full-stack search engine built in Java featuring an in-memory inverted index, multithreaded web crawler, and an embedded web server with a custom search UI.

**23 Java source files | 1,653 lines of Javadoc | 2,119 source lines of code (SLOC)**

## Architecture Overview

```
┌──────────────────────────────────────────────────────────────────┐
│                        Driver (CLI Entry)                        │
│   Parses flags: -text, -html, -query, -server, -threads, etc.   │
└──────┬──────────────┬──────────────┬──────────────┬──────────────┘
       │              │              │              │
       ▼              ▼              ▼              ▼
  ┌─────────┐  ┌────────────┐  ┌─────────┐  ┌────────────┐
  │  Index   │  │    Web     │  │  Search  │  │    Web     │
  │ Builder  │  │  Crawler   │  │ Processor│  │  Server    │
  └────┬─────┘  └─────┬──────┘  └────┬─────┘  └─────┬──────┘
       │              │              │              │
       ▼              ▼              ▼              ▼
  ┌────────────────────────────────────────────────────────────┐
  │              InvertedIndex / ThreadSafeInvertedIndex        │
  │     TreeMap<word, TreeMap<location, TreeSet<position>>>     │
  └────────────────────────────────────────────────────────────┘
```

## Features

- **Inverted Index** — Nested `TreeMap` structure mapping words to document locations and positions, supporting both single-threaded and thread-safe concurrent access
- **Multithreaded Web Crawler** — Recursively crawls from a seed URL with configurable depth, follows redirects (up to 3 hops), extracts and normalizes links, and indexes page content
- **Search with Ranking** — Exact and partial (prefix) search with TF-based relevance scoring (`occurrences / totalWordsInDocument`), sorted by score, count, then location
- **Embedded Web Server** — Eclipse Jetty servlet container serving a search UI with search history, dark mode, and index download
- **Concurrency** — Custom `WorkQueue` thread pool and `MultiReaderLock` (read/write lock) enabling concurrent reads with exclusive writes
- **XSS Protection** — HTML special characters escaped in search results via Apache Commons Text

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Build | Apache Maven |
| NLP | Apache OpenNLP (Snowball stemmer) |
| Logging | Apache Log4j2 2.21.1 |
| Web Server | Eclipse Jetty 11 + Jakarta Servlets |
| Frontend | HTML, CSS, vanilla JavaScript |
| Testing | JUnit 5 |
| Utilities | Apache Commons Text |

## Project Structure

```
src/main/java/edu/usfca/cs272/
├── Driver.java                           # CLI entry point
├── ArgumentParser.java                   # Command-line flag/value parsing
│
├── InvertedIndex.java                    # Core inverted index data structure
├── ThreadSafeInvertedIndex.java          # Thread-safe wrapper with read/write locks
├── InvertedIndexProcessor.java           # Single-threaded file indexing
├── MultiThreadInvertedIndexProcessor.java# Multithreaded file indexing
│
├── SearchProcessor.java                  # Single-threaded search
├── MultiThreadSearchProcessor.java       # Multithreaded search
├── SearchProcessorInterface.java         # Search abstraction
│
├── CrawlerProcessor.java                 # Single-threaded web crawler
├── MultiThreadCrawlerProcessor.java      # Multithreaded web crawler
├── CrawlerProcessorInterface.java        # Crawler abstraction
├── HtmlFetcher.java                      # HTTP fetching with redirect handling
├── HttpsFetcher.java                     # Low-level HTTPS socket connections
├── LinkFinder.java                       # URL extraction and normalization
├── HtmlCleaner.java                      # HTML tag stripping and entity decoding
│
├── SearchServer.java                     # Jetty server configuration
├── SearchServlet.java                    # HTTP request handler
│
├── FileStemmer.java                      # Word stemming (Snowball/English)
├── FileFinder.java                       # File discovery with predicates
├── JsonWriter.java                       # Pretty-printed JSON serialization
├── WorkQueue.java                        # Thread pool implementation
└── MultiReaderLock.java                  # Custom read/write lock

src/main/resources/
├── index.html                            # Search UI (dark mode, search history)
├── log4j2.xml                            # Logging configuration
└── images/                               # Logo assets
```

## Usage

```bash
# Build
mvn clean compile

# Index local text files
java -cp target/classes edu.usfca.cs272.Driver -text path/to/files -index index.json

# Crawl the web and start the search server
java -cp target/classes edu.usfca.cs272.Driver \
  -html https://example.com \
  -crawl 50 \
  -threads 5 \
  -server 8080

# Run search queries from file
java -cp target/classes edu.usfca.cs272.Driver \
  -text path/to/files \
  -query queries.txt \
  -results results.json \
  -partial
```

### Command-Line Flags

| Flag | Description | Default |
|------|------------|---------|
| `-text <path>` | Path to text file or directory to index | — |
| `-html <url>` | Seed URL for web crawler | — |
| `-crawl <n>` | Max pages to crawl | 1 |
| `-query <path>` | Path to query file | — |
| `-index <path>` | Output inverted index as JSON | `index.json` |
| `-counts <path>` | Output word counts as JSON | `counts.json` |
| `-results <path>` | Output search results as JSON | `results.json` |
| `-server <port>` | Start web server on port | 8080 |
| `-threads <n>` | Number of worker threads | 5 (when flag present) |
| `-partial` | Enable partial (prefix) search | exact search |

## How It Works

### Inverted Index

The core data structure is a nested `TreeMap`:

```
word → { location → [position1, position2, ...] }
```

`TreeMap` keeps words sorted, enabling efficient prefix-based partial search via `tailMap()`. A separate map tracks total word counts per document for relevance scoring.

### Web Crawler

1. Fetches HTML from seed URL (follows up to 3 redirects)
2. Strips HTML tags, stems text content, adds to inverted index
3. Extracts and normalizes links from anchor tags
4. Recursively crawls discovered URLs up to the configured limit
5. In multithreaded mode, each URL is processed as a `WorkQueue` task

### Search & Ranking

Results are ranked by term frequency:

```
score = matchCount / totalWordsInDocument
```

Ties are broken by match count (descending), then by location (alphabetical). Partial search matches any indexed word starting with the query prefix.

### Concurrency Model

- **WorkQueue**: Fixed-size thread pool where worker threads pull `Runnable` tasks from a shared queue. Supports graceful shutdown and a `finish()` barrier that blocks until all pending work completes.
- **MultiReaderLock**: Allows multiple concurrent readers or a single exclusive writer. Active writers can re-enter both read and write locks.

## Live Demo

Try the search engine live at **[kingke.dev/demo](https://kingke.dev/demo/)** — crawling Wikipedia pages with multithreaded indexing and real-time search.

<p align="center">
  <img src="src/main/resources/images/logo_transparent.png" alt="WebDepth Search Logo" width="150">
</p>
