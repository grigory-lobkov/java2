package ru.progwards.java2.lessons.synchro.gc;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Heap {

    class MBlock {
        int ptr;
        int size;

        public MBlock(int ptr, int size) {
            this.ptr = ptr;
            this.size = size;
        }
    }

    byte[] memory;
    HashMap<Integer, MBlock> objectsMapByPtr; // список объектов по адресам, ключ = адрес
    TreeMap<Integer, ArrayDeque<MBlock>> emptiesTreeBySize; // поиск пустых блоков по размеру
    HashMap<Integer, MBlock> emptiesMapByPtr; // список пустых по адресу
    final int averageObjectSize = 64; // средний размер объекта (для рассчета общего количества)

    //final HeapService heapService = new HeapService();
    //final Thread thread = new Thread(heapService);

    Heap(int maxHeapSize) {
        memory = new byte[maxHeapSize];
        int expectedObjectsCount = maxHeapSize / averageObjectSize;
        int expectedEmptiesCount = expectedObjectsCount / 10;

        objectsMapByPtr = new HashMap<>(expectedObjectsCount);
        emptiesTreeBySize = new TreeMap<>();
        emptiesMapByPtr = new HashMap<>(expectedEmptiesCount);

        MBlock emptyBlock = new MBlock(0, maxHeapSize);
        ArrayDeque<MBlock> emptyBlockArray = new ArrayDeque<>();
        emptyBlockArray.add(emptyBlock);
        emptiesTreeBySize.put(maxHeapSize, emptyBlockArray);
        emptiesMapByPtr.put(0, emptyBlock);

        //thread.start();
    }

    public int malloc(int size) throws OutOfMemoryException {
        Map.Entry<Integer, ArrayDeque<MBlock>> found;

        found = emptiesTreeBySize.ceilingEntry(size);
        if (found == null) {

            defrag();

            found = emptiesTreeBySize.ceilingEntry(size);
            if (found == null) {

                compact();

                found = emptiesTreeBySize.ceilingEntry(size);
                if (found == null) throw new OutOfMemoryException("Cannot malloc " + size + " bytes of memory.");
            }
        }

        int foundSize = found.getKey();
        if (foundSize == size) {
            MBlock foundEmpty = pollEmpty(found.getValue());
            MBlock newObject = newObject(foundEmpty.ptr, size);
            return newObject.ptr;
        } else {
            int ptr = shrinkEmpty(found.getValue(), size);
            MBlock newObject = newObject(ptr, size);
            return ptr;
        }
    }

    private int shrinkEmpty(ArrayDeque<MBlock> empties, int takeSize) {
        MBlock block = empties.pollFirst();
        int result = block.ptr;
        if (empties.size() == 0) {
            emptiesTreeBySize.remove(block.size);
        }
        emptiesMapByPtr.remove(block.ptr);

        block.size -= takeSize;
        block.ptr += takeSize;
        ArrayDeque<MBlock> newArray = emptiesTreeBySize.get(block.size);

        if (newArray != null) {
            newArray.add(block);
        } else {
            newArray = new ArrayDeque<>();
            newArray.add(block);
            emptiesTreeBySize.put(block.size, newArray);
        }
        emptiesMapByPtr.put(block.ptr, block);

        return result;
    }

    private void resizeEmpty(MBlock block, int addSize) {
        ArrayDeque<MBlock> empties = emptiesTreeBySize.get(block.size);
        if (empties.size() == 1) {
            emptiesTreeBySize.remove(block.size);
        } else {
            empties.remove(block);
        }

        block.size += addSize;
        ArrayDeque<MBlock> newArray = emptiesTreeBySize.get(block.size);

        if (newArray != null) {
            newArray.add(block);
        } else {
            newArray = new ArrayDeque<>();
            newArray.add(block);
            emptiesTreeBySize.put(block.size, newArray);
        }
    }

    private MBlock newEmpty(int ptr, int size) {
        MBlock block = new MBlock(ptr, size);
        ArrayDeque<MBlock> newArray = emptiesTreeBySize.get(size);

        if (newArray != null) {
            newArray.add(block);
        } else {
            newArray = new ArrayDeque<>();
            newArray.add(block);
            emptiesTreeBySize.put(size, newArray);
        }

        emptiesMapByPtr.put(ptr, block);

        return block;
    }


    private MBlock pollEmpty(ArrayDeque<MBlock> empties) {
        MBlock block = empties.poll();

        if (empties.size() == 0) {
            emptiesTreeBySize.remove(block.size);
        }
        emptiesMapByPtr.remove(block.ptr);

        return block;
    }

    private void removeEmpty(MBlock block) {
        ArrayDeque<MBlock> empties = emptiesTreeBySize.get(block.size);
        empties.remove(block);
        if (empties.size() == 0) {
            emptiesTreeBySize.remove(block.size);
        }
        emptiesMapByPtr.remove(block.ptr);
    }

    private MBlock newObject(int ptr, int size) {
        MBlock newObject = new MBlock(ptr, size);

        objectsMapByPtr.put(ptr, newObject);

        return newObject;
    }


    private MBlock pollObject(int ptr) throws InvalidPointerException {
        MBlock block = objectsMapByPtr.remove(ptr);
        if (block == null) throw new InvalidPointerException();

        return block;
    }

    public void free(int ptr) throws InvalidPointerException {
        MBlock block = pollObject(ptr);
        MBlock newEmpty = newEmpty(block.ptr, block.size);
        //TODO: добавить newEmpty в очередь для анализа соседей в фоне (не будет эффекта, если высвобождение редкое)
    }

    public void defrag() throws OutOfMemoryException {
        System.out.print("Defrag(" + emptiesMapByPtr.size() + ")...");

        Object[] sorted = emptiesMapByPtr.values().toArray();
        Arrays.sort(sorted, Comparator.comparingInt(b -> ((MBlock) b).ptr));
        if (sorted.length <= 1) throw new OutOfMemoryException();

        MBlock prevBlock = (MBlock) sorted[0];
        int len = sorted.length;

        for (int i = 1; i < len; i++) {
            MBlock block = (MBlock) sorted[i];
            if (block.size > 0) {
                if (block.ptr == prevBlock.ptr + prevBlock.size) {
                    removeEmpty(prevBlock);
                    resizeEmpty(block, prevBlock.size);
                }
                prevBlock = block;
            }
        }
        System.out.println(" done(" + emptiesMapByPtr.size() + ")");
    }

    public void compact() throws OutOfMemoryException {
        System.out.print("Compact(" + emptiesMapByPtr.size() + ")...");

        Object[] sorted = objectsMapByPtr.values().toArray();
        Arrays.sort(sorted, Comparator.comparingInt(b -> ((MBlock) b).ptr));
        if (sorted.length == 0) throw new OutOfMemoryException();

        MBlock prevBlock = (MBlock) sorted[0];
        int len = sorted.length;

        for (int i = 1; i < len; i++) {
            MBlock block = (MBlock) sorted[i];
            int freePtr = prevBlock.ptr+prevBlock.size;
            if (freePtr < block.ptr) {
                moveObject(block, freePtr);
            }
            prevBlock = block;
        }
        int lastByte = prevBlock.ptr+prevBlock.size;

        System.out.print(" lastByte=" + lastByte);

        emptiesTreeBySize = new TreeMap<>();
        emptiesMapByPtr = new HashMap<>();
        int size = memory.length - lastByte;
        MBlock block = new MBlock(lastByte, size);
        ArrayDeque<MBlock> newArray = new ArrayDeque<>();
        newArray.add(block);
        emptiesTreeBySize.put(size, newArray);
        emptiesMapByPtr.put(lastByte, block);
        System.out.println(" done(" + emptiesMapByPtr.size() + ")");
    }

    private void moveObject(MBlock block, int ptr) {
        HeapTest.getBytes(block.ptr, null);
        HeapTest.setBytes(ptr, null);
        objectsMapByPtr.remove(block.ptr);
        block.ptr = ptr;
        objectsMapByPtr.put(block.ptr, block);
    }
}
/*
Defrag(225316)... done(194920)
Compact(194920)... lastByte=999709213 done(1)
Defrag(133)... done(111)
Compact(111)... lastByte=999958068 done(1)
Defrag(20)... done(16)
Compact(16)... lastByte=999973100 done(1)
Defrag(8)... done(7)
Compact(7)... lastByte=999991694 done(1)

free memory: 271
malloc time: 13315 free time: 3036
total time: 16351 count: 3564159
[
main.synchro.gc.Heap(int)                             total:581    self:581    count:1         ns/exec:581000000,
main.synchro.gc.Heap.compact()                        total:7514   self:2896   count:4         ns/exec:724000000,
main.synchro.gc.Heap.defrag()                         total:1586   self:177    count:4         ns/exec:44250000,
main.synchro.gc.Heap.free(int)                        total:2498   self:1480   count:1782050   ns/exec:830,
main.synchro.gc.Heap.malloc(int)                      total:15024  self:3226   count:3564159   ns/exec:905,
main.synchro.gc.Heap.moveObject(.synchro.gc.Heap$MBlock,int)total:4618   self:1549   count:1781888   ns/exec:869,
main.synchro.gc.Heap.newEmpty(int,int)                total:595    self:595    count:1782050   ns/exec:333,
main.synchro.gc.Heap.newObject(int,int)               total:963    self:963    count:3564159   ns/exec:270,
main.synchro.gc.Heap.pollEmpty(java.util.ArrayDeque)  total:446    self:446    count:1556576   ns/exec:286,
main.synchro.gc.Heap.pollObject(int)                  total:423    self:423    count:1782050   ns/exec:237,
main.synchro.gc.Heap.removeEmpty(.synchro.gc.Heap$MBlock)total:740    self:740    count:30423     ns/exec:24323,
main.synchro.gc.Heap.resizeEmpty(.synchro.gc.Heap$MBlock,int)total:669    self:669    count:30423     ns/exec:21989,
main.synchro.gc.Heap.shrinkEmpty(java.util.ArrayDeque,int)total:1289   self:1289   count:2007583   ns/exec:642,
main.synchro.gc.HeapTest.<clinit>()                   total:0      self:0      count:1         ns/exec:0,
main.synchro.gc.HeapTest.getBytes(int,byte[])         total:3069   self:3069   count:1781888   ns/exec:1722,
main.synchro.gc.HeapTest.getRandomSize()              total:410    self:410    count:3564159   ns/exec:115,
main.synchro.gc.HeapTest.main(java.lang.String[])     total:23125  self:0      count:1         ns/exec:0,
main.synchro.gc.HeapTest.mainTest(boolean)            total:23125  self:4612   count:1         ns/exec:4612000000]
*/