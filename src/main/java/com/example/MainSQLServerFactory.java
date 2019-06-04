package com.example;

import org.hsqldb.Server;
import org.hsqldb.server.ServerAcl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.*;

public class MainSQLServerFactory {


    public static final int DB_PORT = 9009;
    public static final String DB_NAME = "mainDB";

    public static Server create() throws IOException, ServerAcl.AclFormatException, SQLException, ExecutionException, InterruptedException {
        return create(DB_NAME);
    }

    public static Server create(String dbname) throws IOException, ServerAcl.AclFormatException, SQLException, ExecutionException, InterruptedException {
        Server server = new Server();
        server.setDatabasePath(0, "mem:" + dbname);
        server.setDatabaseName(0, dbname);
        server.setPort(DB_PORT);
        server.setLogWriter(null); // can use custom writer
        server.setErrWriter(null); // can use custom writer
        return server;
    }

    public static Connection connect() throws SQLException {
        String url = "jdbc:hsqldb:hsql://localhost:9009/" + DB_NAME;
        Connection con = DriverManager.getConnection(url, "SA", "");
        return con;
    }

}
