package ru.progwards.java2.lessons.gc.test;

import ru.progwards.java2.lessons.gc.Heap;
import ru.progwards.java2.lessons.gc.InvalidPointerException;
import ru.progwards.java2.lessons.gc.OutOfMemoryException;

public class StressTester {

    static final int HEAP_SIZE=1*1024*1024;
    static Heap heap;

    void fillAndFree(int wordSize) throws OutOfMemoryException, InvalidPointerException {
        fill(wordSize, 1);
        free(wordSize, 1);
    }

    void fill(int wordSize, int step) throws OutOfMemoryException {
        fill(wordSize, step, 0, HEAP_SIZE);
    }

    void free(int wordSize, int step) throws InvalidPointerException {
        free(wordSize, step, 0, HEAP_SIZE);
    }

    void fill(int wordSize, int step, int fromByte, int toByte) throws OutOfMemoryException {
        int pos = fromByte;
        int to=toByte-wordSize;
        int stepPos = wordSize*step;
        while(pos<=to) {
            heap.malloc(wordSize);
            pos+=stepPos;
        }
    }

    void free(int wordSize, int step, int fromByte, int toByte) throws InvalidPointerException {
        int pos = fromByte;
        int to=toByte-wordSize;
        int stepPos = wordSize*step;
        while(pos<=to) {
            heap.free(pos,wordSize);
            pos+=stepPos;
        }
    }

    long startMem;
    long stepMem;
    long spentTime;
    long stepTime;
    long serviceTime;

    private static long SLEEP_INTERVAL = 100;

    private long getMemoryUse(){
        collectGarbage();
        collectGarbage();
        long totalMemory = Runtime.getRuntime().totalMemory();
        collectGarbage();
        collectGarbage();
        long freeMemory = Runtime.getRuntime().freeMemory();
        return (totalMemory - freeMemory);
    }
    private void collectGarbage() {
        try {
            System.gc();
            Thread.currentThread().sleep(SLEEP_INTERVAL);
            System.runFinalization();
            Thread.currentThread().sleep(SLEEP_INTERVAL);
        }
        catch (InterruptedException ex){
            ex.printStackTrace();
        }
    }

    void statStart(String name){
        startMem = getMemoryUse();
        stepMem = startMem;
        spentTime = 0;
        serviceTime=0;
        System.out.println("\nStatistic "+name+" started.");
        stepTime = System.nanoTime();
    }

    void statStep(String name){
        long time=System.nanoTime();
        long mem=getMemoryUse();
        long spentStep = time-stepTime;
        spentTime+=spentStep;
        String step=name+": "+" ".repeat(Math.max(7-name.length(),0))+(mem-stepMem)/1024+" kb "+spentStep/1000+" mcs";
        System.out.println(step+" ".repeat(Math.max(30-step.length(),0))+" Total: "+mem/1024+" kb "+spentTime/1000+" mcs");
        stepMem = mem;
        stepTime = System.nanoTime();
        serviceTime = stepTime-time;
    }

    void statEnd(String name){
        System.out.println(name+" (finished), serviceTime "+serviceTime/1000+" mcs");
    }

    void testFullFill(int wordSize) throws Exception {
        statStart("testFullFill(" + wordSize + ")");
        heap = new Heap(HEAP_SIZE);
        statStep("Create");
        fill(wordSize, 1);
        statStep("Fill");
        free(wordSize, 1);
        statStep("Free");
        heap.defrag();
        statStep("Empty defrag");
        heap.compact();
        statStep("Empty compact");
        fill(wordSize, 1);
        statStep("Fill");
        for (int i = 1; i < 4; i++) free(wordSize, 4, i*wordSize, HEAP_SIZE);
        statStep("Free(3/4)");
        heap.defrag();
        statStep("Defrag");
        heap.compact();
        statStep("Compact");
        statEnd("testFullFill(" + wordSize + ")");
    }


    void testHalfFill(int wordSize) throws Exception {
        statStart("testHalfFill(" + wordSize + ")");
        heap = new Heap(HEAP_SIZE);
        int half = HEAP_SIZE;
        fill(wordSize, 1, 0, half); // наплняем до половины кучу
        free(wordSize, 2, 0, half); // чистим каждый воторой заполненный, больше трогать не будем
        statStep("Create 1/2");
        fill(wordSize, 1, half, HEAP_SIZE);
        free(wordSize, 1, half, HEAP_SIZE);
        statStep("Fragment 2/2");
        heap.defrag();
        statStep("Defrag");
        heap.compact();
        statStep("Compact");
        heap = new Heap(HEAP_SIZE);
        fill(wordSize, 1, 0, half); // наплняем до половины кучу
        free(wordSize, 2, 0, half); // чистим каждый воторой заполненный, больше трогать не будем
        statStep("Create 1/2");
        fill(wordSize, 1, half, HEAP_SIZE);
        free(wordSize, 1, half, HEAP_SIZE);
        statStep("Fragment 2/2");
        heap.compact();
        statStep("Compact");
        statEnd("testHalfFill(" + wordSize + ")");
    }

    public static void main(String[] args) throws Exception {
        StressTester t = new StressTester();

        // тесты с целиковой кучей
        t.testFullFill(32);
        t.testFullFill(16);
        t.testFullFill(8);

        // тесты с первой половиной заполненной и разряженной
        t.testHalfFill(64);
        t.testHalfFill(32);
        t.testHalfFill(16);
    }

}
