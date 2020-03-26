package ru.progwards.java2.lessons.tests.test.calc;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import ru.progwards.java2.lessons.tests.calc.SimpleCalculator;

public class SimpleCalculator_mock {

    @Test
    public void sumTest(){
        SimpleCalculator calculator =  Mockito.mock(SimpleCalculator.class);
        Mockito.when(calculator.sum(1,2)).thenReturn(1000);
        int actual = calculator.sum(1,2);
        int expected = 1000;

        Assert.assertEquals(expected, actual);
    }

}