package com.example;

import com.example.dao.AccountDAOImpl;
import com.example.model.Account;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainHttpServerFactory {

    public MainHttpServerFactory() {

    }

    public static HttpServer create() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api", MainHttpServerFactory::handleAPIRequest);
        server.createContext("/api/accounts", MainHttpServerFactory::handleAccounts);
        server.createContext("/api/accounts/clean", MainHttpServerFactory::handleCleanAccounts);
        server.createContext("/api/account", MainHttpServerFactory::handleAccount);
        server.createContext("/api/account/add", MainHttpServerFactory::handleAccountCreate);
        server.createContext("/api/account/send", MainHttpServerFactory::handleSend);
        return server;
    }

    private static void handleAccounts(HttpExchange exchange) throws IOException {
        String response = "List of accounts:\n";
        try {
            Connection con = MainSQLServerFactory.connect();
            AccountDAOImpl accountDAO = new AccountDAOImpl(con);
            List<Account> accs = accountDAO.getAccounts();
            for (Account acc : accs) {
                response += String.format("Account %s has balance %f \n", acc.getId(), acc.getBalance());
            }
        } catch (SQLException e) {
            System.out.println("There was an exception when getting value by id. Exception " + e.getMessage());
        }

        exchange.sendResponseHeaders(200, response.getBytes().length);//response code and length
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private static void handleCleanAccounts(HttpExchange exchange) throws IOException {
        String response = "Accounts are cleaned\n";
        try {
            Connection con = MainSQLServerFactory.connect();
            AccountDAOImpl accountDAO = new AccountDAOImpl(con);
            accountDAO.cleanAccounts();
        } catch (SQLException e) {
            System.out.println("Could not remove accounts. Exception " + e.getMessage());
        }

        exchange.sendResponseHeaders(200, response.getBytes().length);//response code and length
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private static void handleAPIRequest(HttpExchange exchange) throws IOException {
        String response = "List of available API:\n" +
                " /api - list of API\n" +
                " /api/accounts - list of all available accounts with their balances and ids\n" +
                " /api/account?id=1 - get an information about account with id equals to 1\n" +
                " /api/account/add?balance=500 - create a new account with the balance of 500 or with balance=0 if not specified\n" +
                " /api/account/send?sender=1&reciever=2&amount=30 -  send 30 from account with id 1 to account with id 2\n";
        exchange.sendResponseHeaders(200, response.getBytes().length);//response code and length
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private static void handleAccountCreate(HttpExchange exchange) throws IOException {
        Map params = MainHttpServerFactory.queryToMap(exchange.getRequestURI().getQuery());
        String response = "No account with such id";
        double balance = 0.0d;
        if (params.containsKey("balance")) {
            balance = Double.parseDouble((String) params.get("balance"));
        }

        try {
            Connection con = MainSQLServerFactory.connect();
            AccountDAOImpl accountDAO = new AccountDAOImpl(con);
            Account acc = new Account("0", balance);
            acc = accountDAO.addAccount(acc);
            response = String.format("Account %s with balance %f was created", acc.getId(), balance);
        } catch (SQLException e) {
            response = "Could not create the account. Exception " + e.getMessage();
            System.out.println(response);
        }
        exchange.sendResponseHeaders(200, response.getBytes().length);//response code and length
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private static void handleAccount(HttpExchange exchange) throws IOException {
        Map params = MainHttpServerFactory.queryToMap(exchange.getRequestURI().getQuery());
        String response = "";
        double balance = 0.0d;
        if (params.containsKey("balance")) {
            try {
                Connection con = MainSQLServerFactory.connect();
                AccountDAOImpl accountDAO = new AccountDAOImpl(con);
                Account acc = accountDAO.getAccountById((String) params.get("id"));
                balance = acc.getBalance();
                response = String.format("Account %s has balance %f", params.get("id"), balance);
            } catch (SQLException e) {
                System.out.println("There was an exception when getting value by id. Exception " + e.getMessage());
            }

        }

        exchange.sendResponseHeaders(200, response.getBytes().length);//response code and length
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private static boolean areParamsOk(Map params) {
        return params.containsKey("sender") && params.containsKey("reciever") && params.containsKey("amount");
    }

    private static void handleSend(HttpExchange exchange) throws IOException {
        Map params = MainHttpServerFactory.queryToMap(exchange.getRequestURI().getQuery());
        String response;
        Boolean wasTransferSuccessful = false;
        if (areParamsOk(params)) {
            double amount = 0.0d;
            try {
                String senderId = (String) params.get("sender");
                String recieverId = (String) params.get("reciever");
                amount = Double.parseDouble((String) params.get("amount"));


                Connection con = MainSQLServerFactory.connect();
                AccountDAOImpl accountDAO = new AccountDAOImpl(con);
                Account sender = accountDAO.getAccountById(senderId);
                Account reciver = accountDAO.getAccountById(recieverId);
                wasTransferSuccessful = accountDAO.transferMoney(sender, reciver, amount);

            } catch (SQLException e) {
                System.out.println("There was an exception when sending values from acc to acc " + e.getMessage());
            } catch (NumberFormatException e) {
                response = "Could not parse amount param " + params.get("amount");
            }
            if (wasTransferSuccessful) {
                response = String.format("%f is sent from %s account to %s account", amount, params.get("sender"), params.get("reciever"));
            } else {
                response = String.format("Not enough money on account %s to transfer %f", params.get("sender"), amount);
            }

        } else {
            response = "Not enough parameters. You should pass 'sender' 'reciever' 'amount' ";
        }

        exchange.sendResponseHeaders(200, response.getBytes().length);//response code and length
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    public static Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<String, String>();
        for (String param : query.split("&")) {
            String pair[] = param.split("=");
            if (pair.length > 1) {
                result.put(pair[0], pair[1]);
            } else {
                result.put(pair[0], "");
            }
        }
        return result;
    }
}
