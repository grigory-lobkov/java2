package ru.progwards.java2.lessons.http.bankomat.service;

import ru.progwards.java2.lessons.http.bankomat.model.Account;

public interface AccountService {

    public double balance(Account account);
    public void deposit(Account account, double amount);
    public void withdraw(Account account, double amount);
    public void transfer(Account from, Account to, double amount);


}