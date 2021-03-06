package ru.progwards.java2.lessons.basetypes;

import javax.swing.text.html.HTMLDocument;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;


// Интерфейс для ключей таблицы

interface HashValue {
    int getHash();
}


// Пример реализации ключа

class IntValue implements HashValue {
    private final int v;
    public IntValue(int value) {
        v = value;
    }
    public static IntValue of(int value) {
        return new IntValue(value);
    }
    public String toString() {
        return "{" + v + '}';
    }
    public int getHash(){
        return v;
    }
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntValue intValue = (IntValue) o;
        return v == intValue.v;
    }
}


// Лямбда для задания алгоритма поиска позиции хранения

interface DoubleHashFunction {

    // результат должен быть в интервале [0, size-1]
    // hash - рассчитанный хэш элемента
    // size - размер хранилища на текущий момент
    // coll - номер коллизии (начинается от нуля)
    int apply(int hash, int size, int coll);
}


// Реализация таблицы с двойным хэшированием

public class DoubleHashTable<K extends HashValue,V>
        extends Dictionary<K,V> {

    // вспомогательный класс генерации простых чисел для определения размера хранилища
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
                        found = i == num;
                        break;
                    }
                }
            }
            return num;
        }
    }

    // запись для хранения одного элемента таблицы
    static class Entry<K extends HashValue, V> {

        private V value;
        final private K key;
        final private int hash;

        Entry(int hash, K key, V value) {
            this.key = key;
            this.value = value;
            this.hash = hash;
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
     */

    private Object[] storage;      // хранилище
    private boolean[] deleted;     // удаленные элементы
    private int storageSize;       // размер хранилища
    private int incPercent;        // на сколько процентов увеличивать при переполнении
    private int collPercent = 10;  // допустимый процент коллизий
    private int size;              // количество элементов в таблице
    private int threshold;         // при каком количестве коллизий увеличивать хранилище
    private DoubleHashFunction f1; // функция хэширования (принимает хэш, размер хранилища и номер коллизии)

    DoubleHashTable() {
        storageSize = 100;
        incPercent = 100;
        initialize();
    }

    DoubleHashTable(int storageSize) {
        this.storageSize = storageSize;
        incPercent = 100;
        initialize();
    }

    DoubleHashTable(int storageSize, int incPercent) {
        this.storageSize = storageSize;
        this.incPercent = incPercent;
        initialize();
    }

    private void initialize() {
        storageSize = Prime.getHigher(storageSize);
        storage = new Object[storageSize];
        deleted = new boolean[storageSize];
        size = 0;
        threshold = storageSize * collPercent / 100;
        f1 = (hash, size, collis) -> {
            // тут в неявном виде делаем двойное хэширование: для сокращения операций с плавающей точкой
            // объединил функцию по хэш и по коллизиям в одну операцию
            // В пользовательской лямбде может быть как угодно. Это просто дефолтная.
            double d = 0.6180339887d * ((hash + collis * hash) & 0x7FFFFFFF); // золотое сечение =(sqrt(5)-1)/2
            int rslt = (int) ((d - Math.floor(d)) * size);
            //System.out.println("k="+k+" s="+s+" c="+c+"  result="+rslt);
            return rslt;
        };
    }

    public void setCalculateFunction(DoubleHashFunction f) {
        if (size == 0) f1 = f;
        else throw new IllegalStateException("Table is not empty");
    }

    // получить количество элементов
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

    protected void rehashWithIncrement() {
        storageSize = storageSize + storageSize * incPercent / 100;
        rehash();
    }

    protected void rehash() {
        DoubleHashTable table = new DoubleHashTable(storageSize, incPercent);
        table.setCalculateFunction(f1);

        for (int i = storage.length; i-- > 0; ) {
            Entry<K, V> e = (Entry<K, V>) storage[i];
            if (e != null) {
                table.put(e.key, e.value);
            }
        }

        storageSize = table.storageSize;
        storage = table.storage;
        deleted = table.deleted;
        threshold = table.threshold;
    }

    // добавить пару ключ-значение
    public V put(K key, V value) { // from IntDictionary<K,V>
        if (value == null) {
            throw new NullPointerException();
        }
        int hash = key.getHash();
        int collis = 0;
        while (true) {
            if (collis > threshold) {
                rehashWithIncrement();
                collis = 0;
            }
            int index = f1.apply(hash, storageSize, collis++);
            Entry<K, V> e = (Entry<K, V>) storage[index];
            if (e == null) {
                storage[index] = new Entry<>(hash, key, value);
                deleted[index] = false;
                size++;
                return null;
            } else if ((e.hash == hash) && e.key.equals(key)) {
                V oldValue = e.value;
                e.value = value;
                size++;
                return oldValue;
            }
        }
    }

    // добавить пару ключ-значение
    public V add(K key, V value) { // по задаче имя д.б. такое
        return put(key, value);
    }

    // получить значение по ключу
    public V get(Object key) { // from IntDictionary<K,V>
        int hash = ((K) key).getHash();
        int collis = 0;
        while (true) {
            if (collis > threshold) {
                return null;
            }
            int index = f1.apply(hash, storageSize, collis++);
            Entry<K, V> e = (Entry<K, V>) storage[index];
            if (e == null) {
                if (!deleted[index]) return null;
            } else if ((e.hash == hash) && e.key.equals(key)) {
                return e.value;
            }
        }
    }

    // удалить элемент по ключу
    public V remove(Object key) {
        int hash = ((K) key).getHash();
        int collis = 0;
        while (true) {
            if (collis > threshold) {
                return null;
            }
            int index = f1.apply(hash, storageSize, collis++);
            Entry<K, V> e = (Entry<K, V>) storage[index];
            if (e == null) {
                if (!deleted[index]) {
                    // элемент не найден и не удален, выходим, или можно вернуть исключение
                    return null;
                }
            } else if ((e.hash == hash) && e.key.equals(key)) {
                storage[index] = null;
                deleted[index] = true;
                size--;
                return e.value;
            }
        }
    }

    // изменить значение ключа у элемента с key1 на key2
    public V change(K key1, K key2) {
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

    public Iterator<Entry<K, V>> getIterator() {
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
        final Object[] table = DoubleHashTable.this.storage;
        final boolean[] deleted = DoubleHashTable.this.deleted;
        int storageIndex = -1;
        int count = 0;
        int allCount = DoubleHashTable.this.size;
        int storageSize = DoubleHashTable.this.storageSize;
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
            return count < allCount;
        }

        public T nextElement() {
            for (int i = storageIndex + 1; i < storageSize; i++) {
                Entry<?, ?> e = (Entry<?, ?>) table[i];
                if (e != null) {
                    storageIndex = i;
                    count++;
                    return type == KEYS ? (T) e.key : (type == VALUES ? (T) e.value : (T) e);
                }
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
            if (!iterator) throw new UnsupportedOperationException();
            storage[storageIndex] = null;
            deleted[storageIndex] = true;
        }
    }

    public static void main(String[] args) {
        /*long start = System.nanoTime();
        System.out.println(Prime.getHigher(10000));
        System.out.println(Prime.getHigher(20));
        System.out.println((System.nanoTime()-start)/1000);//1310751-18x
        System.out.println("[2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101, 103, 107, 109, 113, 127, 131, 137, 139, 149, 151, 157, 163, 167, 173, 179, 181, 191, 193, 197, 199]");
        System.out.println(Prime.list);*/

        DoubleHashTable<IntValue, String> table = new DoubleHashTable();

        table.setCalculateFunction((k, s, c) -> {
            double d = 0.6180339887d * ((k + c) & 0x7FFFFFFF); // золотое сечение =(sqrt(5)-1)/2
            int rslt = (int) ((d - Math.floor(d)) * s);
            //System.out.println("k="+k+" s="+s+" c="+c+"  result="+rslt);
            return rslt;
        });

        table.setCalculateFunction((k, s, c) -> {
            double t1 = 0.6180339887d * (k & 0x7FFFFFFF); // золотое сечение =(sqrt(5)-1)/2
            int f1 = (int) ((t1 - Math.floor(t1)) * s);
            int f2 = c * c;
            int rslt = (f1 + f2) % s;
            //System.out.println("k="+k+" s="+s+" c="+c+"  result="+rslt);
            return rslt;
        });

        // https://neerc.ifmo.ru/wiki/index.php?title=%D0%A0%D0%B0%D0%B7%D1%80%D0%B5%D1%88%D0%B5%D0%BD%D0%B8%D0%B5_%D0%BA%D0%BE%D0%BB%D0%BB%D0%B8%D0%B7%D0%B8%D0%B9#.D0.9F.D1.80.D0.B8.D0.BD.D1.86.D0.B8.D0.BF_.D0.B4.D0.B2.D0.BE.D0.B9.D0.BD.D0.BE.D0.B3.D0.BE_.D1.85.D0.B5.D1.88.D0.B8.D1.80.D0.BE.D0.B2.D0.B0.D0.BD.D0.B8.D1.8F
        table.setCalculateFunction((k, m, c) -> {
            // При двойном хешировании используются две независимые хеш-функции h1(k) и h2(k).
            // Пусть k — это наш ключ, m — размер нашей таблицы, n mod m — остаток от деления n на m,
            // тогда сначала исследуется ячейка с адресом h1(k), если она уже занята, то рассматривается
            // (h1(k)+h2(k))mod m, затем (h1(k)+2⋅h2(k))mod m и так далее.
            // В общем случае идёт проверка последовательности ячеек (h1(k)+i⋅h2(k))modm где i=(0,1,...,m−1)
            double tmp = 0.6180339887d * (k & 0x7FFFFFFF); // золотое сечение =(sqrt(5)-1)/2
            int h1 = (int) ((tmp - Math.floor(tmp)) * m);
            int h2 = k * k;
            int rslt = (h1 + c * h2) % m;
            //System.out.println("k="+k+" s="+s+" c="+c+"  result="+rslt);
            return rslt;
        });

        Function<Integer, Integer> f1 = x -> x * x;
        Function<Integer, Integer> f2 = x -> (int)(618.0339887d*x);

        table.setCalculateFunction((k, m, c) -> {
            int r1 = f1.apply(k);
            int r2 = f2.apply(k);
            int rslt = (r1 + c * r2) % m;
            //System.out.println("k="+k+" s="+s+" c="+c+"  result="+rslt);
            return rslt;
        });

        //table.setCalculateFunction((h,s,c)->c);
        for (int i = 200; i < 388; i++) {
            //211 elements -> 21 collisions limit
            //184 for k+c
            //200 for k+c*k*c
            //207 for k+c*k
            table.put(IntValue.of(i), "value " + i);
        }
        System.out.println("StorageSize=" + table.storageSize);

        /*Iterator<Entry<IntValue, String>> i = table.getIterator();
        while (i.hasNext()) {
            Entry<IntValue, String> e = i.next();
            System.out.println("k="+e.key+" v="+e.value);
        }*/
    }
}