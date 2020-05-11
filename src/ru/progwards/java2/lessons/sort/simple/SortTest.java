package ru.progwards.java2.lessons.sort.simple;

import ru.progwards.java2.lessons.gc.BiHeap;
import ru.progwards.java2.lessons.trees.BinaryTree;
import ru.progwards.java2.lessons.trees.TreeException;


import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class SortTest {
    static final int COUNT = 100_000;

    static void fill(Integer[] a) {
        HashSet<Integer> set = new HashSet<>();
        for (int i = 0; i < a.length; i++) {
            int n;
            do {
                n = ThreadLocalRandom.current().nextInt();
            } while (set.contains(n));
            a[i] = n;
            set.add(n);
        }
    }

    static void fill2(Integer[] a) {
        Arrays.fill(a, 1);
    }

    static Integer[] copy(Integer[] src) {
        Integer[] res = new Integer[src.length];
        for (int i = 0; i < src.length; i++)
            res[i] = src[i];
        return res;
    }

    static void selection(Integer[] org) {
        Integer[] a = copy(org);
        long start, sort;
//        start = System.currentTimeMillis();
//        SelectionSort.sort0(a);
//        sort = System.currentTimeMillis()-start;
//        System.out.println("selection sort0(" + a.length + "): "+sort);

//        a = copy(org);
//        start = System.currentTimeMillis();
//        SelectionSort.sort2(a);
//        sort = System.currentTimeMillis()-start;
//        System.out.println("selection sort2(" + a.length + "): "+sort);

//        a = copy(org);
//        start = System.currentTimeMillis();
//        SelectionSort.sort3(a);
//        sort = System.currentTimeMillis()-start;
//        System.out.println("selection sort3(" + a.length + "): "+sort);

        start = System.currentTimeMillis();
        SelectionSort.sort(a);
        sort = System.currentTimeMillis() - start;
        System.out.println("selection sort(" + a.length + "): " + sort);
    }

    static void heap(Integer[] org) {
        Integer[] a = copy(org);

        long start = System.currentTimeMillis();
        //BinaryHeap<Integer> heap = BinaryHeap.from(BinaryHeap.Type.MIN_HEAP, a);
        //heap.sort(a);
        BiHeap<Integer> heap = new BiHeap();
        for (Integer e : a)
            heap.insert(e);
        Integer[] result = new Integer[a.length];
        int i = a.length - 1;
        while (!heap.isEmpty()) {
            result[i--] = heap.max();
            heap.delete();
        }
        long sort = System.currentTimeMillis() - start;
        System.out.println("heap sort(" + a.length + "): " + sort);
    }

    static void tree(Integer[] org) throws TreeException {
        long start, sort;

        start = System.currentTimeMillis();
        ArrayList<BinaryTree.TreeLeaf> sorted = new ArrayList<>(COUNT);
        BinaryTree<Integer, Integer> tree = new BinaryTree<>();
        for (Integer n : org)
            tree.add(n, n);
        tree.process(sorted::add);
        sort = System.currentTimeMillis() - start;
        System.out.println("tree sort(" + org.length + "): " + sort);

//        start = System.currentTimeMillis();
//        ArrayList<Integer> sorted2 = new ArrayList<>(COUNT);
//        TreeMap<Integer, Integer> tree2 = new TreeMap<>();
//        for(Integer n: org)
//            tree2.put(n, n);
//        sorted2.addAll(tree2.keySet());
//        sort = System.currentTimeMillis()-start;
//        System.out.println("tree2 sort(" + a.length + "): "+sort);
    }

    static void bubble(Integer[] org) {
        Integer[] a = copy(org);
        long start = System.currentTimeMillis();
        BubbleSort.sort(a);
        long sort = System.currentTimeMillis() - start;

        System.out.println("bubble sort(" + a.length + "): " + sort);
    }

    static void shaker(Integer[] org) {
        Integer[] a;
        long start, sort;

//        a = copy(org);
//        start = System.currentTimeMillis();
//        ShakerSort.sort1(a);
//        sort = System.currentTimeMillis() - start;
//        System.out.println("shaker sort1(" + a.length + "): " + sort);

        a = copy(org);
        start = System.currentTimeMillis();
        ShakerSort.sort(a);
        sort = System.currentTimeMillis() - start;
        System.out.println("shaker sort(" + a.length + "): " + sort);
    }

    static void comb(Integer[] org) {
        Integer[] a;
        long start, sort;

        a = copy(org);
        start = System.currentTimeMillis();
        CombSort.sort(a);
        sort = System.currentTimeMillis() - start;
        System.out.println("comb sort(" + a.length + "): " + sort);
    }

    static void insertion(Integer[] org) {
        Integer[] a;
        long start, sort;
        a = copy(org);
        start = System.currentTimeMillis();
        InsertionSort.sort1(a);
        sort = System.currentTimeMillis() - start;
        System.out.println("insertion sort1(" + a.length + "): " + sort);

//        a = copy(org);
//        start = System.currentTimeMillis();
//        InsertionSort.sort2(a);
//        sort = System.currentTimeMillis()-start;
//        System.out.println("insertion sort2(" + a.length + "): "+sort);

//        a = copy(org);
//        start = System.currentTimeMillis();
//        InsertionSort.sort3(a);
//        sort = System.currentTimeMillis()-start;
//        System.out.println("insertion sort3(" + a.length + "): "+sort);

        a = copy(org);
        start = System.currentTimeMillis();
        InsertionSort.sort(a);
        sort = System.currentTimeMillis() - start;
        System.out.println("insertion sort(" + a.length + "): " + sort);
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
        long sort2 = System.currentTimeMillis() - start;
        System.out.println("quick sort2(" + a.length + "): " + sort2);
        //System.out.println(Arrays.toString(a));
    }

    static void shell(Integer[] org) {
        Integer[] a = copy(org);
        long start = System.currentTimeMillis();
        ShellSort.sort(a);
        long sort = System.currentTimeMillis() - start;
        System.out.println("shell sort(" + a.length + "): " + sort);
        //System.out.println(Arrays.toString(a));
    }

    static void arrays(Integer[] org) {
        Integer[] a = copy(org);
        long start = System.currentTimeMillis();

        Arrays.sort(a);
        long sort = System.currentTimeMillis() - start;
        System.out.println("arrays sort(" + a.length + "): " + sort);
    }

    public static void main(String[] args) throws TreeException {
        Integer[] org1 = new Integer[COUNT];
        fill(org1);
        Integer[] org10 = new Integer[COUNT * 10];
        fill(org10);
        Integer[] org100 = new Integer[COUNT * 100];
        fill(org100);
        System.out.println("Preparation done.");

        bubble(org1); //46740,  COUNT = 100_000;
        //bubble(org1); //32841,  COUNT = 100_000;
        selection(org1); //13020,  COUNT = 100_000;
        shaker(org1); //35105, COUNT = 100_000;

        insertion(org1); //2633, COUNT = 100_000;
        insertion(org10); //290990, COUNT = 1_000_000;

        heap(org10);  //316,  COUNT = 1_000_000;
        heap(org100); //3206, COUNT = 10_000_000;
        comb(org10);  //1301, COUNT = 1_000_000;
        comb(org100); //19525,COUNT = 10_000_000;
        tree(org10);  //1149, COUNT = 1_000_000;
        tree(org100); //25165,COUNT = 10_000_000;

        quick(org10);   //471,  COUNT = 1_000_000;
        quick(org100);  //4762, COUNT = 10_000_000;
        shell(org10);   //1518, COUNT = 1_000_000;
        shell(org100);  //65223,COUNT = 10_000_000;
        arrays(org10);  //617,  COUNT = 1_000_000;
        arrays(org100); //5755, COUNT = 10_000_000;
    }

}