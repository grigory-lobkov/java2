package ru.progwards.java2.lessons.gc;

import java.util.NoSuchElementException;

public class BiHeap2int {

    // Бинарная куча, содержащая за элементами данные
    // в верхушке - максимальный элемент

    //private List<int> items; // список элементов
    protected IntList items; // список элементов
    protected IntList datas; // список данных

    // конструктор
    public BiHeap2int(int initSize) {
        items = new IntList(initSize);
        datas = new IntList(initSize);
    }

    // восходящее восстановление свойства кучи от индекса
    private void shiftUp(int startIdx) {
        int pos = startIdx;
        while (pos > 0) {
            int curr = (pos - 1) / 2;
            int iPos = items.get(pos);
            int iCurr = items.get(curr);
            if (iPos>iCurr) {
                items.set(pos, iCurr);
                items.set(curr, iPos);
                datas.swap(pos, curr);
                pos = curr;
            } else break;
        }
    }

    // нисходящее восстановление свойства кучи от индекса
    private void shiftDown(int startIdx) {
        int curr = startIdx;
        int s = items.size;
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
                datas.swap(max, curr);
                curr = max;
                leftChild = 2 * curr + 1;
            } else break;
        }
    }

    // добавление элемента в кучу
    public void insert(int item, int data) {
        items.add(item);
        datas.add(data);
        shiftUp(items.size - 1);
    }

    // удаление произвольной вершины
    public void delete(int idx) throws NoSuchElementException {
        int s = items.size-1;
        if (s < idx) {
            throw new NoSuchElementException();
        } else if (s == idx) {
            datas.remove(idx);
            items.remove(idx);
        } else {
            items.set(idx, items.remove(s));
            datas.set(idx, datas.remove(s));
            shiftDown(0);
        }
    }

    // обновление данных вершины
    public void update(int idx, int newVal, int newData) {
        int oldVal = items.get(idx);
        datas.set(idx, newData);
        items.set(idx, newVal);
        if (oldVal > newVal) {
            shiftDown(idx);
        } else {
            shiftUp(idx);
        }
    }

    // размер кучи
    public int size() {
        return items.size;
    }

    // проверка на пустоту
    public boolean isEmpty() {
        return items.isEmpty();
    }

    // преобразование в строку
    public String toString() {
        int size = datas.size;
        StringBuilder sb = new StringBuilder(size*12);
        int[] ePoses = datas.nums;
        int[] eSizes = items.nums;
        for(int i=0; i< size; i++) {
            sb.append(eSizes[i]+":"+ePoses[i]+",");
        }
        return "BiHeap{" + sb.toString() + " size=" + size +'}';
    }

    // максимальный элемент
    public int maxVal() {
        return items.get(0);
    }
    // данные, подвязанные к вершине кучи
    public int maxValData() {
        return datas.get(0);
    }

    // провести быстрый поиск в куче, найти элемент с наиболее подходящим значением, не меньше заданного
    public int findMinValItemIdx(int minValue) {
        int pos = items.size - 1;
        while (pos > 0) {
            int curr = (pos - 1) / 2;
            if (items.get(pos)<minValue) {
                pos = curr;
            } else {
                return pos;
            }
        }
        //pos=0 on exit
        if(items.get(0)>=minValue) {
            //System.out.println("findMinValItemIdx: size="+items.get(0)+" for asked="+minValue);
            return 0;
        }
        throw new NoSuchElementException();
    }
    public int getVal(int idx) {
        return items.get(idx);
    }
    public int getValData(int idx) {
        return datas.get(idx);
    }


    public static void main(String[] args) {
        BiHeap2int h = new BiHeap2int(15);
        h.insert(16, 1);
        h.insert(9, 2);
        h.insert(11, 3);
        h.insert(10, 4);
        h.insert(13, 5);
        h.insert(31, 6);
        h.insert(19, 7);
        h.insert(2, 8);
        h.insert(50, 9);
        h.insert(23, 10);
        System.out.println(h);
        int i = h.findMinValItemIdx(2);
        System.out.println("idx="+i+" v="+h.getVal(i)+" d="+h.getValData(i));
        while (!h.isEmpty()) {
            //System.out.print(test.max()+" ");
            h.delete(0);
            System.out.println(h.items);
        }
    }
}