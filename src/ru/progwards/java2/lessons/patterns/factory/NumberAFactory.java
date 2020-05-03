package ru.progwards.java2.lessons.patterns.factory;

import ru.progwards.java2.lessons.patterns.factory.factories.*;

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
        NumberFactory factory;

        switch (type) {
            case "integer":
                factory = IntegerFactory.INSTANCE;
                break;
            case "complex":
                factory = ComplexFactory.INSTANCE;
                break;
            default:
                return null;
        }

        return factory.createNumber(nums);
    }

    static void print(Number n) {
        System.out.println(n.getClass().getSimpleName() + "=" + n);
    }

    public static void main(String[] args) {
        print(NumberAFactory.INSTANCE.getNumber("integer", 1));
        print(NumberAFactory.INSTANCE.getNumber("integer", 3021));
        print(NumberAFactory.INSTANCE.getNumber("integer", 82021));
        System.out.println("");
        print(NumberAFactory.INSTANCE.getNumber("complex", 82021, 3));
    }
}
