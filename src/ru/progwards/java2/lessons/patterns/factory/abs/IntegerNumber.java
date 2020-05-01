package ru.progwards.java2.lessons.patterns.factory.abs;

import ru.progwards.java2.lessons.patterns.factory.Number;


public abstract class IntegerNumber implements Number {

    int bytes;

    public IntegerNumber add(IntegerNumber num1, IntegerNumber num2) {
        //if(num1 instanceof IntegerNumber && num2 instanceof IntegerNumber)
        //    return ((IntegerNumber)num1).bytes >= ((IntegerNumber)num2).bytes ? num1.add2(num2) : num2.add2(num1);
        //else
        //    throw new RuntimeException(num1.getClass().getSimpleName()+" or "+num2.getClass().getSimpleName()+" is not "+IntegerNumber.class.getSimpleName());
        return num1.bytes >= num2.bytes ? num1.add2(num2) : num2.add2(num1);
    }

    public abstract IntegerNumber add2(IntegerNumber num);

    public abstract String toString();

    public byte toByte() {
        return 0;
    }

    public short toShort() {
        return 0;
    }

    public int toInt() {
        return 0;
    }

    // test
    public static void main(String[] args) {
        IntegerNumber num1 = new ByteIntegerNumber((byte)10);
        IntegerNumber num2 = new ShortIntegerNumber((short)1314);
        System.out.println(num1.add(num1, num2));
    }
}
