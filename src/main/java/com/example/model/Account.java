package com.example.model;

public class Account {
    String id;
    double balance;

    public Account(String id, double balance) {
        this.id = id;
        this.balance = balance;
    }


    public String getId() {
        return id;
    }

    public double getBalance() {
        return balance;
    }

}
