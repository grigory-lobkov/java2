import org.junit.*;

import static org.junit.Assert.*;

import java.io.*;

public class CalcTest {

    private Calc calc;
    private static BufferedReader fis;
    private static int param1;
    private static int param2;
    private static int expect;

    @BeforeClass
    public static void init() {
        try {
            fis = new BufferedReader(new FileReader("test/test.txt"));
            param1 = Integer.parseInt(fis.readLine());
            param2 = Integer.parseInt(fis.readLine());
            expect = Integer.parseInt(fis.readLine());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Before
    public void initTest() {
        calc = new Calc();
    }

    @Test(timeout=2000)
    public void testSum() {
        int expected = expect;
        int actual = calc.sum(param1, param2);
        assertEquals(expected,actual);
    }

    @Test
    public void testSum2() {
        int expected = -5;
        int actual = calc.sum(-1,-4);
        assertEquals(expected,actual);
    }

    @Ignore
    @Test(timeout=200)
    public void testDif() {
        int expected = 3;
        int actual = calc.dif(1, 2);
        assertEquals(expected,actual);
    }

    @Test(expected = ArithmeticException.class)
    public void testDiv() {
        int expected = -1;
        int actual = calc.div(1, 0);
        assertEquals(expected,actual);
    }

    @After
    public void destroyedTest() {
        calc = null;
    }

    @AfterClass
    static public void destroyClass() {
        try {
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
