package ru.progwards.java2.lessons.generics;

public class DynamicArray<T> {
/*
Реализовать класс, BlockArray - обобщающий динамический массив, растущий блоками, на основе обычного статического массива.
Стратегия роста - при полном заполнении текущего объема, новый размер массива должен быть в 2 раза больше предыдущего.
*/

    private T[] storage;    // хранилище
    private int length;     // количество элементов в массиве
    private int size = 16;  // размер хранилища
    private int incPercent = 100; // на сколько процентов увеличивать переполненное хранилище

    DynamicArray() {
        init();
    }

    DynamicArray(int size) {
        this.size = size;
        init();
    }

    DynamicArray(int size, int incPercent) {
        this.size = size;
        this.incPercent = incPercent > 1 ? incPercent : 1;
        init();
    }

    private void init() {
        length = 0;
        storage = (T[]) new Object[size];
    }

    private void increment() {
        int newSize = size + size * incPercent / 100;
        if (newSize==size) newSize++;
        T[] newStorage = (T[]) new Object[newSize];
        System.arraycopy(storage, 0, newStorage, 0, size);
        storage = newStorage;
        size = newSize;
    }

    // добавляет элемент в конец массива
    public void add(T item) {
        if (length == size) increment();
        storage[length++] = item;
    }

    private void checkPos(int pos) {
        if (pos >= length)
            throw new RuntimeException("Position " + pos + " is greater than length " + length + " of array");
        if (pos < 0) throw new RuntimeException("Position " + pos + " is less than first element index");
    }

    // добавляет элемент в заданную позицию позицию массива. Параметр int pos - первый, параметр с элементом массива - второй.
    public void insert(int pos, T item) {
        if (pos > length)
            throw new RuntimeException("Position " + pos + " is greater than length " + length + " of array"); // не могу checkPos, т.к. pos может быть равен length
        if (pos < 0) throw new RuntimeException("Position " + pos + " is less than first element index");
        if (length == size) increment();

        for (int i = length; i > pos; i--) storage[i] = storage[i - 1];
        storage[pos] = item;
        length++;
    }

    // удаляет элемент в позиции pos массива.
    public void remove(int pos) {
        checkPos(pos);
        System.arraycopy(storage, pos + 1, storage, pos, length - pos - 1);
        length--;
    }

    // возвращает элемент по индексу pos
    public T get(int pos) {
        checkPos(pos);
        return storage[pos];
    }

    // возвращает текущий реальный объем массива
    public int size() {
        return size;
    }

    // возвращает количество элементов в массиве
    public int length() {
        return length;
    }

}