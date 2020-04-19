package ru.progwards.java2.lessons.synchro;

import java.util.*;

public class Heap {

    // Исключение: не верный указатель.
    // Возникает при освобождении блока, если переданный указатель не является началом блока
    public class InvalidPointerException extends RuntimeException {}
    // Исключение: нет свободного блока подходящего размера
    public class OutOfMemoryException extends RuntimeException {}

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
    TreeMap<Integer, ArrayList<MBlock>> emptiesTreeBySize; // поиск пустых блоков по размеру
    PriorityQueue<MBlock> emptiesQueueByPtr; // список пустых по адресу
    final int averageObjectSize = 64; // средний размер объекта

    Heap(int maxHeapSize) {
        memory = new byte[maxHeapSize];
        int expectedObjectsCount = maxHeapSize / averageObjectSize;
        int expectedEmptiesCount = expectedObjectsCount / 10;

        objectsMapByPtr = new HashMap<Integer, MBlock>(expectedObjectsCount);
        objectsQueueByPtr = new PriorityQueue<MBlock>(expectedObjectsCount);
        emptiesTreeBySize = new TreeMap<Integer, ArrayList<MBlock>>();
        emptiesQueueByPtr = new PriorityQueue<MBlock>(expectedEmptiesCount);

        MBlock emptyBlock = new MBlock(0, maxHeapSize);
        ArrayList<MBlock> emptyBlockArray = new ArrayList<>();
        emptyBlockArray.add(emptyBlock);
        emptiesTreeBySize.put(maxHeapSize, emptyBlockArray);
    }

    public int malloc(int size) throws OutOfMemoryException {
        Map.Entry<Integer, ArrayList<MBlock>> found = emptiesTreeBySize.ceilingEntry(size);
        if(found==null) {
            defrag();
            found = emptiesTreeBySize.ceilingEntry(size);
            if(found==null) {
                compact();
                found = emptiesTreeBySize.ceilingEntry(size);
                if(found==null) throw new OutOfMemoryException();
            }
        }
        return
    }

    public void free(int size) throws InvalidPointerException {

    }

    public void defrag() {

    }

    public void compact() {

    }
}
