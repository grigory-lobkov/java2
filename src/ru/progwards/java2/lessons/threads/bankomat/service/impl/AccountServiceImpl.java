package ru.progwards.java2.lessons.threads.bankomat.service.impl;

import ru.progwards.java2.lessons.threads.bankomat.DI;
import ru.progwards.java2.lessons.threads.bankomat.model.Account;
import ru.progwards.java2.lessons.threads.bankomat.service.AccountService;
import ru.progwards.java2.lessons.threads.bankomat.service.StoreService;

@DI.Dependency(name="AccountService", isSingleton=true)
public class AccountServiceImpl implements AccountService {


    private StoreService service;

    public AccountServiceImpl(){
    }

    public AccountServiceImpl(StoreService service){
        this.service = service;
    }

    @Override
    public double balance(Account account) {
        synchronized (account) {
            return account.getAmount();
        }
    }

    @Override
    public void deposit(Account account, double amount) {
        synchronized (account) {
            double sum = account.getAmount() + amount;
            account.setAmount(sum);
            service.update(account);
        }
    }

    @Override
    public void withdraw(Account account, double amount) {
        synchronized (account) {
            double sum = account.getAmount() - amount;
            if (sum < 0) {
                throw new RuntimeException("Not enough money");
            }
            account.setAmount(sum);
            service.update(account);
        }
    }

    @Override
    public void transfer(Account from, Account to, double amount) {
        synchronized (from) {
            synchronized (to) {
                double fromSum = from.getAmount() - amount;
                double toSum = to.getAmount() + amount;
                if (fromSum < 0) {
                    throw new RuntimeException("Not enough money");
                }
                from.setAmount(fromSum);
                service.update(from);
                to.setAmount(toSum);
                service.update(to);
            }
        }
    }
}
