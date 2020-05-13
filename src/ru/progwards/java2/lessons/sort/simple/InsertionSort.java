package ru.progwards.java2.lessons.sort.simple;

/**
 * Сортировка вставками
 *
 * Перебираем все элементы сподряд и если элемент меньше, чем тот который слева - двигаем
 * тот на наше место и сравниваем со следующим левым. Когда нашли элемент меньше нас,
 * останавливаемся и сохраняем в освободившееся место себя.
 *
 * Замеры скоростей:
 * sort1: 11517
 * sort2: 5
 * sort3: 1034
 * sort: 1010
 */

public class InsertionSort {

    // Базовый алгоритм

    public static <T extends Comparable<T>> void sort1(T[] a) {
        for (int j = 1; j < a.length; j++) {
            T cur = a[j];
            int i = j - 1;
            while (i >= 0 && a[i].compareTo(cur) > 0) {
                a[i + 1] = a[i];
                i--;
            }
            a[i + 1] = cur;
        }
    }

    // Делаем сдвиг вызовом системной функции - нельзя сдвинуть слева направо

    public static <T extends Comparable<T>> void sort2(T[] a) {
        for (int j = 1; j < a.length; j++) {
            T cur = a[j];
            int i = j - 1;
            while (i >= 0 && a[i].compareTo(cur) > 0) {
                //a[i + 1] = a[i];
                i--;
            }
            if (i + 2 < a.length && j - i - 2 > 0) {
                System.arraycopy(a, i + 1, a, i + 2, j - i - 2); // нельзя использовать, затирает всё одним значением
            }
            a[i + 1] = cur;
        }
    }

    // Перевернули порядок обхода массива - теперь идем сконца
    // и используем системную функцию копирование справа на лево

    public static <T extends Comparable<T>> void sort3(T[] a) {

        for (int j = a.length - 1; j >= 0; j--) {
            T cur = a[j];
            int i = j + 1;
            while (i < a.length && a[i].compareTo(cur) < 0) {
                //a[i + 1] = a[i];
                i++;
            }
            if (i - j - 1 > 0) {
                System.arraycopy(a, j+1, a, j, i - j - 1);
                a[i - 1] = cur;
            }
        }
    }

    // Добавили переменную длинны массива на основе sort3

    public static <T extends Comparable<T>> void sort(T[] a) {

        final int l = a.length;

        for (int j = l - 1; j >= 0; j--) {
            T cur = a[j];
            int i = j + 1;
            while (i < l && a[i].compareTo(cur) < 0) {
                i++;
            }
            if (i - j - 1 > 0) {
                System.arraycopy(a, j+1, a, j, i - j - 1);
                a[i - 1] = cur;
            }
        }
    }

    // Находим позицию для нулевого элемента в отсортированном массиве

    public static <T extends Comparable<T>> void sortZeroQuick(T[] a) {

        int low = 2;
        int high = a.length - 1;
        T el = a[0];
        if(high==0 || a[0].compareTo(a[1]) < 0) return;

        while(low<high) {
            int mid = (low + high) / 2;
            int cmp = el.compareTo(a[mid]);
            if (cmp > 0) {
                low = mid + 1;
            } else if (cmp < 0) {
                high = mid - 1;
            } else if (cmp == 0) {
                low = mid;
                high = mid;
                break;
            }
        }
        if(low<=high) {
            int cmp = el.compareTo(a[low]);
            if (cmp <= 0) low--;
        } else if(low>high) {
            low = high;
        }
        System.arraycopy(a, 1, a, 0, low);
        a[low] = el;
    }


    // Сортировка в указанных пределах

    public static <T extends Comparable<T>> void sortInLimits(T[] a, int from, int to) {

        final int l = to+1;

        for (int j = to; j >= from; j--) {
            T cur = a[j];
            int i = j + 1;
            while (i < l && a[i].compareTo(cur) < 0) {
                i++;
            }
            if (i - j - 1 > 0) {
                System.arraycopy(a, j+1, a, j, i - j - 1);
                a[i - 1] = cur;
            }
        }
    }
}