package ru.progwards.java2.lessons.annotation.jtest;

import ru.progwards.java2.lessons.annotation.jtest.JTest.*;

public class SimpleCalculator_test {

    SimpleCalculator calculator;

    @Before
    public void init() {
        calculator = new SimpleCalculator();
    }

    @Test(priority = 1)
    public void test_sum() {
        int v1 = 3;
        int v2 = 7;
        int expected = v1 + v2;
        int got = calculator.sum(v1, v2);
        Assert.Equals(expected, got);
    }

    @Test
    public void test_diff() {
        int v1 = 3;
        int v2 = 7;
        int expected = v1 - v2;
        int got = calculator.diff(v1, v2);
        Assert.Equals(expected, got);
    }

    @Test(priority = 2)
    public void test_mult() {
        int v1 = 3;
        int v2 = 7;
        int expected = v1 * v2+10000;
        int got = calculator.mult(v1, v2);
        Assert.Equals(expected, got);
    }

    @Test(priority = 3)
    public void test_div() {
        int v1 = 30;
        int v2 = 5;
        int expected = v1 / v2;
        int got = calculator.div(v1, v2);
        Assert.Equals(expected, got);
    }

    @After
    public void close() {
        calculator = null;
    }
}
