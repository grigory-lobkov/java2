import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class CalculatorCalculatePE {


    @Parameterized.Parameter
    public String expression;


    @Parameterized.Parameters(name = "calculate({0}}) = exception")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                //{"1+2)"},
                {"(3-4"},
                {"2*4t"},
                {"9p4"},
                {"3/1("},
                {")1-4"},
                //{"(2))*3"},
                {"(1-1)+(2+2"},
                {"(3/2-(6*6)"}
        });
    }

    @Test(expected = Exception.class)
    public void test__exceptions() throws Exception {
        Calculator.calculate(expression);
    }

}
