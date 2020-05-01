package ru.progwards.java2.lessons.patterns.factory.complex;

/*
класс, реализующий операции в комплексных числах.

Напоминаем, что комплексное число записывается как z = a + bi,
где z это комплексное число, a действительная часть, b мнимая часть, i обозначение мнимой части.
a и b реализуем целыми числами
*/

public class IntComplexNumber implements ComplexNumber {

    int a; // действительная часть
    int b; // мнимая часть

    // создание нового комплексного числа, a - действительная часть, b - мнимая часть
    public IntComplexNumber(int a, int b) {
        this.a = a;
        this.b = b;
    }

    // создание нового комплексного числа, a - действительная часть, b - мнимая часть
    @Override
    public IntComplexNumber newComplexNum(int a, int b) {
        return new IntComplexNumber(a, b);
    }

    @Override
    public String toString() {
        return a + "+" + b + "i";
    }

    // сложение комплексных чисел по формуле: (a + bi) + (c + di) = (a + c) + (b + d)i
    @Override
    public IntComplexNumber add(IntComplexNumber num1, IntComplexNumber num2) {
        return new IntComplexNumber(num1.a + num2.a, num1.b + num2.b);
    }
    // вычитание комплексных чисел по формуле: (a + bi) - (c + di) = (a - c) + (b - d)i
    @Override
    public IntComplexNumber sub(IntComplexNumber num1, IntComplexNumber num2) {
        return new IntComplexNumber(num1.a - num2.a, num1.b - num2.b);
    }
    // умножение комплексных чисел по формуле: (a + bi) * (c + di) = (a*c - b*d) + (b*c + a*d)i
    @Override
    public IntComplexNumber mul(IntComplexNumber num1, IntComplexNumber num2) {
        int a = num1.a, b = num1.b, c = num2.a, d = num2.b;
        return new IntComplexNumber(a * c - b * d, b * c + a * d);
    }
    // деление комплексных чисел по формуле:
    //(a + bi) / (c + di) = (a*c + b*d)/(c*c+d*d) + ((b*c - a*d)/(c*c+d*d))i
    @Override
    public IntComplexNumber div(IntComplexNumber num1, IntComplexNumber num2) {
        int a = num1.a, b = num1.b, c = num2.a, d = num2.b;
        return new IntComplexNumber((a * c + b * d) / (c * c + d * d),
                              (b * c - a * d) / (c * c + d * d));
    }

    // tests
    public static void main(String[] args) {
        IntComplexNumber a = new IntComplexNumber(2, 2);
        IntComplexNumber b = new IntComplexNumber(1, 1);
        System.out.println(a.add(a, b));
        System.out.println(a.sub(a, b));
        System.out.println(a.mul(a, b));
        System.out.println(a.div(a, b));
    }
}
