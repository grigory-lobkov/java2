package ru.progwards.java2.lessons.gc;

import java.util.ArrayList;
import java.util.NoSuchElementException;

public class BiHeap2int {

    //private List<int> items; // список элементов
    private IntList items; // список элементов
    private IntList data; // список данных

    // конструктор
    public BiHeap2int(int initSize) {
        items = new IntList(initSize);
        data = new IntList(initSize);
    }

    // восходящее восстановление свойства кучи
    private void shiftUp() {
        int pos = items.size() - 1;
        while (pos > 0) {
            int curr = (pos - 1) / 2;
            int iPos = items.get(pos);
            int iCurr = items.get(curr);
            if (iPos>iCurr) {
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
                if (items.get(rightChild)>items.get(leftChild)) {
                    max++;
                }
            }
            int iCurr = items.get(curr);
            int iMax = items.get(max);
            if (iCurr<iMax) {
                items.set(curr, iMax);
                items.set(max, iCurr);
                curr = max;
                leftChild = 2 * curr + 1;
            } else break;
        }
    }

    // добавление элемента в кучу
    public void insert(int item) {
        items.add(item);
        shiftUp();
    }

    // удаление первой вершины
    public int delete() throws NoSuchElementException {
        int s = items.size();
        if (s == 0) throw new NoSuchElementException();
        if (s == 1) return items.remove(0);
        int result = items.get(0);
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
    public int max() {
        return items.get(0);
    }


    public static void main(String[] args) {
        BiHeap2int test = new BiHeap2int(15);
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
            //System.out.print(test.max()+" ");
            test.delete();
            System.out.println(test.items);
        }
    }
}