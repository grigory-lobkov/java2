package ru.progwards.java2.lessons.synchro.gc;

public interface HeapInterface {

    //HeapInterface(int maxHeapSize);

    void dispose();

    int malloc(int size) throws OutOfMemoryException;

    void free(int ptr) throws InvalidPointerException;

    void defrag() throws OutOfMemoryException;

    void compact() throws OutOfMemoryException;
}