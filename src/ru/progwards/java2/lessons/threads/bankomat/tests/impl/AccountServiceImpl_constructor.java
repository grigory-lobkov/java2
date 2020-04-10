package ru.progwards.java2.lessons.threads.bankomat.tests.impl;

import org.junit.BeforeClass;
import org.junit.Test;
import ru.progwards.java2.lessons.threads.bankomat.Store;
import ru.progwards.java2.lessons.threads.bankomat.model.Account;
import ru.progwards.java2.lessons.threads.bankomat.service.AccountService;
import ru.progwards.java2.lessons.threads.bankomat.service.StoreService;
import ru.progwards.java2.lessons.threads.bankomat.service.impl.AccountServiceImpl;
import ru.progwards.java2.lessons.threads.bankomat.service.impl.StoreServiceImpl;

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
        Store s = new Store();
        store = s.getStore();
        sservice = new StoreServiceImpl(s);

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