package ru.progwards.java2.lessons.synchro.gc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class HeapTest_old {
    static final int maxSize = 1_000_000_000;//1_000_000_000
    static final int maxSmall = 10;
    static final int maxMedium = 100;
    static final int maxBig = 1000;
    static final int maxHuge = 10000;
    static int allocated = 0;
    static List<Block> blocks = new ArrayList<>();
    static int allocTime;
    static int freeTime;

    static Lock lock = new ReentrantLock();

    static class Block {
        public int ptr;
        public int size;

        public Block(int ptr, int size) {
            this.ptr = ptr;
            this.size = size;
        }
    }

    static int getRandomSize() {
        int n = Math.abs(ThreadLocalRandom.current().nextInt() % 10);
        int size = Math.abs(ThreadLocalRandom.current().nextInt());
        if (n < 6)
            size %= maxSmall;
        else if (n < 8)
            size %= maxMedium;
        else if (n < 9)
            size %= maxBig;
        else
            size %= maxHuge;
        if (size > maxSize - allocated)
            size = maxSize - allocated;
        return size;
    }

    public static long mainTest(boolean reinit) {
        try {
            //Heap.initLog("heap.log.0");
            var heap = new Heap(maxSize);
            int count = 0;
            allocTime = 0;
            freeTime = 0;
            int ptr;

            long start = System.currentTimeMillis();
            // alloc and free 30% random
            while ((maxSize - allocated) > maxSize / 1_000_000) { //100_000
                //while (maxSize != allocated) {
                if (reinit && blocks.size() >= 10_000) {//1_000
                    lock.lock();
                    blocks.clear();
                    lock.unlock();
                }
                long lstart, lstop;
                int size = getRandomSize() + 1;
                if (size > maxSize - allocated) {
                    size = size / 4;
                    if (size < 1 || size > maxSize - allocated)
                        size = maxSize - allocated;
                }
                count++;
                lstart = System.currentTimeMillis();
                ptr = heap.malloc(size);
                lstop = System.currentTimeMillis();
                allocated += size;
                lock.lock();
                allocTime += lstop - lstart;
                blocks.add(new Block(ptr, size));
                int n = Math.abs(ThreadLocalRandom.current().nextInt() % 2);
                if (n == 0 && blocks.size() > 0) {
                    n = Math.abs(ThreadLocalRandom.current().nextInt() % blocks.size());
                    Block block = blocks.get(n);
                    blocks.remove(n);
                    lock.unlock();
                    lstart = System.currentTimeMillis();
                    heap.free(block.ptr);
                    lstop = System.currentTimeMillis();
                    freeTime += lstop - lstart;
                    allocated -= block.size;
                } else {
                    lock.unlock();
                }
                //if (Math.abs(ThreadLocalRandom.current().nextInt() % 100000) == 0) System.out.println((maxSize - allocated));
                if (count % 256_000 == 0)
                    System.out.println((maxSize - allocated));
            }
            long stop = System.currentTimeMillis();
            System.out.println("\nfree memory: " + (maxSize - allocated));
            System.out.println("malloc time: " + (allocTime) + " free time: " + freeTime);
            System.out.println("total time: " + (allocTime + freeTime) + " count: " + count);
            return allocTime;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //Heap.closeLog();
        }
        return 0;
    }

    public static void getBytes(int ptr, byte[] bytes) {
        lock.lock();
        long lstart = System.currentTimeMillis();
        Iterator<Block> i = blocks.iterator();
        while (i.hasNext())
            if (i.next().ptr == ptr) i.remove();
        allocTime -= (System.currentTimeMillis()-lstart);
        lock.unlock();
    }

    public static void setBytes(int ptr, byte[] bytes) {
        //System.arraycopy(bytes, 0, this.bytes, ptr, size);
    }

    public static void main(String[] args) throws InvalidPointerException {
//        long time = 0;
//        for (int i=0; i<10; i++)
//            time += mainTest();
//        System.out.println("******* result="+(time/10));
        mainTest(true);
    }
}