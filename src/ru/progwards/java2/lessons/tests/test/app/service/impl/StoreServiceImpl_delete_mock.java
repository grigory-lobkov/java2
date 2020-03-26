package ru.progwards.java2.lessons.tests.test.app.service.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import ru.progwards.java2.lessons.tests.app.Store;
import ru.progwards.java2.lessons.tests.app.model.Account;
import ru.progwards.java2.lessons.tests.app.service.StoreService;
import ru.progwards.java2.lessons.tests.app.service.impl.StoreServiceImpl;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StoreServiceImpl_delete_mock {

    @Mock
    Store store;
    @Mock
    Account account;

    @InjectMocks
    StoreService service = new StoreServiceImpl();

    /*@Before
    public void init() {
        //service = new StoreServiceImpl();
    }*/

    @Test//(expected = RuntimeException.class)
    public void delete_exception() {
        String id = "d32l984rfq3gWERG";
        when(store.getStore().get(id)).thenReturn(null);

        service.delete(id);
    }

    @Test//(expected = RuntimeException.class)
    public void delete_exception2() {
        String id = "d32l984rfq3gWERG";
        Store test = mock(Store.class);
        Map<String, Account> map = mock(HashMap.class);
        Account a = new Account();
        when(test.getStore()).thenReturn(map);
        when(map.get(id)).thenReturn(a);
        assertEquals(test.getStore().get(id), null);
    }

    @Test//(expected = RuntimeException.class)
    public void delete_exception3() {
        service = mock(StoreServiceImpl.class);
        String id = "d32l984rfq3gWERG";
        when(store.getStore().get(id)).thenReturn(null);

        service.delete(id);
    }

/*    @Test
    public void delete_testAccount() {
        service.delete(testKey);
        Assert.assertTrue("test account is still exists", !exists);
    }*/

}
