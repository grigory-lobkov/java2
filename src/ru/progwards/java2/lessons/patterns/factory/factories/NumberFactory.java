package ru.progwards.java2.lessons.patterns.factory.factories;


import ru.progwards.java2.lessons.patterns.factory.Number;
import ru.progwards.java2.lessons.patterns.factory.abs.IntegerNumber;
import ru.progwards.java2.lessons.patterns.factory.complex.ComplexNumber;
import ru.progwards.java2.lessons.patterns.factory.complex.IntComplexNumber;

public interface NumberFactory {

    // создание нового числа
    Number createNumber(int... nums);

}
