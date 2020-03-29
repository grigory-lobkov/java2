package ru.progwards.java2.lessons.gc;

import java.util.NoSuchElementException;

public class IntList {

    // Список int - работает аналогично ArrayList, только хранит не объекты, а примитив int

    protected int[] nums;
    protected int size;
    private int incPercent = 50; // процент приращения при переполнении

    IntList(int size) {
        this.size = 0;
        nums = new int[size];
    };

    // возвращает количество элементов
    public int size() {
        return size;
    }

    // проверка на пустоту
    public boolean isEmpty() {
        return size==0;
    }

    // добавляет элемент num в конец массива
    public void add(int item) {
        int l = nums.length;
        if(size == l) {
            int[] nums1 = new int[l + l * incPercent / 100];
            System.arraycopy(nums, 0, nums1, 0, l);
            nums1[l] = item;
            nums = nums1;
        } else {
            nums[size++] = item;
        }
    }
    // удаляет элемент в позиции pos массива
    public int remove(int pos) {
        if(pos-1==size) {
            return nums[--size];
        } else if(pos>=size) {
            throw new NoSuchElementException("Position "+pos+" not found");
        } else {
            int result = nums[pos];
            System.arraycopy(nums, pos + 1, nums, pos, size - pos - 1);
            size--;
            return result;
        }
    }
    // возвращает элемент по индексу pos
    public int get(int pos) {
        return nums[pos];
    }
    // устанавливает элемент по индексу pos
    public void set(int pos, int item) {
        nums[pos] = item;
    }
    // меняет два элемента местами
    public void swap(int pos1, int pos2) {
        int t = nums[pos1];
        nums[pos1] = nums[pos2];
        nums[pos2] = t;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(size*5);
        sb.append(size);
        sb.append(": [");
        for(int i=0;i<size;i++) {
            if(i>0) sb.append(", ");
            sb.append(nums[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    // test
    public static void main(String[] args) {
        IntList a = new IntList(10);
        System.out.println(a);
        a.add(1); a.add(2); a.add(4); a.add(5);
        System.out.println(a);
        a.remove(1);
        System.out.println(a);
        System.out.println("a[2] = " + a.get(2));
    }

}
