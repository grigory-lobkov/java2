import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class CalcPTest {

    @Parameterized.Parameter(0)
    public int v1;

    @Parameterized.Parameter(1)
    public int v2;

    @Parameterized.Parameter(2)
    public int result;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        Object[][] data = new Object[][]{{1,2,3},{5,3,8},{121,4,125}};
        return Arrays.asList(data);
    }

    private Calc calc = new Calc();

    @Test
    public void sum() {
        int actual = calc.sum(v1,v2);
        int expected = result;
        Assert.assertEquals(actual,expected);
    }
}
