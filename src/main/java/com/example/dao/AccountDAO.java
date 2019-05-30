package com.example.dao;

import com.example.model.Account;

import java.sql.SQLException;
import java.util.List;

public interface AccountDAO {
    void createTable() throws SQLException;

    void removeTable() throws SQLException;

    List<Account> getAccounts() throws SQLException;

    Account getAccountById(String id) throws SQLException;

    Account addAccount(Account acc) throws SQLException;

    void updateAccount(Account acc, int id) throws SQLException;

    public void cleanAccounts() throws SQLException;

    boolean transferMoney(Account sender, Account reciever, double amount) throws SQLException;

    boolean isTableCreated() throws SQLException;
}
