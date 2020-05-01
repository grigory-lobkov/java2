package ru.progwards.java2.lessons.patterns.factory.abs;


public class ByteIntegerNumber extends IntegerNumber {

    public static int MIN_VALUE = Byte.MIN_VALUE;
    public static int MAX_VALUE = Byte.MAX_VALUE;

    byte value;

    public ByteIntegerNumber(byte value) {
        this.value = value;
        bytes = 1;
    }

    @Override
    public String toString() {
        return Byte.toString(value);
    }

    @Override
    public byte toByte() {
        return value;
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
        return new ByteIntegerNumber((byte) (value + num1.toByte()));
    }

}
