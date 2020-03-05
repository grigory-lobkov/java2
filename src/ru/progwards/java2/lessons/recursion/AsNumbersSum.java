package ru.progwards.java2.lessons.recursion;

import javax.management.relation.RelationSupport;

public class AsNumbersSum {

    private static final String eqSign = " = ";
    private static final String plSign = "+";

    // раскладывает параметр number, как всевозможные уникальные комбинации сумм натуральных чисел, например:
    // 5 = 4+1 = 3+2 = 3+1+1 = 2+2+1 = 2+1+1+1 = 1+1+1+1+1
    public static String asNumbersSum(int number) {
        return number+getVariants("", number, 0, "");
    }

    public static String getVariants(String beforeStr, int number, int now, String afterStr) {
        if (number <= 1) return "";
        System.out.println("in: " + beforeStr + "," + number + "," + now + "," + afterStr);
        String result;
        if (now == 0) result = beforeStr + number + afterStr + getVariants(beforeStr, number - 1, now + 1, afterStr);
        else
            result = beforeStr + number + plSign + now + afterStr + getVariants(beforeStr, number - 1, now + 1, afterStr);
        /*if(now>0) {
            if (number > 1 && number > now)
                result = result + eqSign + beforeStr + getVariants(beforeStr, number - now, 0, afterStr) + plSign + getVariants(beforeStr, now, 0, afterStr) + afterStr;
        } else {
            result = result + getVariants(beforeStr, number-1, 1, afterStr);
        }*/
        System.out.println("in: " + beforeStr + "," + number + "," + now + "," + afterStr + " out: " + result);
        return result;
    }

    public static void main(String[] args) {
        System.out.println(asNumbersSum(3));
    }
}
