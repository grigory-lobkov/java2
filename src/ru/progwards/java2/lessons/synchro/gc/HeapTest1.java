package ru.progwards.java2.lessons.synchro.gc;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class HeapTest1 {

    int maxSize;
    final int maxSmall = 10;
    final int maxMedium = 100;
    final int maxBig = 1000;
    final int maxHuge = 10000;
    int allocated = 0;
    List<Block> blocks = new ArrayList<>(10_000);
    Hashtable<Integer, Block> hashtable = new Hashtable<>(10_000);
    int allocTime;
    int freeTime;
    int count;

    //Lock lock = new ReentrantLock();

    class Block {
        public int ptr;
        public int size;
        public boolean removed = false;

        public Block(int ptr, int size) {
            this.ptr = ptr;
            this.size = size;
        }
    }

    public HeapTest1(int maxSize) {
        this.maxSize = maxSize;
    }

    int getRandomSize() {
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

    public long mainTest(HeapInterface heap) {
        try {
            //Heap.initLog("heap.log.0");
            count = 0;
            allocTime = 0;
            freeTime = 0;
            int ptr;

            long start = System.currentTimeMillis();
            // alloc and free 30% random
            while (maxSize != allocated) { //100_000
                //while (maxSize != allocated) {
                if (blocks.size() >= 10_000) {//1_000
                    //lock.lock();
                    blocks.clear();
                    hashtable.clear();
                    //lock.unlock();
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
                //lock.lock();
                allocTime += lstop - lstart;
                Block block = new Block(ptr, size);
                blocks.add(block);
                hashtable.put(ptr, block);
                int n = Math.abs(ThreadLocalRandom.current().nextInt() % 2);
                if (n == 0 && blocks.size() > 0) {
                    n = Math.abs(ThreadLocalRandom.current().nextInt() % blocks.size());
                    block = blocks.get(n);
                    //lock.unlock();
                    if(!block.removed) {
                        block.removed = true;
                        lstart = System.currentTimeMillis();
                        heap.free(block.ptr);
                        lstop = System.currentTimeMillis();
                        freeTime += lstop - lstart;
                        allocated -= block.size;
                    }
                //} else { lock.unlock();
                }
                //if (Math.abs(ThreadLocalRandom.current().nextInt() % 100000) == 0) System.out.println((maxSize - allocated));
                if (count % 256_000 == 0)
                    System.out.println((maxSize - allocated));
            }
            long stop = System.currentTimeMillis();
            System.out.println(Thread.currentThread().getName()+"\nfree memory: " + (maxSize - allocated));
            System.out.println("malloc time: " + (allocTime) + " free time: " + freeTime);
            System.out.println("total time: " + (allocTime + freeTime) + " execsCount: " + count);
            return allocTime;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //Heap.closeLog();
        }
        return 0;
    }

    public void getBytes(int ptr, byte[] bytes) {
//        lock.lock();
//        long lstart = System.currentTimeMillis();
//        Iterator<Block> i = blocks.iterator();
//        while (i.hasNext())
//            if (i.next().ptr == ptr) i.remove();
//        allocTime -= (System.currentTimeMillis()-lstart);
//        lock.unlock();
        Block block = hashtable.get(ptr);
        if(block!=null)
            block.removed = true;
    }

    public void setBytes(int ptr, byte[] bytes) {
        //System.arraycopy(bytes, 0, this.bytes, ptr, size);
    }
}