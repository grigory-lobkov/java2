package ru.progwards.java2.lessons.annotation.di.model;


import ru.progwards.java2.lessons.annotation.di.DI;

import java.util.Date;

//POJO
@DI.Dependency(name="Account")
public class Account {

    private String id;
    private String holder;
    private Date date;
    private double amount;
    private int pin;

    public Account() {
    }

    public Account(String id, String holder, Integer pin) {
        this.id = id;
        this.holder = holder;
        this.pin = pin;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHolder() {
        return holder;
    }

    public void setHolder(String holder) {
        this.holder = holder;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getPin() {
        return pin;
    }

    public void setPin(int pin) {
        this.pin = pin;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id='" + id + '\'' +
                ", holder='" + holder + '\'' +
                ", date=" + date +
                ", amount=" + amount +
                ", pin=" + pin +
                '}';
    }
}