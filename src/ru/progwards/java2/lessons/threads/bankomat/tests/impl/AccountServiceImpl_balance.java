package ru.progwards.java2.lessons.threads.bankomat.tests.impl;

import org.junit.*;
import ru.progwards.java2.lessons.threads.bankomat.Store;
import ru.progwards.java2.lessons.threads.bankomat.model.Account;
import ru.progwards.java2.lessons.threads.bankomat.service.AccountService;
import ru.progwards.java2.lessons.threads.bankomat.service.StoreService;
import ru.progwards.java2.lessons.threads.bankomat.service.impl.AccountServiceImpl;
import ru.progwards.java2.lessons.threads.bankomat.service.impl.StoreServiceImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class AccountServiceImpl_balance {

    static StoreService sservice;
    static AccountService service;
    static Map<String, Account> store;
    static String testKey;
    static Account testAccount;

    @BeforeClass
    public static void init() {
        Store s = new Store();
        store = s.getStore();
        sservice = new StoreServiceImpl(s);
        service = new AccountServiceImpl(sservice);

        testAccount = new Account();
        testKey = UUID.randomUUID().toString();
        testAccount.setId(testKey);
        testAccount.setPin(2345);
        testAccount.setHolder("Account_for_tests");
        testAccount.setDate(new Date(System.currentTimeMillis() + 365 * 24 * 3600 * 1000));
        testAccount.setAmount(Math.random() * 1_000_000 - 5_000_000);
    }

    @Before
    public void before() {
        store.put(testKey, testAccount);
    }

    @Test
    public void balance_negative() {
        testAccount.setAmount(-1 - Math.random() * 1_000_000);
        var realValue = testAccount.getAmount()*100;
        var gotValue = service.balance(testAccount)*100;
        Assert.assertEquals((long) realValue, (long) gotValue);
    }

    @Test
    public void balance_zero() {
        testAccount.setAmount(0);
        var realValue = testAccount.getAmount()*100;
        var gotValue = service.balance(testAccount)*100;
        Assert.assertEquals((long) realValue, (long) gotValue);
    }

    @Test
    public void balance_positive() {
        testAccount.setAmount(1 + Math.random() * 1_000_000);
        var realValue = testAccount.getAmount()*100;
        var gotValue = service.balance(testAccount)*100;
        Assert.assertEquals((long) realValue, (long) gotValue);
    }

    @Test
    public void balance_complexNum() {
        final String hardNumber = "123456789012345678901234567890.123456789";
        testAccount.setAmount(Double.valueOf(hardNumber));
        BigDecimal realValue = new BigDecimal(hardNumber)
                .setScale(2, RoundingMode.HALF_UP);

        var gotVal = service.balance(testAccount);

        BigDecimal gotValue = BigDecimal
                .valueOf(gotVal)
                .setScale(2, RoundingMode.HALF_UP);
        Assert.assertTrue("Expected("+realValue+") and got("+gotValue+") values are not equal ", realValue.compareTo(gotValue)==0);
    }

    @After
    public void after() {
        store.remove(testKey);
    }

}