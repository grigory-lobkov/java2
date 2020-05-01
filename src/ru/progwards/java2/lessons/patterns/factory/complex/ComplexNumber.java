package ru.progwards.java2.lessons.patterns.factory.complex;

import ru.progwards.java2.lessons.patterns.factory.Number;

public interface ComplexNumber extends Number {

    // создание нового комплексного числа, a - действительная часть, b - мнимая часть
    IntComplexNumber newComplexNum(int a, int b);

    // сложение комплексных чисел по формуле: (a + bi) + (c + di) = (a + c) + (b + d)i
    IntComplexNumber add(IntComplexNumber num1, IntComplexNumber num2);

    // вычитание комплексных чисел по формуле: (a + bi) - (c + di) = (a - c) + (b - d)i
    IntComplexNumber sub(IntComplexNumber num1, IntComplexNumber num2);

    // умножение комплексных чисел по формуле: (a + bi) * (c + di) = (a*c - b*d) + (b*c + a*d)i
    IntComplexNumber mul(IntComplexNumber num1, IntComplexNumber num2);

    // деление комплексных чисел по формуле:
    //(a + bi) / (c + di) = (a*c + b*d)/(c*c+d*d) + ((b*c - a*d)/(c*c+d*d))i
    IntComplexNumber div(IntComplexNumber num1, IntComplexNumber num2);

    @Override
    String toString();
}
