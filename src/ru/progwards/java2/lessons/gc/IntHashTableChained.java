package ru.progwards.java2.lessons.gc;

import java.util.*;

public class IntHashTableChained<V> extends IntDictionary<V> {

    // Хэш таблица с индексом примитива int
    // реализация Hashtable<int, V>
    // количество элементов хранилища - всегда простое число

    private static class Entry<V> {

        private V value;
        final private int key;
        private Entry<V> next;

        Entry(int key, V value, Entry<V> next) {
            this.value = value;
            this.key = key;
            this.next = next;
        }
    }

    private transient Entry<V>[] storage; // хранилище
    private int storageSize;       // размер хранилища
    private int incPercent;        // на сколько процентов увеличивать при переполнении
    private int collPercent = 2;   // допустимый процент коллизий
    private int size;              // количество элементов в таблице
    private int threshold;         // при каком количестве коллизий увеличивать хранилище

    IntHashTableChained() {
            storageSize = 100;
            incPercent = 100;
            initialize();
        }

    IntHashTableChained(int storageSize) {
            this.storageSize = storageSize;
            incPercent = 100;
            initialize();
        }

    IntHashTableChained(int storageSize, int incPercent) {
            this.storageSize = storageSize;
            this.incPercent = incPercent;
            initialize();
        }

    private void initialize() {
        storageSize = Prime.getHigher(storageSize);
        storage = (Entry<V>[])new Entry<?>[storageSize];
        size = 0;
        threshold = storageSize * collPercent / 100;
    }

    public int size() { // from IntDictionary<V>
        return size;
    }

    public boolean isEmpty() { // from IntDictionary<V>
        return size == 0;
    }

    public Enumeration<Integer> keys() { return this.<Integer>getEnumeration(KEYS); }

    public Enumeration<V> elements() {
        return this.<V>getEnumeration(VALUES);
    }

    protected void rehashWithIncrement() {
        storageSize = storageSize + storageSize * incPercent / 100;
        rehash();
    }
    protected void rehash() {
        IntHashTableChained<V> table = new IntHashTableChained(storageSize, incPercent);

        for (int i = storage.length; i-- > 0; ) {
            Entry<V> e = (Entry<V>)storage[i];
            if(e != null) {
                table.put(e.key, e.value);
            }
        }

        storageSize = table.storageSize;
        storage = table.storage;
        threshold = table.threshold;
    }

    public synchronized V put(int key, V value) { // from IntDictionary<V>
        Entry<V>[] tab = storage;
        if (value == null) {
            throw new NullPointerException();
        }
        // Makes sure the key is not already in the hashtable.
        int index = (key & 0x7FFFFFFF) % storageSize;
        Entry<V> entry = tab[index];
        int collis = 0;
        for (; entry != null; entry = entry.next) {
            if (entry.key == key) {
                V old = entry.value;
                entry.value = value;
                return old;
            }
            collis++;
        }

        if (collis >= threshold) {
            rehashWithIncrement();
            index = (key & 0x7FFFFFFF) % storageSize;
            tab = (Entry<V>[])storage;
        }
        Entry<V> e = tab[index];
        tab[index] = new Entry<V>(key, value, e);
        return null;
    }

    public synchronized V add(int key, V value) { // по задаче имя д.б. такое
        return put(key, value);
    }

    public synchronized V get(int key) { // from IntDictionary<V>
        Entry<V>[] tab = storage;
        int index = (key & 0x7FFFFFFF) % storageSize;
        for (Entry<V> n = tab[index]; n != null; n = n.next) {
            if (n.key == key) {
                return n.value;
            }
        }
        return null;
    }

    public synchronized V remove(int key) {
        Entry<V>[] tab = storage;
        int index = (key & 0x7FFFFFFF) % tab.length;
        Entry<V> e = tab[index];
        for(Entry<V> prev = null; e != null ; prev = e, e = e.next) {
            if (e.key == key) {
                if (prev != null) {
                    prev.next = e.next;
                } else {
                    tab[index] = e.next;
                }
                V oldValue = e.value;
                e.value = null;
                return oldValue;
            }
        }
        return null;
    }

    public synchronized V change(int key1, int key2) {
        V value = remove(key1);
        put(key2, value);
        return value;
    }

    private <T> Enumeration<T> getEnumeration(int type) {
        if (size == 0) {
            return Collections.emptyEnumeration();
        } else {
            return new Enumerator<>(type, false);
        }
    }

    private <T> Iterator<T> getIterator(int type) {
        if (size == 0) {
            return Collections.emptyIterator();
        } else {
            return new Enumerator<>(type, true);
        }
    }
    public Iterator<Entry<V>> getIterator() {
        return getIterator(ENTRIES);
    }


    // Types of Enumerations/Iterations
    private static final int KEYS = 0;
    private static final int VALUES = 1;
    private static final int ENTRIES = 2;

    private class Enumerator<T> implements Enumeration<T>, Iterator<T> {
        final Entry<?>[] table = IntHashTableChained.this.storage;
        int index = table.length;
        Entry<?> entry;
        Entry<?> lastReturned;
        final int type;

        final boolean iterator;

        Enumerator(int type, boolean iterator) {
            this.type = type;
            this.iterator = iterator;
        }

        public boolean hasMoreElements() {
            Entry<?> e = entry;
            int i = index;
            Entry<?>[] t = table;
            while (e == null && i > 0) {
                e = t[--i];
            }
            entry = e;
            index = i;
            return e != null;
        }

        public T nextElement() {
            Entry<?> et = entry;
            int i = index;
            Entry<?>[] t = table;

            while (et == null && i > 0) {
                et = t[--i];
            }
            entry = et;
            index = i;
            if (et != null) {
                Entry<?> e = lastReturned = entry;
                entry = e.next;
                return type == KEYS ? (T)Integer.valueOf(e.key) : (type == VALUES ? (T)e.value : (T)e);
            }
            throw new NoSuchElementException("Hashtable Enumerator");
        }

        // Iterator methods
        public boolean hasNext() {
            return hasMoreElements();
        }

        public T next() {
            return nextElement();
        }

        public void remove() {
            if (!iterator)
                throw new UnsupportedOperationException();
            if (lastReturned == null)
                throw new IllegalStateException("Hashtable Enumerator");

            synchronized(this) {
                Entry<?>[] tab = IntHashTableChained.this.storage;
                int index = (lastReturned.key & 0x7FFFFFFF) % IntHashTableChained.this.storageSize;

                Entry<V> e = (Entry<V>)tab[index];
                for(Entry<V> prev = null; e != null; prev = e, e = e.next) {
                    if (e == lastReturned) {
                        if (prev == null)
                            tab[index] = e.next;
                        else
                            prev.next = e.next;
                        lastReturned = null;
                        return;
                    }
                }
                throw new ConcurrentModificationException();
            }
        }
    }


    public static void main(String[] args) {
//    long start = System.nanoTime();
//    System.out.println(Prime.getHigher(10000));
//    System.out.println((System.nanoTime()-start)/1000);//1310751-18x
//    System.out.println("[2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101, 103, 107, 109, 113, 127, 131, 137, 139, 149, 151, 157, 163, 167, 173, 179, 181, 191, 193, 197, 199]");
//    System.out.println(Prime.list);
        IntHashTableChained<String> table = new IntHashTableChained();
        //table.setCalculateFunction((h,s,c)->c);
        for(int i=0; i<8408; i++) { //8408 items stores in 863 capacity hashtable
            table.put(i, "value "+i);
        }
        System.out.println("StorageSize="+table.storageSize);
        /*Iterator<Entry<IntValue, String>> i = table.getIterator();
        while (i.hasNext()) {
            Entry<IntValue, String> e = i.next();
            System.out.println("k="+e.key+" v="+e.value);
        }*/

    }

}
