package ru.progwards.java2.lessons.gc;

import java.util.Enumeration;
import java.util.Iterator;

public abstract
class IntDictionary<V> {

    // интерфейс для хэш таблиц с примитивом int в качестве индекса

    /**
     * Sole constructor.  (For invocation by subclass constructors, typically
     * implicit.)
     */
    public IntDictionary() {
    }

    /**
     * Returns the number of entries (distinct keys) in this dictionary.
     *
     * @return  the number of keys in this dictionary.
     */
    public abstract int size();

    /**
     * Tests if this dictionary maps no keys to value. The general contract
     * for the {@code isEmpty} method is that the result is true if and only
     * if this dictionary contains no entries.
     *
     * @return  {@code true} if this dictionary maps no keys to values;
     *          {@code false} otherwise.
     */
    public abstract boolean isEmpty();

    /**
     * Returns an enumeration of the keys in this dictionary. The general
     * contract for the keys method is that an {@code Enumeration} object
     * is returned that will generate all the keys for which this dictionary
     * contains entries.
     *
     * @return  an enumeration of the keys in this dictionary.
     * @see     java.util.Dictionary#elements()
     * @see     java.util.Enumeration
     */
    public abstract Enumeration<Integer> keys();

    /**
     * Returns an enumeration of the values in this dictionary. The general
     * contract for the {@code elements} method is that an
     * {@code Enumeration} is returned that will generate all the elements
     * contained in entries in this dictionary.
     *
     * @return  an enumeration of the values in this dictionary.
     * @see     java.util.Dictionary#keys()
     * @see     java.util.Enumeration
     */
    public abstract Enumeration<V> elements();

    /**
     * Returns the value to which the key is mapped in this dictionary.
     * The general contract for the {@code isEmpty} method is that if this
     * dictionary contains an entry for the specified key, the associated
     * value is returned; otherwise, {@code null} is returned.
     *
     * @return  the value to which the key is mapped in this dictionary;
     * @param   key   a key in this dictionary.
     *          {@code null} if the key is not mapped to any value in
     *          this dictionary.
     * @exception NullPointerException if the {@code key} is {@code null}.
     * @see     java.util.Dictionary#put(java.lang.Object, java.lang.Object)
     */
    public abstract V get(int key);

    /**
     * Maps the specified {@code key} to the specified
     * {@code value} in this dictionary. Neither the key nor the
     * value can be {@code null}.
     * <p>
     * If this dictionary already contains an entry for the specified
     * {@code key}, the value already in this dictionary for that
     * {@code key} is returned, after modifying the entry to contain the
     *  new element. <p>If this dictionary does not already have an entry
     *  for the specified {@code key}, an entry is created for the
     *  specified {@code key} and {@code value}, and {@code null} is
     *  returned.
     * <p>
     * The {@code value} can be retrieved by calling the
     * {@code get} method with a {@code key} that is equal to
     * the original {@code key}.
     *
     * @param      key     the hashtable key.
     * @param      value   the value.
     * @return     the previous value to which the {@code key} was mapped
     *             in this dictionary, or {@code null} if the key did not
     *             have a previous mapping.
     * @exception  NullPointerException  if the {@code key} or
     *               {@code value} is {@code null}.
     * @see        java.lang.Object#equals(java.lang.Object)
     * @see        java.util.Dictionary#get(java.lang.Object)
     */
    public abstract V put(int key, V value);

    /**
     * Removes the {@code key} (and its corresponding
     * {@code value}) from this dictionary. This method does nothing
     * if the {@code key} is not in this dictionary.
     *
     * @param   key   the key that needs to be removed.
     * @return  the value to which the {@code key} had been mapped in this
     *          dictionary, or {@code null} if the key did not have a
     *          mapping.
     * @exception NullPointerException if {@code key} is {@code null}.
     */
    public abstract V remove(int key);

    // refreshes key of an object
    public abstract V change(int oldKey, int newKey);

    public abstract class Entry {
        protected int key;
        protected V value;
    }

    public abstract Iterator<Entry> getIterator();

    public abstract String toString();

}