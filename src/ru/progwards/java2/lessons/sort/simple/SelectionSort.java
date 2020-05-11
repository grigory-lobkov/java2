package ru.progwards.java2.lessons.sort.simple;

/**
 * Сортировка выбором
 *
 * Доработанный пузырьковый метод, но сравниваем всегда с одним элементом
 * Берем эталоны по порядку от первого до последнего
 *
 * Замеры скоростей методов:
 * sort0: 43075 ms
 * sort2: 14519 ms
 * sort3: 14894 ms
 * sort: 13020 ms
 */

public class SelectionSort {

    // Базовая версия, как нашли меньше, сразу меняем местами

    public static <T extends Comparable<T>> void sort0(T[] a) {
        for (int i = 0; i < a.length; i++)
            for (int j = i + 1; j < a.length; j++)
                if (a[i].compareTo(a[j]) < 0) {
                    T tmp = a[i];
                    a[i] = a[j];
                    a[j] = tmp;
                }
    }

    // Будем находить индекс минимального элемента и менять только если найден - в 5 раз лучше sort0

    public static <T extends Comparable<T>> void sort2(T[] a) {
        for (int i = 0; i < a.length; i++) {
            int min = i;
            for (int j = i + 1; j < a.length; j++) {
                if (a[min].compareTo(a[j]) < 0)
                    min = j;
            }
            if (min != i) {
                T tmp = a[i];
                a[i] = a[min];
                a[min] = tmp;
            }
        }
    }

    // Будем менять всегда, не проверяя, найден или нет - хуже sort2

    public static <T extends Comparable<T>> void sort3(T[] a) {
        for (int i = 0; i < a.length; i++) {
            int min = i;
            for (int j = i + 1; j < a.length; j++) {
                if (a[min].compareTo(a[j]) < 0)
                    min = j;
            }
            T tmp = a[i];
            a[i] = a[min];
            a[min] = tmp;
        }
    }

    // Заведем временную переменную длинны массива - лучше sort2

    public static <T extends Comparable<T>> void sort(T[] a) {

        final int l = a.length;

        for (int i = 0; i < l; i++) {
            int min = i;
            for (int j = i + 1; j < l; j++)
                if (a[min].compareTo(a[j]) < 0)
                    min = j;

            if (min != i) {
                T tmp = a[i];
                a[i] = a[min];
                a[min] = tmp;
            }
        }
    }

}