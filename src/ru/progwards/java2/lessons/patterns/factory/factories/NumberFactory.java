package ru.progwards.java2.lessons.patterns.factory.factories;


import ru.progwards.java2.lessons.patterns.factory.abs.IntegerNumber;
import ru.progwards.java2.lessons.patterns.factory.complex.ComplexNumber;
import ru.progwards.java2.lessons.patterns.factory.complex.IntComplexNumber;

public interface NumberFactory {

    // создание нового действительного числа
    IntegerNumber createInteger(int num);

    // создание нового комплексного числа, a - действительная часть, b - мнимая часть
    default ComplexNumber createComplex(int a, int b) {
        ComplexNumber num = new IntComplexNumber(a, b);
        return num;
    }

}
