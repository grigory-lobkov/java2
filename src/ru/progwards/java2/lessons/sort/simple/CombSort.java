package ru.progwards.java2.lessons.sort.simple;

/**
 * Сортировка массива "Расческой"
 *
 * Бежим по паре не далеко стоящих элементов и сравниваем их между собой,
 * меняем местами при необходимости, пока есть что сравнивать
 */

public class CombSort {

    public static <E extends Comparable<? super E>> void sort(E[] a) {

        int gap = a.length;
        boolean swapped = true;

        while (gap > 1 || swapped) {

            if (gap > 1)
                gap = (int) (gap / 1.247330950103979);

            swapped = false;
            int i = 0;

            while (i + gap < a.length) {
                if (a[i].compareTo(a[i + gap]) > 0) {
                    E t = a[i];
                    a[i] = a[i + gap];
                    a[i + gap] = t;
                    swapped = true;
                }
                i++;
            }

        }
    }

}