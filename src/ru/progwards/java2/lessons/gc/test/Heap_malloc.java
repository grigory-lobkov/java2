package ru.progwards.java2.lessons.gc.test;

import org.junit.*;
import ru.progwards.java2.lessons.gc.Heap;
import ru.progwards.java2.lessons.gc.InvalidPointerException;
import ru.progwards.java2.lessons.gc.OutOfMemoryException;

public class Heap_malloc {

    final int heapSize = 128;
    Heap heap; // куча

    @Before
    public void init() {
        heap = new Heap(heapSize);
    }

    @Test
    public void malloc_byte() throws OutOfMemoryException {
        int expected = 0;
        int actual = heap.malloc(1);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void malloc_full() throws OutOfMemoryException {
        int expected = 0;
        int actual = heap.malloc(heapSize);
        Assert.assertEquals(expected, actual);
    }

    @Test (expected = OutOfMemoryException.class)
    public void malloc_huge() throws OutOfMemoryException {
        heap.malloc(heapSize+1);
    }

    @Test
    public void malloc_byte2() throws OutOfMemoryException {
        int expected = 1;
        heap.malloc(1);
        int actual = heap.malloc(1);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void malloc_full2() throws OutOfMemoryException {
        int expected = heapSize/2;
        heap.malloc(heapSize/2);
        int actual = heap.malloc(heapSize/2);
        Assert.assertEquals(expected, actual);
    }

    @Test (expected = OutOfMemoryException.class)
    public void malloc_huge2() throws OutOfMemoryException {
        heap.malloc(heapSize/2+10);
        int actual = heap.malloc(heapSize/2);
        Assert.assertEquals(-1, actual);
    }

    @Test
    public void malloc_byte3() throws OutOfMemoryException, InvalidPointerException {
        // мой алгоритм после высвобождения мелкого блока повторно должен взять именно его
        int pos1=heap.malloc(1);
        int pos2=heap.malloc(1);
        int pos3=heap.malloc(1);
        int pos4=heap.malloc(1);

        heap.free(pos2,1);
        int actual = heap.malloc(1);
        Assert.assertEquals(pos2, actual);

        heap.free(pos1,1);
        actual = heap.malloc(1);
        Assert.assertEquals(pos1, actual);
    }

    @Test
    public void malloc_full3() throws OutOfMemoryException, InvalidPointerException {
        // мой алгоритм после высвобождения мелкого блока повторно должен взять именно его
        int s = heapSize/4;
        int pos1=heap.malloc(s);
        int pos2=heap.malloc(s);
        int pos3=heap.malloc(s);
        int pos4=heap.malloc(s);

        heap.free(pos2,s);
        int actual = heap.malloc(s);
        Assert.assertEquals(pos2, actual);

        heap.free(pos1,s);
        actual = heap.malloc(s);
        Assert.assertEquals(pos1, actual);
    }

    @After
    public void close() {
        heap = null;
    }
}
