package ru.progwards.java2.lessons.sort.simple;

/**
 * Сортировка перемешиванием - метод двустороннего пузырька
 *
 * В начале пузырек идет слева направо, потом справа на лево, постоянно сужая границы
 * справа и слева - так пока две границы не сойдутся
 */

public class ShakerSort {

    // вариант без swap

    public static <T extends Comparable<T>> void sort(T[] a) {

        int left = 0;
        int right = a.length - 1;

        while (left < right) {
            for (int i = left; i < right; i++)
                if (a[i].compareTo(a[i + 1]) > 0) {
                    T t = a[i];
                    a[i] = a[i + 1];
                    a[i + 1] = t;
                }
            right--;
            for (int i = right; i > left; i--)
                if (a[i - 1].compareTo(a[i]) > 0) {
                    T t = a[i];
                    a[i] = a[i - 1];
                    a[i - 1] = t;
                }
            left++;
        }
    }

    // вариант с доп.методом swap

    public static <T extends Comparable<T>> void sort1(T[] a) {
        if (a.length == 0)
            return;

        int left = 0;
        int right = a.length - 1;
        while (left <= right) {
            for (int i = right; i > left; --i)
                if (a[i - 1].compareTo(a[i]) > 0)
                    swap(a, i - 1, i);
            left++;
            for (int i = left; i < right; ++i)
                if (a[i].compareTo(a[i + 1]) > 0)
                    swap(a, i, i + 1);
            right--;
        }
    }

    static <T> void swap(T[] a, int i, int j) {
        T tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

}