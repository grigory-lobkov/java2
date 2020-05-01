package ru.progwards.java2.lessons.patterns.factory.abs;


public class IntIntegerNumber extends IntegerNumber {

    public static int MIN_VALUE = Integer.MIN_VALUE;
    public static int MAX_VALUE = Integer.MAX_VALUE;

    int value;

    public IntIntegerNumber(int value) {
        this.value = value;
        bytes = 4;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    @Override
    public int toInt() {
        return value;
    }

    @Override
    public IntegerNumber add2(IntegerNumber num1) {
        return new IntIntegerNumber((int) (value + num1.toInt()));
    }

}
