package ru.progwards.java2.lessons.threads.bankomat.service;

import ru.progwards.java2.lessons.threads.bankomat.model.Account;

public interface AccountService {

    double balance(Account account);

    void deposit(Account account, double amount);
    void withdraw(Account account, double amount);
    void transfer(Account from, Account to, double amount);


}