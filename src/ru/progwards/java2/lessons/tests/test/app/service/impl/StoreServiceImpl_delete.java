package ru.progwards.java2.lessons.tests.test.app.service.impl;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.progwards.java2.lessons.tests.app.Store;
import ru.progwards.java2.lessons.tests.app.model.Account;
import ru.progwards.java2.lessons.tests.app.service.StoreService;
import ru.progwards.java2.lessons.tests.app.service.impl.StoreServiceImpl;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class StoreServiceImpl_delete {

    static StoreService service;
    static Map<String, Account> store;
    static String testKey;
    static Account testAccount;

    @BeforeClass
    public static void init() {
        store = Store.getStore();
        service = new StoreServiceImpl();

        testAccount = new Account();
        testKey = UUID.randomUUID().toString();
        testAccount.setId(testKey);
        testAccount.setPin(2345);
        testAccount.setHolder("Account_for_tests");
        testAccount.setDate(new Date(System.currentTimeMillis()+365*24*3600*1000));
        testAccount.setAmount(Math.random()*1_000_000);
    }

    @Test(expected = RuntimeException.class)
    public void delete_exception() {
        service.delete(testKey);
    }

    @Test
    public void delete_testAccount() {
        store.put(testKey, testAccount);

        service.delete(testKey);

        boolean exists = store.containsKey(testKey);
        Assert.assertTrue("test account is still exists", !exists);
    }

}
