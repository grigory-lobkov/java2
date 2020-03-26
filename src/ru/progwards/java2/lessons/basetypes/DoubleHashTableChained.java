package ru.progwards.java2.lessons.basetypes;

import java.util.*;

// не верная реализация, при двойном хэшировании должны быть в формуле коллизии, а здесь их не впихнешь :)

public class DoubleHashTableChained<K extends HashValue,V>
        extends Dictionary<K,V> {

    static class Prime {
        static List<Integer> list;
        private static int lastChecked;

        static {
            list = new ArrayList<Integer>();
            list.addAll(Arrays.asList(2, 3, 5, 7, 11));
            lastChecked = 12;
        }

        static private void fillArray(int fillTo) {
            for (int num = lastChecked + 1; num <= fillTo; num++) {
                for (int i : list) {
                    if (num % i == 0) break;
                    if (i * i > num) {
                        list.add(num);
                        break;
                    }
                }
            }
            lastChecked = fillTo;
        }

        static int getHigher(int num) {
            int fillTo = (int) Math.sqrt(num + Math.sqrt(num)); // до куда проверяем, интервал между простыми примерно равен корню из простого
            fillArray(fillTo);
            boolean found = false;
            while (!found) {
                num++;
                found = true;
                for (int i : list) {
                    if (num % i == 0) {
                        found = false;
                        break;
                    }
                }
            }
            return num;
        }
    }

    class Entry<K extends HashValue, V> {

        private V value;
        final private K key;
        final private int hash;
        private Entry<K, V> next;

        Entry(int hash, K key, V value, Entry<K, V> next) {
            this.key = key;
            this.value = value;
            this.hash = hash;
            this.next = next;
        }
    }
    /*
Реализовать класс DoubleHashTable - хэш таблица с двойным хэшированием

В качестве размера таблицы выбирать простое число, первоначальное значение 101

При количестве коллизий более 10% при одной серии пробирований - перестраивать таблицу, увеличивая размер

Стратегия роста - удвоение размера, но с учетом правила - размер таблицы простое число. Т.е. искать ближайшее простое

Ключи должны реализовывать интерфейс interface HashValue {int getHash();}

Для числовых значение взять хэш 2 функции - деление и умножение

Для строковых - выбрать что-то, из представленных в лекции

Методы

1.1 public void add(K key, V value) - добавить пару ключ-значение

1.2 public V get(K key) - получить значение по ключу

1.3 public void remove(K key) - удалить элемент по ключу

1.4 public void change(K key1, Key key2) - изменить значение ключа у элемента с key1 на key2

1.5 public int size() - получить количество элементов

1.6 public Iterator<DoubleHashTable<K,V>> getIterator()
     */

    private Entry<K, V>[] storage; // хранилище
    //private boolean deleted; // удаленные элементы
    private int storageSize;       // размер хранилища
    private int incPercent;        // на сколько процентов увеличивать при переполнении
    private int collPercent = 10;  // допустимый процент коллизий
    private int size;              // количество элементов в таблице
    private int threshold;         // при каком количестве коллизий увеличивать хранилище

    DoubleHashTableChained() {
        storageSize = 100;
        incPercent = 100;
        initialize();
    }

    DoubleHashTableChained(int storageSize) {
        this.storageSize = storageSize;
        incPercent = 100;
        initialize();
    }

    DoubleHashTableChained(int storageSize, int incPercent) {
        this.storageSize = storageSize;
        this.incPercent = incPercent;
        initialize();
    }

    private

    void initialize() {
        storageSize = Prime.getHigher(storageSize);
        storage = (Entry<K,V>[])new Object[storageSize];
        size = 0;
        threshold = storageSize * collPercent / 100;
    }


    public int size() { // from IntDictionary<K,V>
        return size;
    }

    public boolean isEmpty() { // from IntDictionary<K,V>
        return size == 0;
    }

    public Enumeration<K> keys() {
        return this.<K>getEnumeration(KEYS);
    }

    public Enumeration<V> elements() {
        return this.<V>getEnumeration(VALUES);
    }

    protected void rehash() {
        int newStorageSize = Prime.getHigher(storageSize*incPercent/100);
        Entry<K,V>[] newStorage = (Entry<K,V>[])new Object[newStorageSize];

        for (int i = storageSize ; i-- > 0 ;) {
            for (Entry<K,V> old = storage[i] ; old != null ; ) {
                Entry<K,V> e = old;
                old = old.next;

                int index = (e.hash & 0x7FFFFFFF) % newStorageSize;
                e.next = newStorage[index];
                newStorage[index] = e;
            }
        }
        storage = newStorage;
        storageSize = newStorageSize;
        threshold = newStorageSize * collPercent / 100;
    }

    private void addEntry(int hash, K key, V value, int index, int collis) {
        if (collis >= threshold) {
            rehash();
            index = (hash & 0x7FFFFFFF) % storageSize;
        }
        Entry<K, V> e = storage[index];
        storage[index] = new Entry<K, V>(hash, key, value, e);
    }

    public synchronized V put(K key, V value) { // from IntDictionary<K,V>
        Entry<K,V>[] tab = storage;
        if (value == null) {
            throw new NullPointerException();
        }
        // Makes sure the key is not already in the hashtable.
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % storageSize;
        Entry<K, V> entry = tab[index];
        int collis = 0;
        for (; entry != null; entry = entry.next) {
            if ((entry.hash == hash) && entry.key.equals(key)) {
                V old = entry.value;
                entry.value = value;
                return old;
            }
            collis++;
        }

        addEntry(hash, key, value, index, collis);
        return null;
    }

    public synchronized V add(K key, V value) { // по задаче имя д.б. такое
        return put(key, value);
    }

    public synchronized V get(Object key) { // from IntDictionary<K,V>
        Entry<K,V>[] tab = storage;
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % storageSize;
        for (Entry<K, V> n = tab[index]; n != null; n = n.next) {
            if ((n.hash == hash) && n.key.equals(key)) {
                return n.value;
            }
        }
        return null;
    }

    public synchronized V remove(Object key) {
        Entry<K,V>[] tab = storage;
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;
        Entry<K,V> e = tab[index];
        for(Entry<K,V> prev = null ; e != null ; prev = e, e = e.next) {
            if ((e.hash == hash) && e.key.equals(key)) {
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

    public synchronized V change(K key1, K key2) {
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
    public Iterator<Entry<K,V>> getIterator() {
        return getIterator(ENTRIES);
    }


    // Types of Enumerations/Iterations
    private static final int KEYS = 0;
    private static final int VALUES = 1;
    private static final int ENTRIES = 2;

    /**
     * A hashtable enumerator class.  This class implements both the
     * Enumeration and Iterator interfaces, but individual instances
     * can be created with the Iterator methods disabled.  This is necessary
     * to avoid unintentionally increasing the capabilities granted a user
     * by passing an Enumeration.
     */
    private class Enumerator<T> implements Enumeration<T>, Iterator<T> {
        final Entry<?,?>[] table = DoubleHashTableChained.this.storage;
        int index = table.length;
        Entry<?,?> entry;
        Entry<?,?> lastReturned;
        final int type;

        /**
         * Indicates whether this Enumerator is serving as an Iterator
         * or an Enumeration.  (true -> Iterator).
         */
        final boolean iterator;

        Enumerator(int type, boolean iterator) {
            this.type = type;
            this.iterator = iterator;
        }

        public boolean hasMoreElements() {
            Entry<?,?> e = entry;
            int i = index;
            Entry<?,?>[] t = table;
            while (e == null && i > 0) {
                e = t[--i];
            }
            entry = e;
            index = i;
            return e != null;
        }

        public T nextElement() {
            Entry<?,?> et = entry;
            int i = index;
            Entry<?,?>[] t = table;
            /* Use locals for faster loop iteration */
            while (et == null && i > 0) {
                et = t[--i];
            }
            entry = et;
            index = i;
            if (et != null) {
                Entry<?,?> e = lastReturned = entry;
                entry = e.next;
                return type == KEYS ? (T)e.key : (type == VALUES ? (T)e.value : (T)e);
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

            synchronized(DoubleHashTableChained.this) {
                Entry<?,?>[] tab = DoubleHashTableChained.this.storage;
                int index = (lastReturned.hash & 0x7FFFFFFF) % DoubleHashTableChained.this.storageSize;

                Entry<K,V> e = (Entry<K,V>)tab[index];
                for(Entry<K,V> prev = null; e != null; prev = e, e = e.next) {
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
        /*long start = System.nanoTime();
        System.out.println(Prime.getHigher(1000));
        System.out.println((System.nanoTime()-start)/1000);//1310751-18x
        System.out.println("[2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101, 103, 107, 109, 113, 127, 131, 137, 139, 149, 151, 157, 163, 167, 173, 179, 181, 191, 193, 197, 199]");
        System.out.println(Prime.list);*/
    }
}