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

public class StoreServiceImpl_update {

    static StoreService service;
    static Map<String, Account> store;
    static String testKey;
    static Account testAccount1;
    static Account testAccount2;

    @BeforeClass
    public static void init() {
        store = Store.getStore();
        service = new StoreServiceImpl();

        testKey = UUID.randomUUID().toString();

        testAccount1 = new Account();
        testAccount1.setId(testKey);
        testAccount1.setPin(2345);
        testAccount1.setHolder("Account_for_tests");
        testAccount1.setDate(new Date(System.currentTimeMillis()+365*24*3600*1000));
        testAccount1.setAmount(Math.random()*1_000_000);

        testAccount2 = new Account();
        testAccount2.setId((" "+testKey).substring(1));
        testAccount2.setPin(3456);
        testAccount2.setHolder("AccountForTests");
        testAccount2.setDate(new Date(testAccount1.getDate().getTime()+5));
        testAccount2.setAmount(testAccount1.getAmount()+9);
    }

    @Test(expected = RuntimeException.class)
    public void update_exception() {
        service.update(testAccount1);
    }

    @Test
    public void update_testAccount() {
        store.put(testKey, testAccount1);

        service.update(testAccount2);

        Account gotAccount = store.get(testKey);
        store.remove(testKey);
        Assert.assertEquals(testAccount2, gotAccount);
    }

}
