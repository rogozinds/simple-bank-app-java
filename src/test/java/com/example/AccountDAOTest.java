package com.example;

import com.example.dao.AccountDAOImpl;
import com.example.model.Account;
import org.hsqldb.server.Server;
import org.hsqldb.server.ServerAcl;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class AccountDAOTest {

    static Server server;
    static Connection con;
    @BeforeClass
    public static void serverSetup() throws InterruptedException, SQLException, ServerAcl.AclFormatException, ExecutionException, IOException {
        server = MainSQLServerFactory.create();
        server.start();


    }
    @Before
    public void setup() throws SQLException {
        con = MainSQLServerFactory.connect();
        AccountDAOImpl dao = new AccountDAOImpl(con);
        dao.createTable();
    }

    @After
    public void cleanTable() throws SQLException {
        AccountDAOImpl dao = new AccountDAOImpl(con);
        dao.removeTable();
    }
    @AfterClass
    public static void cleanServer() throws SQLException {
        server.stop();
    }

    @Test
    public void getAccounts_addThreeAccounts_returnListWithAccounts() throws SQLException {
        AccountDAOImpl dao = new AccountDAOImpl(con);
        dao.addAccount(new Account("0",134.0));
        dao.addAccount(new Account("0",134.0));
        dao.addAccount(new Account("0",134.0));
        List<Account> accounts = dao.getAccounts();

        Assert.assertEquals(3, accounts.size());
    }

    @Test
    public void addAccount_retunsCreatedAccount() throws SQLException {
        AccountDAOImpl dao = new AccountDAOImpl(con);
        double balance = 134.0;
        Account acc = new Account("0",balance);
        acc = dao.addAccount(acc);
        Assert.assertNotNull(acc);
        Assert.assertEquals(balance,acc.getBalance(),0.000001);
    }

    @Test
    public void getAccountById_retunCorrectAccount() throws SQLException {
        AccountDAOImpl dao = new AccountDAOImpl(con);
        double balance = 134.0;
        Account acc = new Account("0",balance);
        acc = dao.addAccount(acc);
        acc = dao.getAccountById(acc.getId());
        Assert.assertNotNull(acc);
        Assert.assertEquals(balance,acc.getBalance(),0.000001);
    }

    @Test
    public void trasferMoneyFromAccount() throws SQLException {
        AccountDAOImpl dao = new AccountDAOImpl(con);
        double balance = 134.0;
        Account acc1 = new Account("0",140);
        Account acc2 = new Account("0",200);
        acc1 = dao.addAccount(acc1);
        acc2 = dao.addAccount(acc2);
        dao.transferMoney(acc1,acc2,40d);
        acc1=dao.getAccountById(acc1.getId());
        Assert.assertEquals(100,acc1.getBalance(),0.000001);
        acc2=dao.getAccountById(acc2.getId());
        Assert.assertEquals(240,acc2.getBalance(),0.000001);
    }

    @Test
    public void trasferMoneyFromAccountWhenNotEnoughDoesNotChange() throws SQLException {
        AccountDAOImpl dao = new AccountDAOImpl(con);
        Account acc1 = new Account("0",140);
        Account acc2 = new Account("0",200);
        acc1 = dao.addAccount(acc1);
        acc2 = dao.addAccount(acc2);
        dao.transferMoney(acc1,acc2,400d);
        acc1=dao.getAccountById(acc1.getId());
        Assert.assertEquals(140,acc1.getBalance(),0.000001);
        acc2=dao.getAccountById(acc2.getId());
        Assert.assertEquals(200,acc2.getBalance(),0.000001);
    }

    @Test
    public void trasferMoneyFromAccountWhenNotEnoughReturnsFalse() throws SQLException {
        AccountDAOImpl dao = new AccountDAOImpl(con);
        Account acc1 = new Account("0",140);
        Account acc2 = new Account("0",200);
        acc1 = dao.addAccount(acc1);
        acc2 = dao.addAccount(acc2);
        Boolean isSuccessful = dao.transferMoney(acc1,acc2,400d);
        Assert.assertFalse(isSuccessful);
    }
}
