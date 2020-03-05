package ru.progwards.java2.lessons.recursion;

public class AsNumbersSum {

    private static final String eqSign = " = ";
    private static final String plSign = "+";

    // раскладывает параметр number, как всевозможные уникальные комбинации сумм натуральных чисел, например:
    // 5 = 4+1 = 3+2 = 3+1+1 = 2+2+1 = 2+1+1+1 = 1+1+1+1+1
    public static String asNumbersSum(int number) {
        //return number + getVariants("", number - 1, 1, "");
        return number + getVariants(number-1, 1, "");
    }

//    public static String getVariants(String beforeStr, int number, int now, String afterStr) {
//        if (number <= 1 || now > number) return "";
//        System.out.println("in: " + beforeStr + "," + number + "," + now + "," + afterStr);
//        String result = getVariants(beforeStr, number - 1, now + 1, afterStr);
//        if (now == 0) result = beforeStr + number + afterStr + result;
//        else result = eqSign + beforeStr + number + plSign + now + afterStr + result;
//        /*if(now>0) {
//            if (number > 1 && number > now)
//                result = result + eqSign + beforeStr + getVariants(beforeStr, number - now, 0, afterStr) + plSign + getVariants(beforeStr, now, 0, afterStr) + afterStr;
//        } else {
//            result = result + getVariants(beforeStr, number-1, 1, afterStr);
//        }*/
//        System.out.println("in: " + beforeStr + "," + number + "," + now + "," + afterStr + " out: " + result);
//        return result;
//    }

//    public static String getVariants(String beforeStr, int number, int now, String afterStr) {
//        if (number < 1 || now > number) return "";
//        String result;
//        result = eqSign + beforeStr + number + plSign + now + afterStr
//                + getVariants(beforeStr + number + plSign, now - 1, 1, afterStr)
//                + getVariants(beforeStr, number - 1, now + 1, afterStr);
//                //+ getVariants2(beforeStr, number, now, afterStr);
//        if (number % 2 == 0) {
//            int n = number / 2;
//            //result += getVariants2(beforeStr, number / 2, afterStr);
//            if (n >= now) {
//                String tmpOnes = getOnes(now, afterStr);
//                result += eqSign + beforeStr + n + plSign + n + plSign + now + afterStr
//                        + getVariants(beforeStr + n + plSign + n + plSign, now - 1, 1, afterStr)
//                        + getVariants(beforeStr + n + plSign, n - 1, 1, getOnes(now, afterStr))
//                        + getVariants(beforeStr, n - 1, 1, getOnes(n, tmpOnes));
//            }
//        }
//
//        //System.out.println("in: " + beforeStr + "," + number + "," + now + "," + afterStr + " out: " + result);
//        return result;
//    }
//    public static String getVariants2(String beforeStr, int number, int now, String afterStr) {
//        if (number % 2 == 0 && number >= 1) {
//            int n = number / 2;
//            if (n >= now) {
//                String tmpOnes = getOnes(now, afterStr);
//                String tmpOnes2 = getOnes(n, tmpOnes);
//                return eqSign + beforeStr + n + plSign + n + plSign + now + afterStr
//                        + getVariants(beforeStr + n + plSign + n + plSign, now - 1, 1, afterStr)
//                        + getVariants(beforeStr + n + plSign, n - 1, 1, getOnes(now, afterStr))
//                        + getVariants(beforeStr, n - 1, 1, tmpOnes2)
//                        + getVariants2(beforeStr, n-1, 1, getOnes(n, tmpOnes2));
//            }
//        }
//        return "";
//    }

    public static String getOnes(int now, String afterStr) {
        return now > 0 ? getOnes(now - 1, plSign + "1" + afterStr) : afterStr;
    }

    public static String getVariants(int n, int i, String p) {  // капец какой-то! еле еле решил - часов 6 бился :( ... но решение получилось красивое в итоге! мне нравится! :)
        return
                (n <= 0 ? "" :
                        (i > n
                                ? getVariants(n, i - n, p + n + plSign)
                                : eqSign + p + n + plSign + i
                                + getVariants(i - 1, 1, p + n + plSign)
                        )
                                + getVariants(n - 1, i + 1, p)
                );
    }

    public static void main(String[] args) {
        System.out.println(asNumbersSum(-1));
        System.out.println(asNumbersSum(0));
        System.out.println(asNumbersSum(1));
        System.out.println(asNumbersSum(2));
        System.out.println("2 = 1+1.");
        System.out.println(asNumbersSum(3));
        System.out.println("3 = 2+1 = 1+1+1.");
        System.out.println(asNumbersSum(4));
        System.out.println("4 = 3+1 = 2+2 = 2+1+1 = 1+1+1+1.");
        System.out.println(asNumbersSum(5));
        System.out.println("5 = 4+1 = 3+2 = 3+1+1 = 2+2+1 = 2+1+1+1 = 1+1+1+1+1.");
        System.out.println(asNumbersSum(6));
        System.out.println("6 = 5+1 = 4+2 = 4+1+1 = 3+3 = 3+2+1 = 3+1+1+1 = 2+2+2 = 2+2+1+1 = 2+1+1+1+1 = 1+1+1+1+1+1.");
        System.out.println(asNumbersSum(7));
        System.out.println("7 = 6+1 = 5+2 = 5+1+1 = 4+3 = 4+2+1 = 4+1+1+1 = 3+3+1 = 3+2+2 = 3+2+1+1 = 3+1+1+1+1 = 2+2+2+1 = 2+2+1+1+1 = 2+1+1+1+1+1 = 1+1+1+1+1+1+1.");
        System.out.println(asNumbersSum(8));
        System.out.println("8 = 7+1 = 6+2 = 6+1+1 = 5+3 = 5+2+1 = 5+1+1+1 = 4+4 = 4+3+1 = 4+2+2 = 4+2+1+1 = 4+1+1+1+1 = 3+3+2 = 3+3+1+1 = 3+2+2+1 = 3+2+1+1+1 = 3+1+1+1+1+1 = 2+2+2+2 = 2+2+2+1+1 = 2+2+1+1+1+1 = 2+1+1+1+1+1+1 = 1+1+1+1+1+1+1+1.");
        System.out.println(asNumbersSum(9));
        System.out.println("9 = 8+1 = 7+2 = 7+1+1 = 6+3 = 6+2+1 = 6+1+1+1 = 5+4 = 5+3+1 = 5+2+2 = 5+2+1+1 = 5+1+1+1+1 = 4+4+1 = 4+3+2 = 4+3+1+1 = 4+2+2+1 = 4+2+1+1+1 = 4+1+1+1+1+1 = 3+3+3 = 3+3+2+1 = 3+3+1+1+1 = 3+2+2+2 = 3+2+2+1+1 = 3+2+1+1+1+1 = 3+1+1+1+1+1+1 = 2+2+2+2+1 = 2+2+2+1+1+1 = 2+2+1+1+1+1+1 = 2+1+1+1+1+1+1+1 = 1+1+1+1+1+1+1+1+1.");
        System.out.println(asNumbersSum(10));
        System.out.println("10 = 9+1 = 8+2 = 8+1+1 = 7+3 = 7+2+1 = 7+1+1+1 = 6+4 = 6+3+1 = 6+2+2 = 6+2+1+1 = 6+1+1+1+1 = 5+5 = 5+4+1 = 5+3+2 = 5+3+1+1 = 5+2+2+1 = 5+2+1+1+1 = 5+1+1+1+1+1 = 4+4+2 = 4+4+1+1 = 4+3+3 = 4+3+2+1 = 4+3+1+1+1 = 4+2+2+2 = 4+2+2+1+1 = 4+2+1+1+1+1 = 4+1+1+1+1+1+1 = 3+3+3+1 = 3+3+2+2 = 3+3+2+1+1 = 3+3+1+1+1+1 = 3+2+2+2+1 = 3+2+2+1+1+1 = 3+2+1+1+1+1+1 = 3+1+1+1+1+1+1+1 = 2+2+2+2+2 = 2+2+2+2+1+1 = 2+2+2+1+1+1+1 = 2+2+1+1+1+1+1+1 = 2+1+1+1+1+1+1+1+1 = 1+1+1+1+1+1+1+1+1+1.");
    }

}