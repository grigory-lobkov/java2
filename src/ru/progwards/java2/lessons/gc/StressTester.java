package ru.progwards.java2.lessons.gc;

public class StressTester {

    static final int HEAP_SIZE=1024*1024;
    static Heap heap;

    void fillAndFree(int wordSize) throws OutOfMemoryException, InvalidPointerException {
        fill(wordSize, 1);
        free(wordSize, 0,1);
    }

    void fill(int wordSize, int step) throws OutOfMemoryException {
        int pos = 0;
        int to=HEAP_SIZE-wordSize;
        int stepPos = wordSize*step;
        while(pos<=to) {
            heap.malloc(wordSize);
            pos+=stepPos;
        }
    }

    void free(int wordSize, int offset, int step) throws InvalidPointerException {
        int pos = wordSize*offset;
        int to=HEAP_SIZE-wordSize;
        int stepPos = wordSize*step;
        while(pos<=to) {
            heap.free(pos,wordSize);
            pos+=stepPos;
        }
    }

    long startMem;
    long stepMem;
    long startTime;
    long stepTime;

    void statStart(String name){
        //System.gc();
        System.out.println("\nStatistic "+name+" started.");
        startMem = Runtime.getRuntime().freeMemory();
        stepMem = startMem;
        startTime = System.currentTimeMillis();
        stepTime = startTime;
    }

    void statStep(String name){
        long mem=Runtime.getRuntime().freeMemory();
        long time=System.currentTimeMillis();
        String step=name+": "+" ".repeat(Math.max(7-name.length(),0))+(stepMem-mem)/1024+" kb "+(time-stepTime)+" ms";
        System.out.println(step+" ".repeat(Math.max(30-step.length(),0))+" Total: "+(startMem-mem)/1024+" kb "+(time-startTime)+" ms");
        stepMem = mem;
        stepTime = time;
    }

    void statEnd(String name){
        statStep(name+" (finished)");
    }

    void testFullFill(int wordSize) throws Exception {
        statStart("testFullFill(wordSize="+wordSize+")");
        heap = new Heap(HEAP_SIZE);
        statStep("Create");
        fill(wordSize,1);
        statStep("Fill");
        free(wordSize,0,1);
        statStep("Free");
        heap.defrag();
        statStep("Empty defrag");
        heap.compact();
        statStep("Empty compact");
        fill(wordSize,1);
        statStep("Fill");
        for(int i=1; i<4; i++) free(wordSize,i,4);
        statStep("Free(3/4)");
        heap.defrag();
        statStep("Defrag");
        heap.compact();
        statStep("Compact");
    }


    public static void main(String[] args) throws Exception {
        //heap = new Heap(HEAP_SIZE);
        /*fillAndFree(4);
        fillAndFree(6);
        heap.malloc(4);
        heap.malloc(4);
        heap.malloc(4);
        System.out.println();
        System.out.println("Empties: "+heap.empties);
        System.out.println("Objects: "+heap.objects);
        System.out.println();
        heap.malloc(4);
        System.out.println();
        System.out.println("Empties: "+heap.empties);
        System.out.println("Objects: "+heap.objects);*/

        StressTester t = new StressTester();
        t.testFullFill(64);
        t.testFullFill(32);
        t.testFullFill(16);
        t.testFullFill(8);
        t.testFullFill(4);
    }

}
