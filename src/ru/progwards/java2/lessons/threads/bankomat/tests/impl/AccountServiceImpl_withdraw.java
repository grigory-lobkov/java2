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

public class AccountServiceImpl_withdraw {

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
        testAccount.setId(UUID.randomUUID().toString());
        testAccount.setPin(2345);
        testAccount.setHolder("Account_for_tests");
        testAccount.setDate(new Date(System.currentTimeMillis() + 365 * 24 * 3600 * 1000));
        testAccount.setAmount(Math.random() * 1_000_000);

        testKey = testAccount.getId();
    }

    @Before
    public void before() {
        store.put(testKey, testAccount);
    }

    @Test(expected = RuntimeException.class)
    public void withdraw_negative() {
        double withdraw = -0.01d;
        service.withdraw(testAccount,withdraw);
    }

    @Test
    public void withdraw_small() {
        testAccount.setAmount((200+Math.random() * 1_000_000));
        double withdraw = 100.22d;
        var realValue = (testAccount.getAmount()-withdraw)*100;

        service.withdraw(testAccount,withdraw);

        var gotValue = testAccount.getAmount()*100;
        Assert.assertEquals((long) realValue, (long) gotValue);
    }

    @Test
    public void withdraw_zero() {
        testAccount.setAmount((200+Math.random() * 1_000_000));
        double withdraw = 0.0d;
        var realValue = (testAccount.getAmount()-withdraw)*100;

        service.withdraw(testAccount,withdraw);

        var gotValue = testAccount.getAmount()*100;
        Assert.assertEquals((long) realValue, (long) gotValue);
    }

    @Test(expected = RuntimeException.class)
    public void withdraw_notEnougth() {
        testAccount.setAmount((100+Math.random() * 1_000));
        double withdraw = 10_000.0;
        var realValue = (testAccount.getAmount()-withdraw)*100;

        service.withdraw(testAccount,withdraw);

        var gotValue = testAccount.getAmount()*100;
        Assert.assertEquals((long) realValue, (long) gotValue);
    }

    @Test
    public void withdraw_big() {
        testAccount.setAmount(Long.MAX_VALUE - 10_000);
        double withdraw = Long.MAX_VALUE / 2;
        var realValue = (testAccount.getAmount()-withdraw)*100;

        service.withdraw(testAccount,withdraw);

        var gotValue = testAccount.getAmount()*100;
        Assert.assertEquals((long) realValue, (long) gotValue);
    }

    @Test
    public void withdraw_veryBig() {
        testAccount.setAmount(((double)Long.MAX_VALUE) * ((double)Long.MAX_VALUE) * (1.0d+Math.random()));
        double withdraw = 123456789.123456789d;
        BigDecimal realValue = BigDecimal.valueOf(testAccount.getAmount()-withdraw).setScale(2);

        service.withdraw(testAccount,withdraw);

        BigDecimal gotValue = BigDecimal.valueOf(testAccount.getAmount()).setScale(2);
        Assert.assertTrue("Expected and got values are not equal", realValue.compareTo(gotValue)==0);
    }

    @Test
    public void withdraw_complexNum() {
        final String hardNumber = "123456789012345678901234567890.123456789";
        testAccount.setAmount(Double.valueOf(hardNumber));
        double withdraw = 123456789.123456789d;
        BigDecimal realValue = BigDecimal
                .valueOf(testAccount.getAmount())
                .setScale(2, RoundingMode.HALF_UP)
                .subtract(BigDecimal.valueOf(withdraw).setScale(2, RoundingMode.HALF_UP));

        service.withdraw(testAccount,withdraw);

        double gotVal = testAccount.getAmount();
        Assert.assertTrue("Amount volume is 'Infinity', despite of "+realValue, !Double.isInfinite(gotVal));

        BigDecimal gotValue = BigDecimal.valueOf(gotVal).setScale(2, RoundingMode.HALF_UP);
        Assert.assertTrue("Expected("+realValue+") and got("+gotValue+") values are not equal ", realValue.compareTo(gotValue)==0);
    }

    @After
    public void after() {
        store.remove(testKey);
    }

}
