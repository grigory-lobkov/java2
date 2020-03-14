package ru.progwards.java2.lessons.generics;

import java.util.ArrayList;
import java.util.List;

public class Tests {

    // Создайте статический метод с именем from, который принимает параметром массив,
    // обобщающего типа, создает новый ArrayList, копирует в него содержимое массива и возвращает ArrayList в качестве результата метода.

    static <T> ArrayList<T> from(T... items) {
        ArrayList<T> result = new ArrayList<T>(items.length);
        for (T i: items) {
            result.add(i);
        }
        return result;
    }

    // Создайте статический метод с именем swap типа  void, который принимает параметром List, обобщающего типа, и два int как индексы в списке.
    // Реализовать метод, который меняет элементы с указанными индексами местами.

    static <T> void swap(List<T> list, int i1, int i2) {
        T tmp = list.get(i1);
        list.set(i1, list.get(i2));
        list.set(i2, tmp);
    }

    // Определен enum CompareResult {LESS, EQUAL, GREATER};
    //
    //Создайте статический метод с именем compare, который содержит 2 параметра обобщающего типа, и сравнивает их через метод compareTo. Метод compare должен возвращать CompareResult, причем
    //
    //CompareResult.LESS если первый параметр меньше второго
    //CompareResult.GREATER если первый параметр больше второго
    //CompareResult.EQUAL если первый параметр равен второму

    enum CompareResult {LESS, EQUAL, GREATER};
    static <T extends Comparable> CompareResult compare(T obj1, T obj2) {
        int rslt = obj1.compareTo(obj2);
        if(rslt==0) return CompareResult.EQUAL;
        else if(rslt>0) return CompareResult.GREATER;
        else return CompareResult.LESS;
    }

}