package ru.progwards.java2.lessons.gc;

import java.util.*;
import java.lang.*;

public class BiHeap<T extends Comparable<T>> {

    private List<T> items; // список элементов

    // конструктор
    public BiHeap() {
        items = new ArrayList<T>();
    }

    // восходящее восстановление свойства кучи
    private void shiftUp() {
        int pos = items.size() - 1;
        while (pos > 0) {
            int curr = (pos - 1) / 2;
            T iPos = items.get(pos);
            T iCurr = items.get(curr);
            if (iPos.compareTo(iCurr) > 0) {
                items.set(pos, iCurr);
                items.set(curr, iPos);
                pos = curr;
            } else break;
        }
    }

    // нисходящее восстановление свойства кучи
    private void shiftDown() {
        int curr = 0;
        int s = items.size();
        int leftChild = 2 * curr + 1;
        while (leftChild < s) {
            int max = leftChild;
            int rightChild = leftChild + 1;
            if (rightChild < s) {
                if (items.get(rightChild).compareTo(items.get(1)) > 0) {
                    max++;
                }
            }
            T iCurr = items.get(curr);
            T iMax = items.get(max);
            if (iCurr.compareTo(iMax) < 0) {
                items.set(curr, iMax);
                items.set(max, iCurr);
                curr = max;
                leftChild = 2 * curr + 1;
            } else break;
        }
    }

    // добавление элемента в кучу
    public void insert(T item) {
        items.add(item);
        shiftUp();
    }

    // удаление первой вершины
    public T delete() throws NoSuchElementException {
        int s = items.size();
        if (s == 0) throw new NoSuchElementException();
        if (s == 1) return items.remove(0);
        T result = items.get(0);
        items.set(0, items.remove(s - 1));
        shiftDown();
        return result;
    }

    // размер кучи
    public int size() {
        return items.size();
    }

    // проверка на пустоту
    public boolean isEmpty() {
        return items.isEmpty();
    }

    // преобразование в строку
    public String toString() {
        return items.toString();
    }

    // максимальный элемент
    public T max() {
        return items.get(0);
    }

/*    // вывод кучи на экран
    public void print() {
        int s = items.size();
        for (T i: items) {
            System.out.print(i.toString() + " ");
        }
        System.out.println();
    }*/


    public static void main(String[] args) {
        BiHeap<Integer> test = new BiHeap<Integer>();
        test.insert(16);
        test.insert(9);
        test.insert(11);
        test.insert(10);
        test.insert(13);
        test.insert(31);
        test.insert(19);
        test.insert(2);
        test.insert(50);
        test.insert(23);
        System.out.println(test);
        while (!test.isEmpty()) {
            System.out.print(test.max().toString() + " ");
            test.delete();
        }
    }

}