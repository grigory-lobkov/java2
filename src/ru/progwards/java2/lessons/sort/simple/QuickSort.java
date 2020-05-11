package ru.progwards.java2.lessons.sort.simple;

/**
 * Быстрая сортировка массива
 *
 * По методу Ломуто и по методу Хоара
 *
 * При исключительных случаях вырождается в O(N2), но работает на порядки быстрее пузырька
 *
 * Ломуто: Последний элемент делаем опорным, ведём к нему все элементы больше него. Как дошли - ставим справа. Повторяем.
 * Хоара: Средний элемент считаем опорным и ищем слева меньшие, справа большие, когда находим - меняем местами. Повторяем.
 */

public class QuickSort {

    public static <T extends Comparable<T>> int hoare(T[] a, int left, int right) {
        T pivot = a[(left + right) / 2];
        int i = left - 1;
        int j = right + 1;
        for (; ; ) {
            do {
                i++;
            } while (a[i].compareTo(pivot) < 0);

            do {
                j--;
            } while (a[j].compareTo(pivot) > 0);

            if (i >= j)
                return j;

            swap(a, i, j);
        }
    }

    public static <T extends Comparable<T>> int lomuto(T[] a, int left, int right) {
        int i = left;
        T pivot = a[right];
        for (int j = left; j < right; j++)
            if (a[j].compareTo(pivot) < 0)
                swap(a, i++, j);
        swap(a, i, right);
        return i;
    }

    static <T> void swap(T[] a, int i, int j) {
        T tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    public static <T extends Comparable<T>> void sortHoare(T[] a, int left, int right) {
        if (left < right) {
            int pivot = hoare(a, left, right);
            sortHoare(a, left, pivot);
            sortHoare(a, pivot + 1, right);
        }
    }

    public static <T extends Comparable<T>> void sortLomuto(T[] a, int left, int right) {
        if (left < right) {
            int pivot = lomuto(a, left, right);
            sortLomuto(a, left, pivot - 1);
            sortLomuto(a, pivot + 1, right);
        }
    }

    public static <T extends Comparable<T>> void sort(T[] a) {
        sortLomuto(a, 0, a.length - 1);
    }

    public static <T extends Comparable<T>> void sort2(T[] a) {
        sortHoare(a, 0, a.length - 1);
    }
}