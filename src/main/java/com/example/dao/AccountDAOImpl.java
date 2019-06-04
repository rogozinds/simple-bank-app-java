package com.example.dao;

import com.example.model.Account;
import org.hsqldb.jdbc.JDBCPreparedStatement;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AccountDAOImpl implements AccountDAO {

    Connection con;
    private final String TABLE_NAME = "ACCOUNTS";

    public AccountDAOImpl(Connection con) {
        this.con = con;
    }

    @Override
    public boolean isTableCreated() throws SQLException {
        DatabaseMetaData meta = con.getMetaData();
        ResultSet res = meta.getTables(null, null, TABLE_NAME, new String[]{"TABLE"});
        if (res.next()) {
            return true;
        }
        return false;
    }

    @Override
    public void createTable() throws SQLException {
        Statement stm = con.createStatement();
        String query =
                "CREATE TABLE " + TABLE_NAME +
                        "(id INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1, INCREMENT BY 1), balance FLOAT, PRIMARY KEY (id));";
        ResultSet res = stm.executeQuery(query);
    }

    @Override
    public void removeTable() throws SQLException {
        Statement stm = con.createStatement();
        stm.executeQuery("DROP TABLE " + TABLE_NAME);
    }

    @Override
    public void cleanAccounts() throws SQLException {
        Statement stm = con.createStatement();
        stm.executeQuery("DELETE FROM " + TABLE_NAME);
    }

    @Override
    public List<Account> getAccounts() throws SQLException {
        List<Account> accounts = new ArrayList<>();
        Statement stm = con.createStatement();
        String query = "select * from " + TABLE_NAME;
        ResultSet res = stm.executeQuery(query);
        while (res.next()) {
            String id = Integer.toString(res.getInt("id"));
            Account acc = new Account(id, res.getDouble("balance"));
            accounts.add(acc);
        }
        return accounts;
    }

    @Override
    public Account getAccountById(String id) throws SQLException {
        Statement stm = con.createStatement();
        String query = String.format("select * from %s where id=%s", TABLE_NAME, id);
        ResultSet res = stm.executeQuery(query);
        if (res.next()) {
            Double balance = res.getDouble("balance");
            return new Account(id, balance);
        } else {
            throw new SQLException("Could not retrieve results");
        }
    }

    @Override
    public Account addAccount(Account acc) throws SQLException {
        String query = String.format("INSERT INTO %s (balance) VALUES (%f);", TABLE_NAME, acc.getBalance());
        PreparedStatement stm = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        stm.executeUpdate();
        ResultSet genKeys = stm.getGeneratedKeys();
        int id = 0;
        if (genKeys.next()) {
            id = genKeys.getInt(1);
        }
        acc = new Account(Integer.toString(id), acc.getBalance());
        return acc;
    }

    @Override
    public void updateAccount(Account acc, int id) throws SQLException {
        String query = String.format("UPDATE %s set balance=%f WHERE id=%i;", TABLE_NAME, acc.getBalance(), id);
        Statement stm = con.createStatement();
        stm.executeUpdate(query);
    }

    @Override
    //Returns true if it was successful
    public boolean transferMoney(Account sender, Account reciever, double amount) throws SQLException {
        try {
            con.setAutoCommit(false);
            con.setTransactionIsolation(con.TRANSACTION_READ_COMMITTED);
            PreparedStatement stm;
            stm = con.prepareStatement("START TRANSACTION");
            stm.executeUpdate();
            String query;
            query = String.format("select * from %s where id=%s for update", TABLE_NAME, sender.getId());
            stm = con.prepareStatement(query);
            ResultSet res = stm.executeQuery();
            Double senderBalance = 0.0;
            if (res.next()) {
                senderBalance = res.getDouble("balance");
            } else {
                throw new SQLException("Could not retrieve results");
            }

            query = String.format("select * from %s where id=%s", TABLE_NAME, reciever.getId());
            stm = con.prepareStatement(query);
            res = stm.executeQuery();
            Double recieverBalance = 0.0;
            if (res.next()) {
                recieverBalance = res.getDouble("balance");
            } else {
                throw new SQLException("Could not retrieve results");
            }

            //We want to raise an exception or something, to explicitly show what is the problem. If there is no enough money on acc.
            // For now just close the transaction and silently return
            //
            if (senderBalance >= amount) {
                query = "UPDATE ACCOUNTS set balance = ? where id = ?;\n";

                System.out.println(String.format("Transfer: Sender balance: %f accId: %s", senderBalance, sender.getId()));
                stm = con.prepareStatement(query);
                stm.setDouble(1, senderBalance - amount);
                stm.setInt(2, Integer.valueOf(sender.getId()));
                stm.executeUpdate();
                stm.setDouble(1, recieverBalance + amount);
                stm.setInt(2, Integer.valueOf(reciever.getId()));
                stm.executeUpdate();
                stm = con.prepareStatement("COMMIT");
                stm.executeUpdate();
                con.commit();
            } else {
                con.setAutoCommit(true);
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Rollback the transacction");
            con.rollback();
            return false;
        }
        con.setAutoCommit(true);
        return true;
    }
}
