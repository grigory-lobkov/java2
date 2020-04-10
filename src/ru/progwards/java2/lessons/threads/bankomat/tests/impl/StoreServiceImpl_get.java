package ru.progwards.java2.lessons.threads.bankomat.tests.impl;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.progwards.java2.lessons.threads.bankomat.Store;
import ru.progwards.java2.lessons.threads.bankomat.model.Account;
import ru.progwards.java2.lessons.threads.bankomat.service.StoreService;
import ru.progwards.java2.lessons.threads.bankomat.service.impl.StoreServiceImpl;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class StoreServiceImpl_get {

    // надо бы перевести на Моки  http://java-online.ru/junit-mockito.xhtml

    static StoreService service;
    static Map<String, Account> store;
    static String testKey;
    static Account testAccount;

    @BeforeClass
    public static void init() {
        Store s = new Store();
        store = s.getStore();
        service = new StoreServiceImpl(s);

        testAccount = new Account();
        testKey = UUID.randomUUID().toString();
        testAccount.setId(testKey);
        testAccount.setPin(2345);
        testAccount.setHolder("Account_for_tests");
        testAccount.setDate(new Date(System.currentTimeMillis()+365*24*3600*1000));
        testAccount.setAmount(Math.random()*1_000_000);
    }

    @Test(expected = RuntimeException.class)
    public void get_exception() {
        store.put(testKey, null);
        service.get(testKey);
    }

    @Test
    public void get_testAccount() {
        store.put(testKey, testAccount);
        var acc = service.get(testKey);
        store.remove(testKey);
        Assert.assertEquals(testAccount, acc);
    }

    @Test
    public void getCol_size() {
        int size = store.keySet().size();
        if(size==0) throw new RuntimeException("Test Store is empty. Cannot Test it.");
        var accs = service.get();
        Assert.assertEquals(size, accs.size());
    }

}
