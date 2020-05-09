package ru.progwards.java2.lessons.sort;

import ru.progwards.java2.lessons.gc.Heap;
import ru.progwards.java2.lessons.trees.BinaryTree;
import ru.progwards.java2.lessons.trees.TreeException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class SortTest {
    static final int COUNT = 1_000_000;

    static void fill(Integer[] a) {
        HashMap<Integer, Integer> map = new HashMap<>();
        for(int i=0; i < COUNT; i++) {
            int n;
            do {
                n = ThreadLocalRandom.current().nextInt();
            } while ( map.containsKey(n));
            a[i] = n;
            map.put(n,n);
        }
    }

    static void fill2(Integer[] a) {
        Arrays.fill(a, 1);
    }

    static Integer[] copy(Integer[] src) {
        Integer[] res = new Integer[src.length];
        for(int i=0; i < src.length; i++)
            res[i] = src[i];
        return res;
    }

    static void selection(Integer[] org) {
        Integer[] a = copy(org);
        long start, sort;
//        start = System.currentTimeMillis();
//        SelectionSort.sort0(a);
//        sort = System.currentTimeMillis()-start;
//        System.out.println("selection sort0: "+sort);

//        a = copy(org);
//        start = System.currentTimeMillis();
//        SelectionSort.sort2(a);
//        sort = System.currentTimeMillis()-start;
//        System.out.println("selection sort2: "+sort);

//        a = copy(org);
//        start = System.currentTimeMillis();
//        SelectionSort.sort3(a);
//        sort = System.currentTimeMillis()-start;
//        System.out.println("selection sort3: "+sort);

        start = System.currentTimeMillis();
        SelectionSort.sort(a);
        sort = System.currentTimeMillis()-start;
        System.out.println("selection sort: "+sort);
    }

    static void heap(Integer[] org) {
        Integer[] a = copy(org);

        long start = System.currentTimeMillis();
        //BinaryHeap<Integer> heap = BinaryHeap.from(BinaryHeap.Type.MIN_HEAP, a);
        //heap.sort(a);
        long sort = System.currentTimeMillis()-start;
        System.out.println("heap sort: "+sort);
    }

    static void tree(Integer[] org) throws TreeException {
        long start, sort;

        start = System.currentTimeMillis();
        ArrayList<BinaryTree.TreeLeaf> sorted = new ArrayList<>(COUNT);
        BinaryTree<Integer, Integer> tree = new BinaryTree<>();
        for(Integer n: org)
            tree.add(n, n);
        tree.process(sorted::add);
        sort = System.currentTimeMillis()-start;
        System.out.println("tree sort: "+sort);

//        start = System.currentTimeMillis();
//        ArrayList<Integer> sorted2 = new ArrayList<>(COUNT);
//        TreeMap<Integer, Integer> tree2 = new TreeMap<>();
//        for(Integer n: org)
//            tree2.put(n, n);
//        sorted2.addAll(tree2.keySet());
//        sort = System.currentTimeMillis()-start;
//        System.out.println("tree2 sort: "+sort);
    }

    static void bubble(Integer[] org) {
        Integer[] a = copy(org);
        long start = System.currentTimeMillis();
        BubbleSort.sort(a);
        long sort = System.currentTimeMillis()-start;

        System.out.println("bubble sort: "+sort);
    }

    static void shaker(Integer[] org) {
        Integer[] a = copy(org);
        long start = System.currentTimeMillis();
        ShakerSort.sort(a);
        long sort = System.currentTimeMillis()-start;

        System.out.println("shaker sort: "+sort);
    }

    static void comb(Integer[] org) {
        Integer[] a;
        long start, sort;

        a = copy(org);
        start = System.currentTimeMillis();
        CombSort.sort(a);
        sort = System.currentTimeMillis()-start;
        System.out.println("comb sort: "+sort);
    }

    static void insertion(Integer[] org) {
        Integer[] a;
        long start, sort;
//        a = copy(org);
//        start = System.currentTimeMillis();
//        InsertionSort.sort1(a);
//        sort = System.currentTimeMillis()-start;
//        System.out.println("insertion sort1: "+sort);

//        a = copy(org);
//        start = System.currentTimeMillis();
//        InsertionSort.sort2(a);
//        sort = System.currentTimeMillis()-start;
//        System.out.println("insertion sort2: "+sort);

//        a = copy(org);
//        start = System.currentTimeMillis();
//        InsertionSort.sort3(a);
//        sort = System.currentTimeMillis()-start;
//        System.out.println("insertion sort3: "+sort);

        a = copy(org);
        start = System.currentTimeMillis();
        InsertionSort.sort(a);
        sort = System.currentTimeMillis()-start;
        System.out.println("insertion sort: "+sort);
    }

    static void quick(Integer[] org) {
        Integer[] a = copy(org);
//        long start = System.currentTimeMillis();
//        QuickSort.sort(a);
//        long sort = System.currentTimeMillis()-start;
//
//        a = copy(org);
        long start = System.currentTimeMillis();
        QuickSort.sort2(a);
        long sort2 = System.currentTimeMillis()-start;
        System.out.println("quick sort2: "+sort2);
        //System.out.println(Arrays.toString(a));
    }

    static void shell(Integer[] org) {
        Integer[] a = copy(org);
        long start = System.currentTimeMillis();
        ShellSort.sort(a);
        long sort = System.currentTimeMillis()-start;
        System.out.println("shell sort: "+sort);
        //System.out.println(Arrays.toString(a));
    }

    static void arrays(Integer[] org) {
        Integer[] a = copy(org);
        long start = System.currentTimeMillis();

        Arrays.sort(a);
        long sort = System.currentTimeMillis()-start;
        System.out.println("arrays sort: "+sort);
    }

    public static void main(String[] args) throws TreeException {
        Integer[] org = new Integer[COUNT];
        fill(org);

        //bubble(org); //46740,  COUNT = 100_000;
        //bubble(org); //32841,  COUNT = 100_000;
        //selection(org); //13020,  COUNT = 100_000;
        //shaker(org); //35105, COUNT = 100_000;
        //insertion(org); //1833, COUNT = 100_000;

        //heap(org); //0, COUNT = 1_000_000;  // нет нужного класса
        comb(org); //1318, COUNT = 1_000_000;
        tree(org); //1028, COUNT = 1_000_000;

        quick(org); //343, COUNT = 1_000_000;
        shell(org); //1655, COUNT = 1_000_000;
        arrays(org); //682, COUNT = 1_000_000;
    }
}