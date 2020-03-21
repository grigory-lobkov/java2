

public class Calc {
    public int sum (int a, int b) {
        return a+b;
    }
    public int dif (int a, int b) {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return a+b;
    }
 public int div (int a, int b) {
     return a/b;
 }
}
