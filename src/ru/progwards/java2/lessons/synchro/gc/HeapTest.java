package ru.progwards.java2.lessons.synchro.gc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class HeapTest {

    public static void getBytes(int ptr, byte[] bytes) {
        for (MakeTest t:runnables)
            t.heapTest.getBytes(ptr, bytes);
    }

    public static void setBytes(int ptr, byte[] bytes) {
        for (MakeTest t:runnables)
            t.heapTest.setBytes(ptr, bytes);
    }

    final static int maxSize = 1_000_000_000;
    final static int threadsCount = 4;
    final static int threadMaxSize = maxSize / threadsCount;

    final static Thread[] threads = new Thread[threadsCount];
    final static MakeTest[] runnables = new MakeTest[threadsCount];

    static class MakeTest implements Runnable {
        HeapTest1 heapTest;
        HeapInterface heap;

        public MakeTest(HeapTest1 heapTest, HeapInterface heap) {
            this.heapTest = heapTest;
            this.heap = heap;
        }

        @Override
        public void run() {
            heapTest.mainTest(heap);
        }
    }

    public static void main(String[] args) throws InterruptedException {

        final HeapInterface heap = new Heap(maxSize); //Heap_initial_hashTable,Heap

        for(int i=0; i<threadsCount; i++) {
            runnables[i] = new MakeTest(new HeapTest1(threadMaxSize), heap);
            threads[i] = new Thread(runnables[i]);
            threads[i].start();
        }
        long start = System.currentTimeMillis();
        int maxSize=0;
        int allocated=0;
        int allocTime=0;
        int freeTime=0;
        int count = 0;
        for (int i=0; i<threadsCount; i++) {
            threads[i].join();
            HeapTest1 t = runnables[i].heapTest;
            maxSize+=t.maxSize;
            allocated+=t.allocated;
            allocTime+=t.allocTime;
            freeTime+=t.freeTime;
            count+=t.count;
        }
        long stop = System.currentTimeMillis();
        heap.dispose();
        System.out.println("\nfree memory: " + (maxSize - allocated));
        System.out.println("malloc time: " + (allocTime) + " free time: " + freeTime);
        System.out.println("total time: " + (allocTime + freeTime) + " count: " + count);
        System.out.println("passed time: " + (stop - start));
    }
}

