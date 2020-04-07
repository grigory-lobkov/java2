package ru.progwards.java2.lessons.annotation.di.service;

import ru.progwards.java2.lessons.annotation.di.model.Account;

import java.util.Collection;

public interface StoreService {
    public Account get(String id);
    public Collection<Account> get();
    public void delete(String id);
    public void insert(Account account);
    public void update(Account account);
}