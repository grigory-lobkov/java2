package ru.progwards.java2.lessons.basetypes;

import java.util.*;

public class BiDirList<T> implements Iterable<T> {
    /*
                 Двунаправленный связный список
     */

    public static class Node<T> {
        T item;
        Node prev;
        Node next;

        Node(Node<T> prev, T item, Node<T> next) {
            this.item = item;
            this.prev = prev;
            this.next = next;
        }
    }

    int size;           // размер списка
    Node<T> firstNode;  // первый элемент списка
    Node<T> lastNode;   // последний элемент списка

    BiDirList() {
        size = 0;
        firstNode = null;
        lastNode = null;
    }

    // добавить в конец списка
    public void addLast(T item) {
        Node<T> node = new Node(lastNode, item, null);
        if(lastNode!=null)
            lastNode.next = node;
        else
            firstNode = node;
        lastNode = node;
        size++;
    }

    // добавить в начало списка
    public void addFirst(T item) {
        Node<T> node = new Node(null, item, firstNode);
        if(firstNode!=null)
            firstNode.prev = node;
        else
            lastNode = node;
        firstNode = node;
        size++;
    }

    // удалить
    public void remove(T item) {
        Node<T> node = firstNode;
        while (node != null) {
            if (node.item == item) {
                // элемент найден
                if (node.prev == null)
                    firstNode = node.next;
                else
                    node.prev.next = node.next;

                if (node.next == null)
                    lastNode = node.prev;
                else
                    node.next.prev = node.prev;
                size--;
            }
            node = node.next;
        }
    }

    // получить элемент по индексу
    public T at(int i) {
        if (i < 0 || i >= size) return null;
        int l = 0;
        Node<T> node = firstNode;
        while (node != null && l != i) {
            node = node.next;
            l++;
        }
        return node.item;
    }

    // получить количество элементов
    public int size() {
        return size;
    }

    // конструктор из списка
    public static <T> BiDirList<T> from(T[] array) {
        BiDirList<T> result = new BiDirList<T>();
        for (T item : array) {
            result.addLast(item);
        }
        return result;
    }

    // конструктор из массива
    public static <T> BiDirList<T> of(T... array) {
        BiDirList<T> list = new BiDirList<T>();
        for (T item : array) {
            list.addLast(item);
        }
        return list;
    }

    //скопировать в массив
    public T[] toArray() {
        T[] array = (T[]) new Object[size];
        int i = 0;
        Node<T> node = firstNode;
        while (node != null) {
            array[i++] = node.item;
            node = node.next;
        }
        return array;
    }

    //получить итератор
    public Iterator<T> getIterator() {
        return new ForwardIterator();
    }
    public Iterator<T> iterator() {
        return new ForwardIterator();
    }

    private class ForwardIterator implements Iterator<T> {
        private Node<T> lastReturned;

        ForwardIterator() {
            lastReturned = null;
        }

        public boolean hasNext() {
            return lastReturned==null || lastReturned.next != null;
        }

        public T next() {
            lastReturned = lastReturned == null ? firstNode : lastReturned.next;
            return lastReturned.item;
        }
    }

    public static void main(String[] args) {
        BiDirList<Integer> l = BiDirList.of(3,1,4,6);
        for (Integer i:l) {
            System.out.println(i);
        }
    }
}