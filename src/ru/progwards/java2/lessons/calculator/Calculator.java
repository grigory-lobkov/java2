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


    /*******************************************************************************************************************/


    public static int indexOf(Pattern pattern, String s) {
        Matcher matcher = pattern.matcher(s);
        return matcher.find() ? matcher.start() : -1;
    }

    final static char signMul = '*';
    final static char signDiv = '/';
    final static char signPlus = '+';
    final static char signMinus = '-';

    public static int calculate2(String expression) { // Произвольное количество цифр +34 минуты
        Pattern valPattern = Pattern.compile("[^0-9]");

        int val = indexOf(valPattern, expression);
        if (val < 0) return Integer.valueOf(expression);

        int result = Integer.valueOf(expression.substring(0, val));

        Character op = expression.charAt(val);
        String leastString = expression.substring(val + 1);
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

    /*******************************************************************************************************************/


    public static int bracketEnd(String expression) {
        int i = -1;
        int l = expression.length();
        int b = 0;
        while (++i < l) {
            char c = expression.charAt(i);
            if (c == ')') {
                if (b == 0) return i;
                else b--;
            } else {
                if (c == '(') b++;
            }
        }
        throw new RuntimeException("Cannot find closing bracket in '" + expression + "'");
    }
    public static int makeOper(int num1, char op, int num2) {
        switch (op) {
            case '*': return num1*num2;
            case '/': return num1/num2;
            case '+': return num1+num2;
            case '-': return num1-num2;
        }
        throw new RuntimeException("Operation '" + op + "' is unknown: "+num1+op+num2);
    }

    public static int calculate3(String expression) { // добавление скобок
        System.out.println("Calcalating:"+expression);
        int i = -1;
        int l = expression.length();
        int result = 0;
        boolean isNum = false;
        int numStart = -1;
        int numEnd = -1;
        char op = '+';
        while (++i < l) {
            char c = expression.charAt(i);
            if (c >= '0' && c <= '9') {
                if (!isNum) {
                    numStart = i;
                    isNum = true;
                }
                numEnd = i;
            } else {
                if (isNum) {
                    result = makeOper(result, op, Integer.valueOf(expression.substring(numStart,numEnd+1)));
                    isNum = false;
                }
                String leastExpr = expression.substring(i + 1);
                switch (c) {
                    case '+':
                        return result + calculate3(leastExpr);
                    case '-':
                        return result - calculate3(leastExpr);
                    case '*':
                    case '/':
                        op = c;
                        break;
                    case '(':
                        int brEnd = bracketEnd(leastExpr);
                        result = calculate3(leastExpr.substring(0, brEnd));
                        i += brEnd+1;
                        break;
                    case ')':
                        throw new RuntimeException("Unexpected '" + c + "'");
                    default:
                        throw new RuntimeException("Operation '" + c + "' is unknown!");
                }
            }
        }
        if (isNum) result = makeOper(result, op, Integer.valueOf(expression.substring(numStart,numEnd+1)));
        return result;
    }



    /*******************************************************************************************************************/
    /*******************************************************************************************************************/
    /*******************************************************************************************************************/



    private String  expression; // входной пример
    private int     result;     // результат вычислений
    private boolean firstPass = true; // Первый проход?
    private int     pos;        // в каком месте строки разбираем
    private int     len;        // общая длинна выражения
    private enum    OPERATIONS {PLUS, MINUS, MULTIPLE, DIVIDE}; // писок операций

    Calculator(String expression) {
        this.expression = expression;
        result = 0;
        pos = 0;
        len = expression.length();
    }
    Calculator(String expression, boolean firstPass) {
        this(expression);
        this.firstPass = firstPass;
    }
    public int getResult() {
        OPERATIONS op = OPERATIONS.PLUS;
        OPERATIONS op2 = null;
        while (hasNext()) { // цикл по сложениям и вычитаниям
            if (isOperation()) {
                op = getOperation();
            } else {
                int value = getElement();
                while (hasNext()) { // цикл по умножениям и делениям
                    op2 = getOperation();
                    if (op2 == OPERATIONS.MULTIPLE || op2 == OPERATIONS.DIVIDE) {
                        value = makeOperation(value, op2, getElement());
                    } else break;
                }
                result = makeOperation(result, op, value);
                op = op2;
            }
        }
        //System.out.println(expression+"="+result);
        return result;
    }
    private boolean hasNext() {
        return pos < len;
    }
    private boolean isOperation() {
        return "+-*/".contains(expression.substring(pos,pos+1));
    }
    private OPERATIONS getOperation() {
        char op = expression.charAt(pos++);
        switch (op) {
            case '+': return OPERATIONS.PLUS;
            case '-': return OPERATIONS.MINUS;
            case '*': return OPERATIONS.MULTIPLE;
            case '/': return OPERATIONS.DIVIDE;
        }
        throw new RuntimeException("Unknown operation '"+op+"'");
    }
    private char getChar() {
        return expression.charAt(pos++);
    }
    private boolean isBracket() {
        return expression.charAt(pos)=='(';
    }
    private String getBracket() {
        int from = ++pos;
        int b = 0;
        while (pos < len) {
            char c = expression.charAt(pos++);
            if (c == ')') {
                if (b == 0) {
                    return expression.substring(from, pos-1);
                }
                else b--;
            } else {
                if (c == '(') b++;
            }
        }
        throw new RuntimeException("Cannot find closing bracket in '" + expression + "'");
    }
    private boolean isValue() {
        return "0123456789".contains(expression.substring(pos,pos+1));
    }
    private int getValue() {
        int from = pos++;
        while (hasNext()&&isValue()) pos++;
        return Integer.valueOf(expression.substring(from, pos));
    }
    private int getElement() {
        if(isBracket()) {
            return new Calculator(getBracket()).getResult();
        } else if (isValue()) {
            return getValue();
        } else {
            throw new RuntimeException("Unknown symbol '"+getChar()+"'");
        }
    }
    private int makeOperation(int num1, OPERATIONS op, int num2) {
        switch (op) {
            case MULTIPLE:
                return num1 * num2;
            case DIVIDE:
                return num1 / num2;
            case PLUS:
                return num1 + num2;
            case MINUS:
                return num1 - num2;
        }
        throw new RuntimeException("Operation '" + op + "' is unknown: " + num1 + " " + op + " " + num2);
    }

    public static int calculate(String expression) { // добавление скобок, функциональная декомпозиция
        Calculator calculator = new Calculator(expression.replace(" ",""));
        return calculator.getResult();
    }

    public static void main(String[] args) {
        System.out.println(calculate("2+3*2+7*2")); //22
        System.out.println(calculate("11+01*2+01*005")); //18
        System.out.println(calculate("11+(02*2)+(-10/005+11)"));//11+4-2+11=24
        System.out.println(calculate("-11+1-(02*2)-(-10/005+11)"));//-23
    }
}