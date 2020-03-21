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

public class AccountServiceImpl_transfer {

    static StoreService sservice;
    static AccountService service;
    static Map<String, Account> store;
    static Account accountFrom;
    static Account accountTo;

    @BeforeClass
    public static void init() {
        store = Store.getStore();
        sservice = new StoreServiceImpl();
        service = new AccountServiceImpl(sservice);

        accountFrom = new Account();
        accountFrom.setId(UUID.randomUUID().toString());
        accountFrom.setPin(2345);
        accountFrom.setHolder("Account_for_outcome");
        accountFrom.setDate(new Date(System.currentTimeMillis() + 360 * 24 * 3600 * 1000));
        accountFrom.setAmount(1_000 + Math.random() * 1_000);

        accountTo = new Account();
        accountTo.setId(UUID.randomUUID().toString());
        accountTo.setPin(4567);
        accountTo.setHolder("Account_for_income");
        accountTo.setDate(new Date(System.currentTimeMillis() + 300 * 24 * 3600 * 1000));
        accountTo.setAmount(Math.random() * 1_000);
    }

    @Before
    public void before() {
        store.put(accountFrom.getId(), accountFrom);
        store.put(accountTo.getId(), accountTo);
    }

    @Test(expected = RuntimeException.class)
    public void transfer_negative() {
        double transfer = -0.01d;
        service.transfer(accountFrom,accountTo,transfer);
    }

    @Test
    public void transfer_small() {
        accountFrom.setAmount(200.10d);
        double transfer = 10.22d;
        var fromExpected = (accountFrom.getAmount()-transfer)*100;
        var toExpected = (accountTo.getAmount()+transfer)*100;

        service.transfer(accountFrom,accountTo,transfer);

        var fromGot = (accountFrom.getAmount())*100;
        var toGot = (accountTo.getAmount())*100;
        Assert.assertEquals("Amount of 'from' account is wrong", (long) fromExpected, (long) fromGot);
        Assert.assertEquals("Amount of 'to' account is wrong", (long) toExpected, (long) toGot);
    }

    @Test
    public void transfer_zero() {
        accountFrom.setAmount(200.10d);
        double transfer = 0.00d;
        var fromExpected = (accountFrom.getAmount()-transfer)*100;
        var toExpected = (accountTo.getAmount()+transfer)*100;

        service.transfer(accountFrom,accountTo,transfer);

        var fromGot = (accountFrom.getAmount())*100;
        var toGot = (accountTo.getAmount())*100;
        Assert.assertEquals("Amount of 'from' account is wrong", (long) fromExpected, (long) fromGot);
        Assert.assertEquals("Amount of 'to' account is wrong", (long) toExpected, (long) toGot);
    }

    @Test(expected = RuntimeException.class)
    public void transfer_notEnougth() {
        accountFrom.setAmount(200.10d);
        double transfer = 10000.00d;

        service.transfer(accountFrom,accountTo,transfer);
    }

    @Test
    public void transfer_big() {
        accountFrom.setAmount(Long.MAX_VALUE - 10_000);
        double transfer = Long.MAX_VALUE / 2;
        var fromExpected = (accountFrom.getAmount()-transfer)*100;
        var toExpected = (accountTo.getAmount()+transfer)*100;

        service.transfer(accountFrom,accountTo,transfer);

        var fromGot = (accountFrom.getAmount())*100;
        var toGot = (accountTo.getAmount())*100;
        Assert.assertEquals("Amount of 'from' account is wrong", (long) fromExpected, (long) fromGot);
        Assert.assertEquals("Amount of 'to' account is wrong", (long) toExpected, (long) toGot);
    }

    @Test
    public void transfer_veryBig() {
        accountFrom.setAmount(((double)Long.MAX_VALUE) * ((double)Long.MAX_VALUE) * (1.0d+Math.random()));
        double transfer = 123456789.123456789d;
        var fromExpected = BigDecimal.valueOf(accountFrom.getAmount()-transfer).setScale(2, RoundingMode.HALF_UP);
        var toExpected = BigDecimal.valueOf(accountTo.getAmount()+transfer).setScale(2, RoundingMode.HALF_UP);

        service.transfer(accountFrom,accountTo,transfer);

        var fromGot = BigDecimal.valueOf(accountFrom.getAmount()).setScale(2, RoundingMode.HALF_UP);
        var toGot = BigDecimal.valueOf(accountTo.getAmount()).setScale(2, RoundingMode.HALF_UP);
        Assert.assertTrue("Amount of 'from' account is wrong", fromExpected.compareTo(fromGot)==0);
        Assert.assertTrue("Amount of 'to' account is wrong", toExpected.compareTo(toGot)==0);
    }

    @Test
    public void transfer_complexNum() {
        final String hardNumber = "123456789012345678901234567890.123456789";
        accountFrom.setAmount(Double.valueOf(hardNumber));
        double transfer = 123456789.123456789d;
        BigDecimal fromExpected = BigDecimal
                .valueOf(accountFrom.getAmount())
                .setScale(2, RoundingMode.HALF_UP)
                .subtract(BigDecimal.valueOf(transfer).setScale(2, RoundingMode.HALF_UP));
        BigDecimal toExpected = BigDecimal
                .valueOf(accountTo.getAmount())
                .setScale(2, RoundingMode.HALF_UP)
                .add(BigDecimal.valueOf(transfer).setScale(2, RoundingMode.HALF_UP));

        service.transfer(accountFrom,accountTo,transfer);

        double fromGotD = accountFrom.getAmount();
        Assert.assertTrue("Amount volume is 'Infinity', despite of "+fromExpected, !Double.isInfinite(fromGotD));
        double toGotD = accountFrom.getAmount();
        Assert.assertTrue("Amount volume is 'Infinity', despite of "+toExpected, !Double.isInfinite(toGotD));

        BigDecimal fromGot = BigDecimal.valueOf(fromGotD).setScale(2, RoundingMode.HALF_UP);
        Assert.assertTrue("Expected("+fromExpected+") and got("+fromGot+") values are not equal ", fromExpected.compareTo(fromGot)==0);
        BigDecimal toGot = BigDecimal.valueOf(fromGotD).setScale(2, RoundingMode.HALF_UP);
        Assert.assertTrue("Expected("+toExpected+") and got("+toGot+") values are not equal ", toExpected.compareTo(toGot)==0);
    }

    @After
    public void after() {
        store.remove(accountFrom.getId());
        store.remove(accountTo.getId());
    }
}
