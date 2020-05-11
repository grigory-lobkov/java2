package ru.progwards.java2.lessons.sort.simple;

/**
 * Сортировка пузырьком
 *
 * Проходим по массиву столько раз, сколько в нём элементов
 * Каждый проход делаем проход по списку оставшихя элементов и проверяем если правый элемент легче, то двигаем его в начало
 */

public class BubbleSort {

    public static <T extends Comparable<T>> void sort(T[] a) {

        final int l = a.length - 2;

        for (int i = 0; i <= l; i++)
            for (int j = l; j >= i; j--)
                if (a[j].compareTo(a[j + 1]) > 0) {
                    T t = a[j];
                    a[j] = a[j + 1];
                    a[j] = t;
                }
    }

}