package ru.progwards.java2.lessons.trees;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TreeCompare<K, V> {
    ArrayList<Map<K, V>> maps;
    SplittableRandom r = new SplittableRandom();
    
    class Entry {
        K key;
        V value;
        public Entry(Object key, Object value) {
            this.key = (K)key;
            this.value = (V)value;
        }
    }

    public TreeCompare() {
        this.maps = new ArrayList<>();
    }

    public static void main(String[] args) {
        TreeCompare tc = new TreeCompare<Integer, String>();
        tc.add(new TreeMap());
        tc.add(new AvlTree2());
        tc.testRandom(1000);
    }

    private void testRandom(int count) {
        System.out.print("testRandom: ");
        for (Map<K, V> map:maps) {
            long spend = doPut(map, ()->new Entry(r.nextInt(),""), count);
            System.out.print(" "+spend/1000);
        }
        System.out.println("\n");
    }
    private long doPut(Map<K, V> map, Supplier<Entry> supplier, int count) {
        Stream<Entry> stream = Stream.generate(supplier).limit(count);
        long timer = System.nanoTime();
        stream.forEach(e->map.put(e.key,e.value));
        return System.nanoTime() - timer;
    }
    private void add(Map map) {
        maps.add(map);
    }
}
