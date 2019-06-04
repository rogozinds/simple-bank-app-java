package com.example;

import com.example.dao.AccountDAOImpl;
import com.example.model.Account;
import com.sun.net.httpserver.HttpExchange;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class MainHandler extends AbstractHandler {
    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        System.out.println(target);
        switch (target) {
            case "/":
                handleAPIRequest(baseRequest, response);
                break;
            case "/accounts/clean":
                handleCleanAccounts(baseRequest, response);
                break;
            case "/account":
                handleAccount(baseRequest, response);
                break;
            case "/accounts":
                handleAccounts(baseRequest, response);
                break;
            case "/account/add":
                handleAccountCreate(baseRequest, response);
                break;
            case "/account/send":
                handleSend(baseRequest, response);
        }
    }

    private void writeHttpResponse(HttpServletResponse response, String content) {
        try {
            response.getWriter().print(content);
            response.getWriter().flush();
        } catch (IOException e) {
            System.out.println("Could not write the response " + e.getMessage());
            e.printStackTrace();
        }

    }

    private void handleAPIRequest(Request request, HttpServletResponse response) throws IOException {
        String responseContent = "List of available API:\n" +
                " /api - list of API\n" +
                " /api/accounts - list of all available accounts with their balances and ids\n" +
                " /api/account?id=1 - get an information about account with id equals to 1\n" +
                " /api/account/add?balance=500 - create a new account with the balance of 500 or with balance=0 if not specified\n" +
                " /api/account/send?sender=1&reciever=2&amount=30 -  send 30 from account with id 1 to account with id 2\n";
        writeHttpResponse(response, responseContent);
    }

    private void handleAccounts(Request request, HttpServletResponse response) throws IOException {
        String resposneContent = "List of accounts:\n";
        try {
            Connection con = MainSQLServerFactory.connect();
            AccountDAOImpl accountDAO = new AccountDAOImpl(con);
            List<Account> accs = accountDAO.getAccounts();
            for (Account acc : accs) {
                resposneContent += String.format("Account %s has balance %f \n", acc.getId(), acc.getBalance());
            }
        } catch (SQLException e) {
            System.out.println("There was an exception when getting value by id. Exception " + e.getMessage());
        }
        writeHttpResponse(response, resposneContent);
    }

    private void handleAccountCreate(Request request, HttpServletResponse response) throws IOException {
        Map<String, String[]> params = request.getParameterMap();
        String responseContent = "Something wend wrong";
        double balance = 0.0d;
        if (params.containsKey("balance")) {
            balance = Double.parseDouble(params.get("balance")[0]);
        }

        try {
            Connection con = MainSQLServerFactory.connect();
            AccountDAOImpl accountDAO = new AccountDAOImpl(con);
            Account acc = new Account("0", balance);
            acc = accountDAO.addAccount(acc);
            responseContent = String.format("Account %s with balance %f was created", acc.getId(), balance);
        } catch (SQLException e) {
            responseContent = "Could not create the account. Exception " + e.getMessage();
            System.out.println(responseContent);
        }
        writeHttpResponse(response, responseContent);
    }

    private void handleCleanAccounts(Request request, HttpServletResponse response) throws IOException {
        String responseContent = "Accounts are cleaned\n";
        try {
            Connection con = MainSQLServerFactory.connect();
            AccountDAOImpl accountDAO = new AccountDAOImpl(con);
            accountDAO.cleanAccounts();
        } catch (SQLException e) {
            System.out.println("Could not remove accounts. Exception " + e.getMessage());
        }
        writeHttpResponse(response, responseContent);
    }

    private void handleAccount(Request request, HttpServletResponse response) throws IOException {
        Map<String, String[]> params = request.getParameterMap();
        String responseContent = "Please specify the id parameter";
        if (params.containsKey("id")) {
            String id = (String) params.get("id")[0];
            responseContent = "Could not find the account with id: " + id;
            try {
                Connection con = MainSQLServerFactory.connect();
                AccountDAOImpl accountDAO = new AccountDAOImpl(con);
                Account acc = accountDAO.getAccountById(id);
                double balance = acc.getBalance();
                responseContent = String.format("Account %s has balance %f", id, balance);
            } catch (SQLException e) {
                System.out.println("There was an exception when getting value by id. Exception " + e.getMessage());
            }

        }
        writeHttpResponse(response, responseContent);
    }

    private void handleSend(Request request, HttpServletResponse response) throws IOException {
        Map<String, String[]> params = request.getParameterMap();
        String responseContent;
        Boolean wasTransferSuccessful = false;
        if (areParamsOk(params)) {
            double amount = 0.0d;
            String senderId = params.get("sender")[0];
            String recieverId = params.get("reciever")[0];
            try {
                amount = Double.parseDouble(params.get("amount")[0]);
                Connection con = MainSQLServerFactory.connect();
                AccountDAOImpl accountDAO = new AccountDAOImpl(con);
                Account sender = accountDAO.getAccountById(senderId);
                Account reciever = accountDAO.getAccountById(recieverId);
                wasTransferSuccessful = accountDAO.transferMoney(sender, reciever, amount);

            } catch (SQLException e) {
                System.out.println("There was an exception when sending values from acc to acc " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Could not parse amount param " + params.get("amount"));
            }
            if (wasTransferSuccessful) {
                responseContent = String.format("%f is sent from %s account to %s account", amount, senderId, params.get("reciever"));
            } else {
                responseContent = String.format("Not enough money on account %s to transfer %f", senderId, amount);
            }

        } else {
            responseContent = "Not enough parameters. You should pass 'sender' 'reciever' 'amount' ";
        }

        writeHttpResponse(response, responseContent);
    }

    private static boolean areParamsOk(Map params) {
        return params.containsKey("sender") && params.containsKey("reciever") && params.containsKey("amount");
    }
}
