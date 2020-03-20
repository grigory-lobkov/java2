package ru.progwards.java2.lessons.tests.test.calc;

import org.junit.BeforeClass;
import org.junit.Test;
import ru.progwards.java2.lessons.tests.calc.SimpleCalculator;

import static org.junit.Assert.assertEquals;

public class SimpleCalculatorDiv2 {

    static SimpleCalculator calc;

    @BeforeClass
    public static void init() {
        calc = new SimpleCalculator();
    }

    @Test(expected = ArithmeticException.class)
    public void test__divByZero() {
        var result = calc.div(1, 0);
    }
}