package ru.progwards.java2.lessons.patterns.factory.factories;

import ru.progwards.java2.lessons.patterns.factory.abs.IntegerNumber;
import ru.progwards.java2.lessons.patterns.factory.abs.IntIntegerNumber;
import ru.progwards.java2.lessons.patterns.factory.complex.ComplexNumber;
import ru.progwards.java2.lessons.patterns.factory.complex.IntComplexNumber;


public enum IntFactory implements NumberFactory {
    INSTANCE;

    public int MIN_VALUE = IntIntegerNumber.MIN_VALUE;
    public int MAX_VALUE = IntIntegerNumber.MAX_VALUE;

    // создание нового действительного числа
    @Override
    public IntegerNumber createInteger(int num) {

        IntIntegerNumber number = new IntIntegerNumber(num);

        return number;

    }

    // создание нового комплексного числа, a - действительная часть, b - мнимая часть
    @Override
    public ComplexNumber createComplex(int a, int b) {

        ComplexNumber num = new IntComplexNumber(a, b);

        return num;

    }

}

