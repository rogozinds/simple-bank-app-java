package com.example;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;

import java.io.IOException;

public class MainHttpServerFactory {

    public MainHttpServerFactory() {

    }

    public static Server create() throws IOException {
        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(8080);
        server.setConnectors(new Connector[] { connector });
        ContextHandler context = new ContextHandler();
        context.setContextPath( "/api/*" );
        context.setHandler(new MainHandler());
        server.setHandler( context );
        return server;
    }

}
