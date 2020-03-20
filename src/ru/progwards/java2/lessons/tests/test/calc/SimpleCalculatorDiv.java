package ru.progwards.java2.lessons.tests.test.calc;

        import org.junit.BeforeClass;
        import org.junit.Test;
        import org.junit.runner.RunWith;
        import org.junit.runners.Parameterized;
        import ru.progwards.java2.lessons.tests.calc.SimpleCalculator;

        import java.util.Arrays;
        import java.util.Collection;

        import static org.junit.Assert.assertEquals;
        import static org.junit.Assert.assertThat;
        import static org.hamcrest.CoreMatchers.is;

@RunWith(value = Parameterized.class)
public class SimpleCalculatorDiv {

    static SimpleCalculator calc;

    @BeforeClass
    public static void init() {
        new SimpleCalculatorDiv2();
        calc = new SimpleCalculator();
    }

    @Parameterized.Parameter(0)
    public int numberA;

    @Parameterized.Parameter(1)
    public int numberB;

    @Parameterized.Parameter(2)
    public int expected;

    @Parameterized.Parameters(name = "div({0}/{1}) = {2}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {1, 1, 1},
                {2, -1, -2},
                {-8, 2, -4},
                {-40, -5, 8},
                {5, 5, 1}
        });
    }

    @Test
    public void test__divTwoNumbes() {
        var result = calc.div(numberA, numberB);
        assertEquals(expected, result);
        //assertThat(result, is(expected));
    }
}
