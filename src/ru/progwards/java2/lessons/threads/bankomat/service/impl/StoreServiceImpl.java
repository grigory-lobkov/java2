package ru.progwards.java2.lessons.threads.bankomat.service.impl;

import ru.progwards.java2.lessons.threads.bankomat.DI;
import ru.progwards.java2.lessons.threads.bankomat.Store;
import ru.progwards.java2.lessons.threads.bankomat.model.Account;
import ru.progwards.java2.lessons.threads.bankomat.service.StoreService;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@DI.Dependency(name="StoreService", isSingleton=true)
public class StoreServiceImpl implements StoreService {

    Map<String, Account> store;
    Lock lock = new ReentrantLock();

    public StoreServiceImpl(Store store) {
        this.store = store.getStore();
    }

    @Override
    public Account get(String id) {
        lock.lock();
        try {
            Account account = store.get(id);
            if (account == null) {
                throw new RuntimeException("Account not found by id: " + id);
            }
            return account;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Collection<Account> get() {
        lock.lock();
        try {
            if (store.size() == 0) {
                throw new RuntimeException("Store is empty");
            }
            return store.values();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void delete(String id) {
        lock.lock();
        try {
            if (store.get(id) == null) {
                throw new RuntimeException("Account not found by id:" + id);
            }
            store.remove(id);
        } finally {
            lock.unlock();
        }
    }

    public void insertInternal(Account account) {
        store.put(account.getId(), account);
    }

    @Override
    public void insert(Account account) {
        lock.lock();
        try {
            insertInternal(account);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void update(Account account) {
        lock.lock();
        try {
            if (store.get(account.getId()) == null) {
                throw new RuntimeException("Account not found by id:" + account.getId());
            }
            this.insertInternal(account);
        } finally {
            lock.unlock();
        }
    }

}