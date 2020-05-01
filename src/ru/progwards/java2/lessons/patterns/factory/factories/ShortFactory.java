package ru.progwards.java2.lessons.patterns.factory.factories;

import ru.progwards.java2.lessons.patterns.factory.abs.IntegerNumber;
import ru.progwards.java2.lessons.patterns.factory.abs.ShortIntegerNumber;


public enum ShortFactory implements NumberFactory {
    INSTANCE;

    public int MIN_VALUE = ShortIntegerNumber.MIN_VALUE;
    public int MAX_VALUE = ShortIntegerNumber.MAX_VALUE;

    // создание нового действительного числа
    @Override
    public IntegerNumber createInteger(int num) {

        if(num < ShortIntegerNumber.MIN_VALUE || num > ShortIntegerNumber.MAX_VALUE)
            throw new UnsupportedOperationException();

        short value = (short)num;

        ShortIntegerNumber number = new ShortIntegerNumber(value);

        return number;

    }

}
