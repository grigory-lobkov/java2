package ru.progwards.java2.lessons.patterns.factory;

import ru.progwards.java2.lessons.patterns.factory.factories.ByteFactory;
import ru.progwards.java2.lessons.patterns.factory.factories.IntFactory;
import ru.progwards.java2.lessons.patterns.factory.factories.NumberFactory;
import ru.progwards.java2.lessons.patterns.factory.factories.ShortFactory;

/**
 * Абстрактная фабрика для чисел, синглетон
 *
 * Возвращает число определенного типа, поддерживаются в зависимости от {@code type}:
 *   integer - целое - расширяют класс {@code IntegerNumber}
 *   complex - комплексное - наследуют интерфейс {@code ComplexNumber}
 *
 * Определение размерности типа ({@code byte}, {@code short}, {@code int}) происходит
 * автоматически, по первому числу определения значения
 * Если числа не заданы, то берется тип {@code int}
 *
 * @author Gregory Lobkov
 */

public enum NumberAFactory {
    INSTANCE;

    /**
     * Получить число
     *
     * @param type тип числа
     * @param nums значение числа по умолчанию
     * @return число определенного параметрами типа
     */
    public Number getNumber(String type, int... nums) {
        NumberFactory factory = IntFactory.INSTANCE;

        int a = 0;
        int b = 0;

        if (nums.length >= 1) {
            a = nums[0];
            if (nums.length >= 2)
                b = nums[1];

            if (a <= ByteFactory.INSTANCE.MAX_VALUE && a >= ByteFactory.INSTANCE.MIN_VALUE)
                factory = ByteFactory.INSTANCE;
            else if (a <= ShortFactory.INSTANCE.MAX_VALUE && a >= ShortFactory.INSTANCE.MIN_VALUE)
                factory = ShortFactory.INSTANCE;
        }

        switch (type) {
            case "integer":
                return factory.createInteger(a);
            case "complex":
                return factory.createComplex(a, b);
        }
        return null;
    }

    static void print(Number n) {
        System.out.println(n.getClass().getSimpleName() + "=" + n);
    }

    public static void main(String[] args) {
        print(NumberAFactory.INSTANCE.getNumber("integer"));
        print(NumberAFactory.INSTANCE.getNumber("integer", 1));
        print(NumberAFactory.INSTANCE.getNumber("integer", 3021));
        print(NumberAFactory.INSTANCE.getNumber("integer", 82021));
        System.out.println("");
        print(NumberAFactory.INSTANCE.getNumber("complex"));
        print(NumberAFactory.INSTANCE.getNumber("complex", 1));
        print(NumberAFactory.INSTANCE.getNumber("complex", 3021));
        print(NumberAFactory.INSTANCE.getNumber("complex", 82021, 3));
        print(NumberAFactory.INSTANCE.getNumber("complex", 82021));
    }
}
