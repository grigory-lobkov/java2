package ru.progwards.java2.lessons.patterns.factory.abs;


public class ShortIntegerNumber extends IntegerNumber {

    public static int MIN_VALUE = Short.MIN_VALUE;
    public static int MAX_VALUE = Short.MAX_VALUE;

    short value;

    public ShortIntegerNumber(short value) {
        this.value = value;
        bytes = 2;
    }

    @Override
    public String toString() {
        return Short.toString(value);
    }

    @Override
    public short toShort() {
        return value;
    }

    @Override
    public int toInt() {
        return value;
    }

    @Override
    public IntegerNumber add2(IntegerNumber num1) {
        return new ShortIntegerNumber((short) (value + num1.toShort()));
    }

}
