package ru.progwards.java2.lessons.sort;

/**
 * Сортировка перемешиванием - метод двустороннего пузырька
 *
 * В начале пузырек идет слева направо, потом справа на лево, постоянно сужая границы
 * справа и слева - так пока две границы не сойдутся
 */

public class ShakerSort {

    public static<T extends Comparable<T>> void sort(T[] a) {

        int left = 0;
        int right = a.length - 1;

        while (left < right) {
            for (int i = left; i < right; i++)
                if (a[i].compareTo(a[i+1]) > 0) {
                    T t = a[i];
                    a[i] = a[i+1];
                    a[i+1] = t;
                }
            right--;
            for (int i = right; i > left; i--)
                if (a[i-1].compareTo(a[i]) > 0) {
                    T t = a[i];
                    a[i] = a[i-1];
                    a[i-1] = t;
                }
            left++;
        }
    }

}
