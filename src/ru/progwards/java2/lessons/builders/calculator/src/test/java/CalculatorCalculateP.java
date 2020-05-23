package ru.progwards.java2.lessons.builders.calculator.src.test.java;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import ru.progwards.java2.lessons.builders.calculator.src.main.java.Calculator;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(value = Parameterized.class)
public class CalculatorCalculateP {


    @Parameterized.Parameter(0)
    public String expression;

    @Parameterized.Parameter(1)
    public int expected;


    @Parameterized.Parameters(name = "calculate({0}}) = {1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"1+2",  3},
                {"3-4",  -1},
                {"2*4",  8},
                {"9/4",  2},
                {"3/1",  3},
                {"(1-4)", -3},
                {"(2)*3", 6},
                {"(1-1)+(2+2)", 4},
                {"(3/2)-(6*6)", -35},
                {"(2-1)*(5+(1*2)/(4-3*3*3/3/3))", 7},
                {"(((((2+3*0/2)))))", 2}
        });
    }

    @Test
    public void test__all() throws Exception {
        int result = Calculator.calculate(expression);
        assertEquals(expected, result);
    }

}
