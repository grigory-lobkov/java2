package ru.progwards.java2.lessons.http.bankomat.service.impl;

import ru.progwards.java2.lessons.http.bankomat.model.Account;
import ru.progwards.java2.lessons.http.bankomat.service.AccountService;
import ru.progwards.java2.lessons.http.bankomat.service.StoreService;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentAccountService implements AccountService {


    private StoreService service;

    public ConcurrentAccountService(){

    }

    public ConcurrentAccountService(StoreService service){
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

    Lock lock_transfer = new ReentrantLock();
    @Override
    public void transfer(Account from, Account to, double amount) {
        boolean locked = true;
        lock_transfer.lock();
        try {
            synchronized (from) {
                synchronized (to) {
                    lock_transfer.unlock();
                    locked = false;
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
        } finally {
            if(locked) lock_transfer.unlock();
        }
    }

}
