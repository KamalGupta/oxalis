package eu.peppol.inbound.server;

import eu.peppol.inbound.util.Log;
import eu.peppol.inbound.util.LoggingConfigurator;
import eu.peppol.util.GlobalConfiguration;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * This ContextListener serves as the main runtime initialization point for Oxalis.
 *
 * @author nigel
 * @author steinar
 * @author thore
 */
public class ContextListener implements ServletContextListener {

    SimpleLogger simpleLocalLogger = null;

    public ContextListener() {
        System.out.println("Initializing the Oxalis inbound server ....");
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        System.out.println("Starting Oxalis Access Point");
        simpleLocalLogger = new SimpleLoggerImpl(event.getServletContext());
        initializeLogging(event);
        Log.info("Starting Oxalis Access Point, retrieving the global configuration properties...");
        try {
            GlobalConfiguration.getInstance(); // initialize configuration
        } catch (RuntimeException e) {
            Log.error("Unable to initialize: " + e, e);
            // Shoves a decent error message into the Tomcat log
            event.getServletContext().log("ERROR: Unable to initialize: " + e, e);
            throw e;
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        System.out.println("Stopping Oxalis Access Point");
        Log.info("Stopping Oxalis Access Point");
    }

    @SuppressWarnings({"unused"})
    protected void initializeLogging(ServletContextEvent event) {
        System.out.println("Oxalis messages are emitted using SLF4J with logback");
        try {
            // Invokes the Oxalis logging configurator
            LoggingConfigurator loggingConfigurator = new LoggingConfigurator();
            loggingConfigurator.execute();
            simpleLocalLogger.log("Configured logback with " + loggingConfigurator.getConfigurationFile());
        } catch (Exception e) {
            simpleLocalLogger.log("Failed to configure logging");
        }
    }

    static interface SimpleLogger {
        void log(String msg);
    }

    static class SimpleLoggerImpl implements SimpleLogger {

        ServletContext servletContext;

        SimpleLoggerImpl(ServletContext servletContext) {
            this.servletContext = servletContext;
        }

        @Override
        public void log(String msg) {
            servletContext.log(msg);
        }

    }

}