package edu.usfca.cs272;

import java.nio.file.Path;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class SearchServer {

    private final int port;
    private final SearchProcessorInterface exactSearchProcessor;
    private final SearchProcessorInterface partialSearchProcessor;

    public SearchServer(int port, SearchProcessorInterface searchProcessor, SearchProcessorInterface partySearchProcessor) {
        this.port = port;
        this.exactSearchProcessor = searchProcessor;
        this.partialSearchProcessor = partySearchProcessor;
    }

    public void startServer() throws Exception {
        Path BASE = Path.of("src", "main", "resources");
        // Set up Jetty server
        Server server = new Server(port);

        // Create servlet context handler
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(true);
        resourceHandler.setResourceBase(BASE.resolve("./images").toString());

        // Add the SearchServlet to the context
        ServletHandler servletHandler = new ServletHandler();
        servletHandler.addServletWithMapping(new ServletHolder(new SearchServlet(exactSearchProcessor, partialSearchProcessor)), "/index.html");


        HandlerList handlers = new HandlerList();
        handlers.addHandler(resourceHandler);

        handlers.addHandler(servletHandler);
        server.setHandler(handlers);
        // Start the server
        server.start();
        server.join();
    }

}
