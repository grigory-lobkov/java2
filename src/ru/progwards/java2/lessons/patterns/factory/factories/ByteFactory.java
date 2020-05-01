package ru.progwards.java2.lessons.patterns.factory.factories;

import ru.progwards.java2.lessons.patterns.factory.abs.ByteIntegerNumber;
import ru.progwards.java2.lessons.patterns.factory.abs.IntegerNumber;


public enum ByteFactory implements NumberFactory {
    INSTANCE;

    public int MIN_VALUE = ByteIntegerNumber.MIN_VALUE;
    public int MAX_VALUE = ByteIntegerNumber.MAX_VALUE;

    // создание нового действительного числа
    @Override
    public IntegerNumber createInteger(int num) {

        if(num < ByteIntegerNumber.MIN_VALUE || num > ByteIntegerNumber.MAX_VALUE)
            throw new UnsupportedOperationException();

        byte value = (byte)num;

        ByteIntegerNumber number = new ByteIntegerNumber(value);

        return number;

    }

}
