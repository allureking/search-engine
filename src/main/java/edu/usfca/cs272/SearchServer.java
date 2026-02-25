package edu.usfca.cs272;

import java.nio.file.Path;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Configures and starts a web server to handle search functionality.
 * Sets up handlers for serving static resources and handling search requests.
 *
 * @author Honghuai Ke
 */
public class SearchServer {

    /**
     * Port number where the server runs
     */
    private final int port;

    /**
     * Processor for handling exact search queries
     */
    private final SearchProcessorInterface exactSearchProcessor;

    /**
     * Processor for handling partial search queries
     */
    private final SearchProcessorInterface partialSearchProcessor;

    /**
     * Inverted index used for search processing
     */
    private final InvertedIndex invertedIndex;


    /**
     * Initializes a new SearchServer.
     *
     * @param port The port number where the server will run.
     * @param exactSearchProcessor The search processor for exact search queries.
     * @param partialSearchProcessor The search processor for partial search queries.
     * @param invertedIndex The inverted index used for searching.
     */
    public SearchServer(int port, SearchProcessorInterface exactSearchProcessor,
                        SearchProcessorInterface partialSearchProcessor, InvertedIndex invertedIndex) {
        this.port = port;
        this.exactSearchProcessor = exactSearchProcessor;
        this.partialSearchProcessor = partialSearchProcessor;
        this.invertedIndex = invertedIndex;
    }

    /**
     * Starts the web server and configures its route handlers.
     *
     * @throws Exception If there is an issue starting the server.
     */
    public void startServer() throws Exception {
        // Base path for resources
        Path base = Path.of("src", "main", "resources");

        // Set up Jetty server on the specified port
        Server server = new Server(port);

        // Context handler for server sessions
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        // Handler for serving static resources
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(false);
        resourceHandler.setResourceBase(base.resolve("./images").toString());

        // Setting up servlet handler for handling search requests
        ServletHandler servletHandler = new ServletHandler();
        servletHandler.addServletWithMapping(new ServletHolder(new SearchServlet(exactSearchProcessor, partialSearchProcessor, invertedIndex)), "/index.html");

        // Combining handlers into a handler list
        HandlerList handlers = new HandlerList();
        handlers.addHandler(resourceHandler);
        handlers.addHandler(servletHandler);

        // Assign handlers to the server and start it
        server.setHandler(handlers);
        server.start();
        server.join();
    }
}
