package ru.progwards.java2.lessons.trees;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

/*
Написать тест, сравнивающий производительность AvlTree и TreeMap отдельно на операциях вставки,
удаления и поиска на случайных (random) и сортированных данных.
А так же на данных из файла wiki.train.tokens, который надо разбить на слова по разделителям
*/
public class TreeTest {
    static final int ITERATIONS = 1000;
    public static void main(String[] args) throws TreeException {
        TreeMap<Integer, Integer> map = new TreeMap<>();
        BinaryTree<Integer, String> tree = new BinaryTree<>();
        for(int i=0; i<ITERATIONS; i++) {
            int key = ThreadLocalRandom.current().nextInt();
            if (!map.containsKey(key)) {
                map.put(key, key);
                tree.add(key, "key=" + key);
            }
        }
        System.out.println("put passed OK");
        //tree.process(System.out::println);
        ArrayList<BinaryTree.TreeLeaf> sorted = new ArrayList<>();
        tree.process(sorted::add);
        for(BinaryTree.TreeLeaf leaf: sorted) {
            System.out.println(leaf.toString());
        }

        for(Integer i:map.keySet()) {
            tree.find(i);
            tree.delete(i);
        }
        System.out.println("find&delete passed OK");
    }
}
