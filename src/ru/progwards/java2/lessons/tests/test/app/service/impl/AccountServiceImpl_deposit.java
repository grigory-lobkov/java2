package ru.progwards.java2.lessons.tests.test.app.service.impl;

import org.junit.*;
import ru.progwards.java2.lessons.tests.app.Store;
import ru.progwards.java2.lessons.tests.app.model.Account;
import ru.progwards.java2.lessons.tests.app.service.AccountService;
import ru.progwards.java2.lessons.tests.app.service.StoreService;
import ru.progwards.java2.lessons.tests.app.service.impl.AccountServiceImpl;
import ru.progwards.java2.lessons.tests.app.service.impl.StoreServiceImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class AccountServiceImpl_deposit {

    static StoreService sservice;
    static AccountService service;
    static Map<String, Account> store;
    static String testKey;
    static Account testAccount;

    @BeforeClass
    public static void init() {
        store = Store.getStore();
        sservice = new StoreServiceImpl();
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

    @Test
    public void deposit_small() {
        testAccount.setAmount((200+Math.random() * 1_000_000));
        double deposit = 100.2d;
        var realValue = (testAccount.getAmount()+deposit)*100;

        service.deposit(testAccount,deposit);

        var gotValue = testAccount.getAmount()*100;
        Assert.assertEquals((long) realValue, (long) gotValue);
    }

    @Test
    public void deposit_zero() {
        testAccount.setAmount((200+Math.random() * 1_000_000));
        double deposit = 0.0d;
        var realValue = (testAccount.getAmount()+deposit)*100;

        service.deposit(testAccount,deposit);

        var gotValue = testAccount.getAmount()*100;
        Assert.assertEquals((long) realValue, (long) gotValue);
    }

    @Test
    public void deposit_big() {
        testAccount.setAmount((100+Math.random() * 1_000));
        double deposit = Long.MAX_VALUE / 100 - 10_000;
        var realValue = (testAccount.getAmount()+deposit)*100;

        service.deposit(testAccount,deposit);

        var gotValue = testAccount.getAmount()*100;
        Assert.assertEquals((long) realValue, (long) gotValue);
    }

    @Test
    public void deposit_veryBig() {
        testAccount.setAmount((100+Math.random() * 1_000));
        double deposit = ((double)Long.MAX_VALUE) * ((double)Long.MAX_VALUE) * (1.0d+Math.random());
        BigDecimal realValue = BigDecimal.valueOf(testAccount.getAmount()+deposit).setScale(2);

        service.deposit(testAccount,deposit);

        BigDecimal gotValue = BigDecimal.valueOf(testAccount.getAmount()).setScale(2);
        Assert.assertTrue("Expected and got values are not equal", realValue.compareTo(gotValue)==0);
    }

    @Test
    public void deposit_complexNum() {
        final String hardNumber = "123456789012345678901234567890.123456789";
        testAccount.setAmount(Double.valueOf(hardNumber));
        double deposit = 123456789.123456789d;
        BigDecimal realValue = BigDecimal
                .valueOf(testAccount.getAmount())
                .setScale(2, RoundingMode.HALF_UP)
                .add(BigDecimal.valueOf(deposit).setScale(2, RoundingMode.HALF_UP));

        service.deposit(testAccount,deposit);

        double gotVal = testAccount.getAmount();
        Assert.assertTrue("Amount volume is 'Infinity', despite of "+realValue, !Double.isInfinite(gotVal));

        BigDecimal gotValue = BigDecimal.valueOf(gotVal).setScale(2, RoundingMode.HALF_UP);
        Assert.assertTrue("Expected("+realValue+") and got("+gotValue+") values are not equal ", realValue.compareTo(gotValue)==0);
    }

    @Test(expected = RuntimeException.class)
    public void deposit_negative() {
        double deposit = -0.01d;
        service.deposit(testAccount,deposit);
    }

    @After
    public void after() {
        store.remove(testKey);
    }

}
