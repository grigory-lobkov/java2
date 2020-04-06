package ru.progwards.java2.lessons.trees;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class TreeCompare<K, V> {
    ArrayList<Map<K, V>> maps;
    SplittableRandom r = new SplittableRandom();
    int gi;
    boolean[] gflag;
    List<Entry> gentries;
    
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
        tc.add(new AvlTree());
        for (int i = 0; i < 3; i++)
            tc.testRandom(10000);
        for (int i = 0; i < 3; i++)
            tc.testSorted(10000);
        for (int i = 0; i < 3; i++)
            tc.testTokens();
    }

    public void wait1() {
        try {
            Thread.sleep(10);
            Thread.sleep(10);
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void testRandom(int count) {
        System.out.println("testRandom("+count+")");
        System.out.print("PUT");
        wait1();
        for (Map<K, V> map : maps) {
            long spend = doPut(map, () -> new Entry(r.nextInt(), ""), count);
            System.out.print(" " + map.getClass().getSimpleName() + " " + spend / 1000);
        }
        System.out.print("\n");


        System.out.print("DEL");
        for (Map<K, V> map : maps) { // prepare to delete
            gi = 0;
            doPut(map, () -> new Entry(gi++, ""), count*10);
        }
        wait1();
        for (Map<K, V> map : maps) {
            gflag = new boolean[count * 10];
            long spend = doDelete(map, () -> {
                int t = r.nextInt(count*10);
                while (gflag[t])
                    t = r.nextInt(count*10);
                gflag[t] = true;
                return new Entry(t, "");
            }, count);
            System.out.print(" " + map.getClass().getSimpleName() + " " + spend / 1000);
        }
        System.out.print("\n");


        System.out.print("FND");
        for (Map<K, V> map : maps) { // prepare to search
            gi = 0;
            doPut(map, () -> new Entry(gi++, ""), count);
        }
        wait1();
        for (Map<K, V> map : maps) {
            long spend = doFind(map, () -> new Entry(r.nextInt(count), ""), count);
            System.out.print(" " + map.getClass().getSimpleName() + " " + spend / 1000);
        }
        System.out.print("\n");
    }

    private void testSorted(int count) {
        System.out.println("testSorted("+count+")");
        System.out.print("PUT");
        wait1();
        for (Map<K, V> map : maps) {
            gi =0;
            long spend = doPut(map, () -> new Entry(gi++, ""), count);
            System.out.print(" " + map.getClass().getSimpleName() + " " + spend / 1000);
        }
        System.out.print("\n");


        System.out.print("DEL");
        for (Map<K, V> map : maps) { // prepare to delete
            gi = 0;
            doPut(map, () -> new Entry(gi++, ""), count);
        }
        wait1();
        for (Map<K, V> map : maps) {
            gi = 0;
            long spend = doDelete(map, () -> new Entry(gi++, ""), count);
            System.out.print(" " + map.getClass().getSimpleName() + " " + spend / 1000);
        }
        System.out.print("\n");


        System.out.print("FND");
        for (Map<K, V> map : maps) { // prepare to search
            gi = 0;
            doPut(map, () -> new Entry(gi++, ""), count);
        }
        wait1();
        for (Map<K, V> map : maps) {
            gi = 0;
            long spend = doFind(map, () -> new Entry(gi++, ""), count);
            System.out.print(" " + map.getClass().getSimpleName() + " " + spend / 1000);
        }
        System.out.print("\n");
    }


    private void testTokens() {
        System.out.println("testTokens()");
        //gentries;
        // read file
        int count=0;
        gentries = new ArrayList<Entry>(1000);
        Path path = Paths.get("src/ru/progwards/java2/lessons/trees/wiki.train.tokens");
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                int k = line.indexOf(' ');
                if(k>0) {
                    Integer t = Integer.valueOf(line.substring(0,k));
                    gentries.add(new Entry(Integer.valueOf(line.substring(0,k)),line.substring(k+1)));
                    count++;
                } else {
                    Integer t = Integer.valueOf(line);
                    gentries.add(new Entry(t,""));
                    count++;
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        }
        System.out.print("PUT");
        wait1();
        for (Map<K, V> map : maps) {
            gi=0;
            long spend = doPut(map, () ->gentries.get(gi++), count);
            System.out.print(" " + map.getClass().getSimpleName() + " " + spend / 1000);
        }
        System.out.print("\n");


        System.out.print("DEL");
        for (Map<K, V> map : maps) { // prepare to delete
            gi = 0;
            doPut(map, () -> gentries.get(gi++), count);
        }
        wait1();
        for (Map<K, V> map : maps) {
            gi = 0;
            long spend = doDelete(map, () -> gentries.get(gi++), count);
            System.out.print(" " + map.getClass().getSimpleName() + " " + spend / 1000);
        }
        System.out.print("\n");


        System.out.print("FND");
        for (Map<K, V> map : maps) { // prepare to search
            gi = 0;
            doPut(map, () -> gentries.get(gi++), count);
        }
        wait1();
        for (Map<K, V> map : maps) {
            gi = 0;
            long spend = doFind(map, () -> gentries.get(gi++), count);
            System.out.print(" " + map.getClass().getSimpleName() + " " + spend / 1000);
        }
        System.out.print("\n");
    }

    private long doPut(Map<K, V> map, Supplier<Entry> supplier, int count) {
        Stream<Entry> stream = Stream.generate(supplier).limit(count);
        long timer = System.nanoTime();
        stream.forEach(e->map.put(e.key,e.value));
        return System.nanoTime() - timer;
    }

    private long doDelete(Map<K, V> map, Supplier<Entry> supplier, int count) {
        Stream<Entry> stream = Stream.generate(supplier).limit(count);
        long timer = System.nanoTime();
        stream.forEach(e->map.remove(e.key));
        return System.nanoTime() - timer;
    }

    private long doFind(Map<K, V> map, Supplier<Entry> supplier, int count) {
        Stream<Entry> stream = Stream.generate(supplier).limit(count);
        long timer = System.nanoTime();
        stream.forEach(e->map.get(e.key));
        return System.nanoTime() - timer;
    }

    private void add(Map map) {
        maps.add(map);
    }
}

/*

testRandom(10000)
PUT TreeMap 12362 AvlTree 16414
DEL TreeMap 29400 AvlTree 15430
FND TreeMap 13164 AvlTree 24783
testRandom(10000)
PUT TreeMap 4038 AvlTree 4399
DEL TreeMap 8354 AvlTree 13974
FND TreeMap 3168 AvlTree 2426
testRandom(10000)
PUT TreeMap 3004 AvlTree 5500
DEL TreeMap 8276 AvlTree 19277
FND TreeMap 11943 AvlTree 5041
testSorted(10000)
PUT TreeMap 6931 AvlTree 12522
DEL TreeMap 6298 AvlTree 4477
FND TreeMap 1462 AvlTree 1217
testSorted(10000)
PUT TreeMap 990 AvlTree 3427
DEL TreeMap 909 AvlTree 3346
FND TreeMap 1103 AvlTree 1160
testSorted(10000)
PUT TreeMap 2936 AvlTree 11856
DEL TreeMap 3687 AvlTree 14655
FND TreeMap 1752 AvlTree 1737
testTokens()
PUT TreeMap 76 AvlTree 107
DEL TreeMap 102 AvlTree 116
FND TreeMap 49 AvlTree 47
testTokens()
PUT TreeMap 55 AvlTree 109
DEL TreeMap 128 AvlTree 116
FND TreeMap 73 AvlTree 73
testTokens()
PUT TreeMap 50 AvlTree 96
DEL TreeMap 71 AvlTree 56
FND TreeMap 74 AvlTree 41

 */