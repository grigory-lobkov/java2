package ru.progwards.java2.lessons.calculator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Calculator {

    //Реализуйте метод public static int calculate(String expression), который вычисляет арифметическое выражение,
    // заданное в виде символьной строки. Выражение содержит только целые цифры и знаки арифметических операций +-*/
    //При вычислении должны учитываться приоритеты операций, например, результат вычисления выражения "2+3*2"
    //должен быть равен 8. По оригинальному условию задачи все числа содержат не более одной цифры, пробелов в строке нет.
    public static int calculate1(String expression) { // оригинальный вариант, 17 минут
        String str = expression;
        int r = Integer.valueOf(str.substring(0, 1));
        if (str.length() == 1) return r;
        String op = str.substring(1, 2);
        while (op.compareTo("*") == 0 || op.compareTo("/") == 0) {
            if (op.compareTo("*") == 0)
                r *= Integer.valueOf(str.substring(2, 3));
            else if (op.compareTo("/") == 0)
                r /= Integer.valueOf(str.substring(2, 3));
            str = str.substring(2);
            if (str.length() == 1) return r;
            op = str.substring(1, 2);
        }
        if (op.compareTo("+") == 0)
            r += calculate1(str.substring(2));
        else if (op.compareTo("-") == 0)
            r -= calculate1(str.substring(2));
        return r;
    }

    public static int indexOf(Pattern pattern, String s) {
        Matcher matcher = pattern.matcher(s);
        return matcher.find() ? matcher.start() : -1;
    }

    final static char signMul = '*';
    final static char signDiv = '/';
    final static char signPlus = '+';
    final static char signMinus = '-';

    public static int calculate2(String expression) { // Произвольное количество цифр +34 минуты
        String str = expression;
        Pattern valPattern = Pattern.compile("[^0-9]");

        int val = indexOf(valPattern, str);
        if (val < 0) return Integer.valueOf(str);

        int result = Integer.valueOf(str.substring(0, val));

        Character op = str.charAt(val);
        String leastString = str.substring(val + 1);
        val = indexOf(valPattern, leastString);
        int r2 = Integer.valueOf(val < 0 ? leastString : leastString.substring(0, val));

        while (op.compareTo(signMul) == 0 || op.compareTo(signDiv) == 0) {

            if (op.compareTo(signMul) == 0)
                result *= r2;
            else if (op.compareTo(signDiv) == 0)
                result /= r2;
            if (val < 0) return result;

            op = leastString.charAt(val);
            leastString = leastString.substring(val + 1);
            val = indexOf(valPattern, leastString);
            r2 = Integer.valueOf(val < 0 ? leastString : leastString.substring(0, val));
        }
        if (op.compareTo(signPlus) == 0)
            result += calculate2(leastString);
        else if (op.compareTo(signMinus) == 0)
            result -= calculate2(leastString);
        return result;
    }

    public static void main(String[] args) {
        //System.out.println(calculate1("2+3*2+7*2"));
        System.out.println(calculate2("11+01*2+01*005"));
    }
}