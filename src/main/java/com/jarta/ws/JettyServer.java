package com.jarta.ws;

import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.ServletContext;

/**
 * Created by wei on 2015/4/23.
 */
public class JettyServer {

    public static void main(String[] args) throws Exception {
        Server server = new Server();
        ServerConnector backServer = new ServerConnector(server);
        backServer.setPort(8081);
        backServer.setName("BackServer");
        backServer.setIdleTimeout(20000);

        server.setConnectors(new Connector[]{backServer});

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        ServletHolder jerseyHolder = new ServletHolder(ServletContainer.class);
        jerseyHolder.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", PackagesResourceConfig.class.getCanonicalName());
        jerseyHolder.setInitParameter("com.sun.jersey.config.property.packages", "com.jarta.ws");
        jerseyHolder.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");

        context.addServlet(jerseyHolder, "/*");

        server.start();
        server.join();

    }
}
