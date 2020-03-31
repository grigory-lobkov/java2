
package ru.progwards.java2.lessons.gc.test;

//import com.google.inject.internal.cglib.core.$CollectionUtils;
import ru.progwards.java2.lessons.gc.Heap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class HeapTest {
    static final int maxSize = 1000000000;
    static final int maxSmall = 10;
    static final int maxMedium = 100;
    static final int maxBig = 1000;
    static final int maxHuge = 10000;
    static int allocated = 0;

    static class Block {
        public int ptr;
        public int size;

        public Block(int ptr, int size) {
            this.ptr = ptr;
            this.size = size;
        }
    }

    static int getRandomSize() {
        int n = Math.abs(ThreadLocalRandom.current().nextInt()%10);
        int size = Math.abs(ThreadLocalRandom.current().nextInt());
        if (n < 6)
            size %= maxSmall;
        else if (n < 8)
            size %= maxMedium;
        else if (n < 9)
            size %= maxBig;
        else
            size %= maxHuge;
        if (size > maxSize-allocated)
            size = maxSize-allocated;
        return size;
    }

    public static void main(String[] args) {
        Heap heap = new Heap(maxSize);
        List<Block> blocks = new ArrayList<>();
        int count = 0;
        int allocTime = 0;
        int freeTime = 0;

        long start = System.currentTimeMillis();
        // alloc and free 30% random
        while ((maxSize - allocated) > maxSize/100000) {
            long lstart, lstop;
            int size = getRandomSize()+1;
            allocated += size;
            count++;
            lstart = System.currentTimeMillis();

            int ptr = heap.malloc(size);

            lstop = System.currentTimeMillis();
            allocTime += lstop-lstart;
            blocks.add(new Block(ptr, size));
            int n = Math.abs(ThreadLocalRandom.current().nextInt()%5);
            if (n == 0) {
                n = Math.abs(ThreadLocalRandom.current().nextInt()%blocks.size());
                Block block = blocks.get(n);
                lstart = System.currentTimeMillis();

                heap.free(block.ptr);

                lstop = System.currentTimeMillis();
                freeTime += lstop-lstart;
                allocated -= block.size;
                blocks.remove(n);
            }
            n = Math.abs(ThreadLocalRandom.current().nextInt()%100000);
            if (n==0)
                System.out.println(maxSize-allocated);
        }
        long stop = System.currentTimeMillis();
        System.out.println(maxSize-allocated);
        System.out.println("malloc time: "+allocTime+" free time: "+freeTime);
        System.out.println("total time: "+(stop-start)+" count: "+count);
    }
}
