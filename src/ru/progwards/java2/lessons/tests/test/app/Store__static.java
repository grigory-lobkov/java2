package ru.progwards.java2.lessons.tests.test.app;

import org.junit.Test;
import ru.progwards.java2.lessons.tests.app.Store;

public class Store__static {

        @Test(timeout = 200)  // this test must be run first to Store!  // не знаю, как иначе затестить таймаут статической инициализации
        public void static_init_timeout() {
            Store.getStore();
        }

}
