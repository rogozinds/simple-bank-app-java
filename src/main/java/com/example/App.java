package com.example;

import com.example.dao.AccountDAOImpl;
import com.example.model.Account;
import com.sun.net.httpserver.HttpServer;
import org.hsqldb.Server;
import org.hsqldb.server.ServerAcl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws IOException, InterruptedException, SQLException, ExecutionException, ServerAcl.AclFormatException {
        Server dbServer = MainSQLServerFactory.create();
        Thread dbServerThread = new Thread(() -> {
            dbServer.start();
        });
        dbServerThread.start();
        HttpServer server = MainHttpServerFactory.create();
        Thread httpServerThread = new Thread(() -> {
            server.start();
        });
        httpServerThread.start();
        Connection con = MainSQLServerFactory.connect();
        AccountDAOImpl dao = new AccountDAOImpl(con);
        if (!dao.isTableCreated()) {
            dao.createTable();
//            dao.addAccount(new Account("0", 134.0));
//            dao.addAccount(new Account("0", 144.0));
//            dao.addAccount(new Account("0", 154.0));
        }
        List<Account> accs = dao.getAccounts();

        for (Account acc : accs) {
            System.out.println("Id column has " + acc.getId() + " Balance column has " + acc.getBalance());
        }
        boolean isExit = false;
        while (!isExit) {
            System.out.println("Type quit to quit");
            Scanner sc = new Scanner(System.in);
            String exit = sc.nextLine();
            isExit = "quit".equals(exit);
        }

        dbServer.stop();
        server.stop(0);

        System.out.println("Server is shuted done");


    }
}
