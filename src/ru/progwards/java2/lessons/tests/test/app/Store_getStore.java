package ru.progwards.java2.lessons.tests.test.app;

import org.junit.Assert;
import org.junit.Test;
import ru.progwards.java2.lessons.tests.app.Store;
import ru.progwards.java2.lessons.tests.app.model.Account;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

public class Store_getStore {

    static Map<String, Account> getStore;
    static String key;
    static Account account;
    static {
        getStore = Store.getStore();
        if (getStore!=null && getStore.size()>0) {
            for (String k:getStore.keySet()) {
                key = k;
                break;
            }
            account = getStore.get(key);
        } else {
            key = null;
            account = null;
        }
    }

    @Test
    public void getStore_notNull() {
        Assert.assertNotNull(getStore);
    }

    @Test
    public void getStore_notEmpty() {
        if (getStore==null) return;

        var result = getStore.size();

        Assert.assertNotEquals(0, result);
    }

    @Test
    public void getStore_key_isBigger_5() {
        if (key==null) return;

        Assert.assertTrue(key.length()>5);
    }

    @Test
    public void getStore_account_getId_length() {
        if (account==null) return;
        var id = account.getId();
        Assert.assertNotNull(id);

        Assert.assertTrue(id.length()>0);
    }

    @Test
    public void getStore_account_getHolder_length() {
        if (account==null) return;
        var holder = account.getHolder();
        Assert.assertNotNull(holder);

        Assert.assertTrue(holder.length()>0);
    }

    @Test
    public void getStore_account_getDate_minValue() {
        if (account==null) return;
        var date = account.getDate();
        Assert.assertNotNull(date);
        var dateMin = Date.from(Instant.parse("2000-01-01T00:00:00.00Z"));

        Assert.assertTrue(date.after(dateMin));
    }

    @Test
    public void getStore_account_getAmount_positive() {
        if (account==null) return;
        var amount = account.getAmount();
        Assert.assertNotNull(amount);

        Assert.assertTrue(amount>=0);
    }

    @Test
    public void getStore_account_getPin_minValue() {
        if (account==null) return;
        var pin = account.getPin();
        Assert.assertNotNull(pin);

        Assert.assertTrue(pin>=1000);
    }



    @Test
    public void getStore_account_setId() { // так то надо тестировать, чтобы id не менялся, но раз уж тут так реализовано...
        if (account==null) return;
        var id = account.getId();
        if (id==null) return;
        var newVal = id + 't';

        account.setId(newVal);

        var nowVal = account.getId();
        Assert.assertEquals(newVal, nowVal);
    }

    @Test
    public void getStore_account_setHolder() {
        if (account==null) return;
        var newVal = "Tester T-estovich Testovichkin"; // чтобы точно не существовал

        account.setHolder(newVal);

        var nowVal = account.getHolder();
        Assert.assertEquals(newVal, nowVal);
    }

    @Test
    public void getStore_account_setDate() {
        if (account==null) return;
        var newVal = Date.from(Instant.now());

        account.setDate(newVal);

        var nowVal = account.getDate();
        Assert.assertEquals(newVal, nowVal);
    }

    @Test
    public void getStore_account_setAmount() {
        if (account==null) return;
        var amount = account.getAmount();
        var newVal = amount + 9 + 100000 * Math.random();

        account.setAmount(newVal);

        var nowVal = account.getAmount();
        Assert.assertEquals((long)newVal, (long)nowVal);
    }

    @Test
    public void getStore_account_setPin() {
        if (account == null) return;
        var pin = account.getPin();
        var newVal = (pin + 1111) % 10000;

        account.setPin(newVal);

        var nowVal = account.getPin();
        Assert.assertEquals(newVal, nowVal);
    }

}
