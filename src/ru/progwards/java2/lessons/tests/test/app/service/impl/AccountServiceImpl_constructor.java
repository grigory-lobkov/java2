package ru.progwards.java2.lessons.tests.test.app.service.impl;

import org.junit.BeforeClass;
import org.junit.Test;
import ru.progwards.java2.lessons.tests.app.Store;
import ru.progwards.java2.lessons.tests.app.model.Account;
import ru.progwards.java2.lessons.tests.app.service.AccountService;
import ru.progwards.java2.lessons.tests.app.service.StoreService;
import ru.progwards.java2.lessons.tests.app.service.impl.AccountServiceImpl;
import ru.progwards.java2.lessons.tests.app.service.impl.StoreServiceImpl;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class AccountServiceImpl_constructor {

    static StoreService sservice;
    static Map<String, Account> store;
    static String testKey;
    static Account testAccount;

    @BeforeClass
    public static void init() {
        store = Store.getStore();
        sservice = new StoreServiceImpl();

        testAccount = new Account();
        testKey = UUID.randomUUID().toString();
        testAccount.setId(testKey);
        testAccount.setPin(2345);
        testAccount.setHolder("Account_for_tests");
        testAccount.setDate(new Date(System.currentTimeMillis() + 365 * 24 * 3600 * 1000));
        testAccount.setAmount(Math.random() * 1_000_000 - 5_000_000);
    }

    @Test(expected = NullPointerException.class)
    public void constructor_empty() {
        AccountService service = new AccountServiceImpl();
        service.deposit(testAccount, testAccount.getAmount() + 7);
    }

}