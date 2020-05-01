package ru.progwards.java2.lessons.synchro.gc;

import java.util.*;

public class Heap_initial_Queue {

    // Исключение: не верный указатель.
    // Возникает при освобождении блока, если переданный указатель не является началом блока
    public class InvalidPointerException extends Exception {}
    // Исключение: нет свободного блока подходящего размера
    public class OutOfMemoryException extends Exception {}

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
    PriorityQueue<MBlock> objectsQueueByPtr; // список объектов сортированный по адресам
    TreeMap<Integer, ArrayDeque<MBlock>> emptiesTreeBySize; // поиск пустых блоков по размеру
    PriorityQueue<MBlock> emptiesQueueByPtr; // список пустых по адресу
    final int averageObjectSize = 64; // средний размер объекта (для рассчета общего количества)

    Heap_initial_Queue(int maxHeapSize) {
        memory = new byte[maxHeapSize];
        int expectedObjectsCount = maxHeapSize / averageObjectSize;
        int expectedEmptiesCount = expectedObjectsCount / 10;

        objectsMapByPtr = new HashMap<>(expectedObjectsCount);
        objectsQueueByPtr = new PriorityQueue<>(expectedObjectsCount, Comparator.comparingInt(b -> -b.ptr));
        emptiesTreeBySize = new TreeMap<>();
        emptiesQueueByPtr = new PriorityQueue<>(expectedEmptiesCount, Comparator.comparingInt(b -> b.size));

        MBlock emptyBlock = new MBlock(0, maxHeapSize);
        ArrayDeque<MBlock> emptyBlockArray = new ArrayDeque<>();
        emptyBlockArray.add(emptyBlock);
        emptiesTreeBySize.put(maxHeapSize, emptyBlockArray);
        emptiesQueueByPtr.add(emptyBlock);
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
                if (found == null) throw new OutOfMemoryException();
            }
        }

        MBlock foundEmpty = pollEmpty(found.getValue());
        MBlock newEmpty = newEmpty(foundEmpty.ptr + size, foundEmpty.size - size);
        MBlock newObject = newObject(foundEmpty.ptr, size);

        return newObject.ptr;
    }


    private MBlock newEmpty(int ptr, int size) {
        MBlock block = new MBlock(ptr, size);
        ArrayDeque<MBlock> newArray = emptiesTreeBySize.get(block.size);

        if(newArray!=null) {
            newArray.add(block);
        } else {
            newArray = new ArrayDeque<>();
            newArray.add(block);
            emptiesTreeBySize.put(size, newArray);
        }
        emptiesQueueByPtr.add(block);

        return block;
    }


    private MBlock pollEmpty(ArrayDeque<MBlock> empties) {
        MBlock block = empties.poll();

        if (empties.size() == 0) {
            emptiesTreeBySize.remove(block.size);
        }
        emptiesQueueByPtr.remove(block);

        return block;
    }


    private void removeEmpty(MBlock block) {
        ArrayDeque<MBlock> empties = emptiesTreeBySize.get(block.size);
        empties.remove(block);
        if (empties.size() == 0) {
            emptiesTreeBySize.remove(block.size);
        }
        emptiesQueueByPtr.remove(block);
    }


    private MBlock newObject(int ptr, int size) {
        MBlock newObject = new MBlock(ptr, size);

        objectsMapByPtr.put(ptr, newObject);
        objectsQueueByPtr.add(newObject);

        return newObject;
    }


    private MBlock pollObject(int ptr) throws InvalidPointerException {

        MBlock block = objectsMapByPtr.remove(ptr);
        if(block==null) throw new InvalidPointerException();
        objectsQueueByPtr.remove(block);

        return block;
    }


    public void free(int ptr) throws InvalidPointerException {
        MBlock block = pollObject(ptr);
        MBlock newEmpty = newEmpty(block.ptr, block.size);
        //TODO: добавить newEmpty в очередь для анализа соседей в фоне (не будет эффекта в нашем тесте, т.к. высвобождение редкое)
    }

    public void defrag() throws OutOfMemoryException {
        System.out.println("Defrag...");
        Iterator<MBlock> iterator = emptiesQueueByPtr.iterator();
        if(!iterator.hasNext()) throw new OutOfMemoryException();
        MBlock prevBlock = iterator.next();

        while (iterator.hasNext()) {
            MBlock block = iterator.next();
            if(block.ptr == prevBlock.ptr+prevBlock.size) {
                removeEmpty(prevBlock);
                removeEmpty(block);
                prevBlock = newEmpty(prevBlock.ptr, prevBlock.size+block.size);
            } else {
                prevBlock = block;
            }
        }
    }

    public void compact() throws OutOfMemoryException {
        System.out.println("Compact...");
        Iterator<MBlock> iterator = objectsQueueByPtr.iterator(); //descending order
        if(!iterator.hasNext()) throw new OutOfMemoryException();
        MBlock prevBlock = iterator.next();

        while (iterator.hasNext()) {
            MBlock block = iterator.next();
            if(prevBlock.ptr > block.ptr+block.size) {
                moveObject(block, prevBlock.ptr-block.size);
            }
            prevBlock = block;
        }
    }

    private void moveObject(MBlock block, int ptr) {
        HeapTest.getBytes(block.ptr, null);
        HeapTest.setBytes(ptr, null);
        objectsMapByPtr.remove(block.ptr);
        block.ptr = ptr;
        objectsMapByPtr.put(block.ptr, block);
        //objectsQueueByPtr менять не требуется, т.к. позиция внутри дерева не поменяется
    }
}
/* //no Defrag, no Compact
malloc time: 186120 free time: 64977
total time: 251097 execsCount: 2213471
*/