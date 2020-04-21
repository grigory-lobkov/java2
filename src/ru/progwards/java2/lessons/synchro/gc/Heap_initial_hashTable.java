package ru.progwards.java2.lessons.synchro.gc;

import java.util.*;


public class Heap_initial_hashTable {

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

    Heap_initial_hashTable(int maxHeapSize) {
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
Defrag(226221)... done(195890)
Compact(195890)... lastByte=999686802 done(1)
Defrag(91)... done(81)
Compact(81)... lastByte=999959011 done(1)
Defrag(22)... done(18)
Compact(18)... lastByte=999993275 done(1)

free memory: 568
malloc time: 12621 free time: 2973
total time: 15594 count: 3554168
[
main.synchro.gc.Heap(int)                             total:572    self:572    count:1         ns/exec:572000000,
main.synchro.gc.Heap.compact()                        total:4518   self:2156   count:3         ns/exec:718666666,
main.synchro.gc.Heap.defrag()                         total:1611   self:181    count:3         ns/exec:60333333,
main.synchro.gc.Heap.free(int)                        total:2450   self:1510   count:1778305   ns/exec:849,
main.synchro.gc.Heap.malloc(int)                      total:11543  self:3408   count:3554168   ns/exec:958,
main.synchro.gc.Heap.moveObject(.synchro.gc.Heap$MBlock,int)total:2362   self:1752   count:1769642   ns/exec:990,
main.synchro.gc.Heap.newEmpty(int,int)                total:698    self:698    count:1778305   ns/exec:392,
main.synchro.gc.Heap.newObject(int,int)               total:645    self:645    count:3554168   ns/exec:181,
main.synchro.gc.Heap.pollEmpty(java.util.ArrayDeque)  total:415    self:415    count:1551965   ns/exec:267,
main.synchro.gc.Heap.pollObject(int)                  total:242    self:242    count:1778305   ns/exec:136,
main.synchro.gc.Heap.removeEmpty(.synchro.gc.Heap$MBlock)total:746    self:746    count:30345     ns/exec:24583,
main.synchro.gc.Heap.resizeEmpty(.synchro.gc.Heap$MBlock,int)total:684    self:684    count:30345     ns/exec:22540,
main.synchro.gc.Heap.shrinkEmpty(java.util.ArrayDeque,int)total:946    self:946    count:2002203   ns/exec:472,
main.synchro.gc.HeapTest.<clinit>()                   total:0      self:0      count:1         ns/exec:0,
main.synchro.gc.HeapTest.getBytes(int,byte[])         total:610    self:610    count:1769642   ns/exec:344,
main.synchro.gc.HeapTest.getRandomSize()              total:312    self:312    count:3554168   ns/exec:87,
main.synchro.gc.HeapTest.main(java.lang.String[])     total:18331  self:0      count:1         ns/exec:0,
main.synchro.gc.HeapTest.mainTest(boolean)            total:18331  self:3454   count:1         ns/exec:3454000000]
*/