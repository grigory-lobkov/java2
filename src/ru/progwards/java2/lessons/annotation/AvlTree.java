package ru.progwards.java2.lessons.annotation;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

// Official documentation
//https://docs.oracle.com/javase/9/javadoc/javadoc-command.htm#JSJAV-GUID-C27CE557-E5C6-4688-9FA5-9E9DE886A569
// Command line(generates docs in the current folder)
//javadoc src\main\java\app\Animal.java

// My variant
//C:\Users\Grigory\IdeaProjects\java2\src\ru\progwards\java2\lessons\annotation>javadoc AvlTree.java
// If command not found, find it yourself
//C:\Users\Grigory\IdeaProjects\java2\src\ru\progwards\java2\lessons\annotation>"C:\Program Files\Java\jdk-13.0.1\bin\javadoc" AvlTree.java
// If encoding is not pretty well, use
//C:\Users\Grigory\IdeaProjects\java2\src\ru\progwards\java2\lessons\annotation>"C:\Program Files\Java\jdk-13.0.1\bin\javadoc" -encoding UTF-8 AvlTree.java

/**
 * AVL tree (named after inventors Adelson-Velsky and Landis) is a self-balancing binary search tree.
 * It was the first such data structure to be invented.[2] In an AVL tree, the heights of the two child
 * subtrees of any node differ by at most one; if at any time they differ by more than one, rebalancing
 * is done to restore this property.
 *
 * Lookup, insertion, and deletion all take O(log n) time in both the average and worst cases, where
 * "n" is the number of nodes in the tree prior to the operation. Insertions and deletions may require
 * the tree to be rebalanced by one or more tree rotations.
 *
 * @author Valery Mazneff
 * @version 0.1b
*/
public class AvlTree<K extends Comparable<K>, V> implements Map<K,V> {



    /**
     * Returns a Set view of the mappings contained in this map
     * @return a set view of the mappings contained in this map
     */
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return null;
    }


    // Text messages for errors
    private static final String KEYEXIST = "Key already exist";
    private static final String KEYNOTEXIST = "Key not exist";

    /**
     * A map entry (key-value pair)
     */
    class Entry<KK extends Comparable<KK>, VV> implements Map.Entry<KK, VV> {

        /** Kay object reference */
        KK key;

        /** Value object reference */
        VV value;

        /** Parent Entry reference */
        Entry parent;

        /** Left child Entry reference */
        Entry left;

        /** Right child Entry  reference */
        Entry right;

        /** Height of Entry inside tree map
         * The lowest Entries have height = 1
         * The empty Entries have height = 0
         * The above Entries have maximum height of it's children plus 1
         */
        int height;  // высота

        /**
         * Returns the key corresponding to this entry.
         * @return the key corresponding to this entry
         */
        public KK getKey() {
            return key;
        }

        /**
         * Returns the value corresponding to this entry.
         * @return the value corresponding to this entry
         */
        public VV getValue() {
            return value;
        }

        /**
         * Replaces the value corresponding to this entry with the specified
         * value (optional operation)
         *
         * @param value new value to be stored in this entry
         * @return old value corresponding to the entry
         */
        public VV setValue(VV value) {
            this.value = value;
            return value;
        }

        /**
         * Constructor - takes the initial parameters
         * @param key key of entry
         * @param value object, linked to entry
         */
        public Entry(KK key, VV value) {
            this.key = key;
            this.value = value;
            height = 1;
        }

        private Entry<KK, VV> find(KK key) {
            int cmp = key.compareTo(this.key);
            if (cmp > 0)
                if (right != null)
                    return right.find(key);
                else
                    return this;
            if (cmp < 0)
                if (left != null)
                    return left.find(key);
                else
                    return this;
            return this;
        }

        /**
         * Adding new Entry as a child to this one
         *
         * @param leaf entry to store
         * @throws TreeException raises if key already exists in tree
         */
        void add(Entry<KK, VV> leaf) throws TreeException {
            int cmp = leaf.key.compareTo(key);
            if (cmp == 0)
                throw new TreeException(KEYEXIST);
            if (cmp > 0) {
                right = leaf;
                leaf.parent = this;
            } else {
                left = leaf;
                leaf.parent = this;
            }
            fixheight(this);
        }


        /**
         * Deleting this entry from tree
         */
        void delete() {
            Entry node = null;
            if (left != null || right != null) {
                if (bfactor() > 0) {
                    node = left.findMaximum();
                } else {
                    node = right.findMinimum();
                }
                node.right = right;
                node.left = left;
                Entry par = node.parent;
                if (par.left == node) {
                    par.left = null;
                } else if (par.right == node) {
                    par.right = null;
                }
                par.parent = parent;
            }
            if (parent.left == this) {
                parent.left = node;
            } else if (parent.right == this) {
                parent.right = node;
            }
            balance(node);
        }

        private Entry<KK, VV> findMaximum() {
            Entry<KK, VV> node = this;
            while (node.right != null) {
                node = node.right;
            }
            return node;
        }

        private Entry<KK, VV> findMinimum() {
            Entry<KK, VV> node = this;
            while (node.left != null) {
                node = node.left;
            }
            return node;
        }

        /**
         * Generates visual representation of the Entry
         *
         * @return visual representation of the Entry
         */
        public String toString() {
            return "("+key.toString()+","+value + ") h=" + height + " b=" + bfactor();
        }

        /**
         * Generates visual representation of the Entry with subEntries
         *
         * @param prefix current spacinf from the starting element
         * @return full tree from current Entry
         */
        public String toStringAll(String prefix) {
            return this.toString()
                    + (right == null ? "" : "\n" + prefix + "R: " + right.toStringAll(prefix + "⁞  "))
                    + (left == null ? "" : "\n" + prefix + "L: " + left.toStringAll(prefix + "⁞  "));
        }

        /**
         * Makes some action with all leafs from the smallest to the highest in the ascending order
         *
         * @param consumer procedure to act. Procedure have income variable of Entry type
         */
        public void process(Consumer<Entry<KK, VV>> consumer) {
            if (left != null)
                left.process(consumer);
            consumer.accept(this);
            if (right != null)
                right.process(consumer);
        }

        /**
         * Returns balance - as a difference between right and left Entry
         * @return difference
         */
        int bfactor() {
            return height(right) - height(left);
        }

        /**
         * Executes fixheight() for given Entry
         * @see Entry#fixheight()
         * @param leaf entry to fix it's height
         */
        void fixheight(Entry<KK, VV> leaf) {
            leaf.fixheight();
        }

        /**
         * Recalculates Entry height and stores it inside.
         * Calculates as the maximum of left and right element heights plus 1
         * @see Entry#left
         * @see Entry#right
         * @see Entry#height
         * @see Entry#height(Entry)
         */
        void fixheight() {
            int h1 = height(this.left);
            int h2 = height(this.right);
            this.height = (h1 > h2 ? h1 : h2) + 1;
        }

        /**
         * Returns height of any Entry (including {@code null})
         * @param leaf Entry to search height in
         * @see Entry#height
         * @return height of Entry
         */
        int height(Entry<K, V> leaf) {
            return leaf == null ? 0 : leaf.height;
        }

    }

    /**
     * The top Entry of the tree
     * All the searching starts from this element.
     * Is the only one link to the tree.
     */
    protected Entry<K, V> root;

    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this map contains no mapping for the key.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or
     *         {@code null} if this map contains no mapping for the key
     */
    public V get(K key) {
        Entry<K, V> p = root;
        while (p != null) {
            int cmp = key.compareTo(p.key);
            if (cmp < 0)
                p = p.left;
            else if (cmp > 0)
                p = p.right;
            else
                return p.value;
        }
        return null;
    }

    /**
     * Adds the given Entry {@code e} to the subtree of Entry {@code p}
     *
     * @param p where to add Entry {@code e}
     * @param e what to add to the Entry {@code p}
     * @return the added Entry {@code p}
     */
    private Entry<K, V> insert(Entry<K, V> p, Entry<K, V> e) {
        if (p == null) return e;
        if (e.key.compareTo(p.key) < 0)
            p.left = insert(p.left, e);
        else
            p.right = insert(p.right, e);
        return balance(p);
    }

    /**
     * Adds the given Entry {@code p} to the {@code root} element
     * @see AvlTree#root
     *
     * @param p Entry to add to the tree
     * @return the same {@code p}
     */
    public Entry<K, V> put(Entry<K, V> p) {
        if (root == null)
            root = p;
        else
            root = insert(root, p);
        return p;
    }

    /**
     * Make element {@code leaf} balanced
     * Calculating balance factor and make rotations
     * @see Entry#bfactor()
     * @see AvlTree#rotateleft(Entry)
     * @see AvlTree#rotateright(Entry)
     *
     * @param leaf Entry to balance
     * @return the same {@code leaf}
     */
    private Entry<K, V> balance(Entry<K, V> leaf) {
        leaf.fixheight();
        int bf = leaf.bfactor();
        //System.out.println("\nbalance(" + leaf.key + ") bf=" + bf);
        if (bf == 2) {
            if (leaf.right.bfactor() < 0)
                leaf.right = rotateright(leaf.right);
            return rotateleft(leaf);
        } else if (bf == -2) {
            if (leaf.left.bfactor() > 0)
                leaf.left = rotateleft(leaf.left);
            return rotateright(leaf);
        }
        return leaf;
    }

    /**
     * Find minimal {@code key} in Entry {@code p} subtree
     * @see Entry#key
     *
     * @param p Entry to search minimal in
     * @return minimal Entry
     */
    Entry<K, V> findmin(Entry<K, V> p) // поиск узла с минимальным ключом в дереве p
    {
        return p.left != null ? findmin(p.left) : p;
    }

    /**
     * Remove Entry with minimal {@code key} in Entry {@code p} subtree
     * @see Entry#key
     *
     * @param p Entry to search minimal in
     * @return balanced top of the same tree
     */
    Entry<K, V> removemin(Entry<K, V> p) // удаление узла с минимальным ключом из дерева p
    {
        if (p.left == null)
            return p.right;
        p.left = removemin(p.left);
        return balance(p);
    }

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @throws UnsupportedOperationException always
     * @exclude
     */
    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns {@code true} if this map contains no key-value mappings.
     *
     * @throws UnsupportedOperationException always
     * @exclude
     */
    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns {@code true} if this map contains a mapping for the specified key
     *
     * @param key key whose presence in this map is to be tested
     * @return {@code true} if this map contains a mapping for the specified
     *         key
     * @throws UnsupportedOperationException always
     * @exclude
     */
    @Override
    public boolean containsKey(Object key) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns {@code true} if this map maps one or more keys to the specified value
     *
     * @param value value whose presence in this map is to be tested
     * @return {@code true} if this map maps one or more keys to the
     *         specified value
     * @throws UnsupportedOperationException always
     * @exclude
     */
    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }
    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this map contains no mapping for the key.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or
     *         {@code null} if this map contains no mapping for the key
     */
    @Override
    public V get(Object key) {
        return get((K)key);
    }

    /**
     * Associates the specified value with the specified key in this map
     * (optional operation).  If the map previously contained a mapping for
     * the key, the old value is replaced by the specified value.  (A map
     * {@code m} is said to contain a mapping for a key {@code k} if and only
     * if {@link #containsKey(Object) m.containsKey(k)} would return
     * {@code true}.)
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with {@code key}, or
     *         {@code null} if there was no mapping for {@code key}.
     *         (A {@code null} return can also indicate that the map
     *         previously associated {@code null} with {@code key},
     *         if the implementation supports {@code null} values.)
     */
    public V put(K key, V value) {
        Entry<K,V> e = put(new Entry<>(key, value));
        return e.value;
    }

    /**
     * Removes the mapping for a key from this map if it is present
     * (optional operation)
     *
     * <p>Returns the value to which this map previously associated the key,
     * or {@code null} if the map contained no mapping for the key.
     *
     * <p>The map will not contain a mapping for the specified key once the
     * call returns.
     *
     * @param key key whose mapping is to be removed from the map
     * @return the previous value associated with {@code key}, or
     *         {@code null} if there was no mapping for {@code key}.
     */
    @Override
    public V remove(Object key) {
        try {
            Entry<K,V> e = remove(root, (K)key);
            return e.value;
        } catch (TreeException e1) {
            e1.printStackTrace();
        }
        return null;
    }

    /**
     * Copies all of the mappings from the specified map to this map
     * (optional operation).  The effect of this call is equivalent to that
     * of calling {@link #put(Object,Object) put(k, v)} on this map once
     * for each mapping from key {@code k} to value {@code v} in the
     * specified map.  The behavior of this operation is undefined if the
     * specified map is modified while the operation is in progress.
     *
     * @param m mappings to be stored in this map
     * @throws UnsupportedOperationException always
     * @exclude
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes all of the mappings from this map (optional operation).
     * The map will be empty after this call returns.
     *
     * @throws UnsupportedOperationException always
     * @exclude
     */
    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a Set view of the keys contained in this map.
     *
     * @return a set view of the keys contained in this map
     * @throws UnsupportedOperationException always
     * @exclude
     */
    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a Collection view of the values contained in this map.
     *
     * @return a set view of the keys contained in this map
     * @throws UnsupportedOperationException always
     * @exclude
     */
    @Override
    public Collection<V> values() {
        throw new UnsupportedOperationException();
    }

    /**
     * Remove element from the tree by key. Recursive
     *
     * @param p Entry to search for key in
     * @param k Key to search to remove Entry
     * @return a set view of the keys contained in this map
     * @throws TreeException when tree is empty
     */
    Entry<K, V> remove(Entry<K, V> p, K k) throws TreeException {
        if (root == null)
            throw new TreeException(KEYNOTEXIST);

        int cmp = k.compareTo(p.key);
        if (cmp < 0)
            p.left = remove(p.left, k);
        else if (cmp > 0)
            p.right = remove(p.right, k);
        else { // cmp = 0, found!
            Entry<K, V> q = p.left;
            Entry<K, V> r = p.right;
            //delete p;
            if (r == null) return q;
            Entry<K, V> min = findmin(r);
            min.right = removemin(r);
            min.left = q;
            return balance(min);
        }
        return balance(p);
    }

    /**
     * Replaces the key from {@code oldKey} to {@code newKey}
     *
     * @param oldKey key to search
     * @param newKey key to replace with
     * @throws TreeException when tree is empty
     */
    public void change(K oldKey, K newKey) throws TreeException {
        Entry<K, V> current = remove(root, oldKey);
        current.key = newKey;
        put(current);
    }

    /**
     * Makes some action with all Entries from the smallest to the highest in the ascending order from the root
     *
     * @see AvlTree#root
     * @see Entry#process(Consumer)
     * @param consumer procedure to act. Procedure have income variable of {@code Entry} type
     */
    public void process(Consumer<Entry<K, V>> consumer) {
        if (root != null)
            root.process(consumer);
    }

    /**
     * Make right rotation of tree element
     *
     * @param p subtree head element to rotate in
     * @return new head element of subtree
     */
    private Entry<K, V> rotateright(Entry<K, V> p) // правый поворот вокруг p
    {
        Entry<K, V> q = p.left;
        p.left = q.right;
        q.right = p;
        p.fixheight();
        q.fixheight();
        return q;
    }

    /**
     * Make left rotation of tree element
     *
     * @param q subtree head element to rotate in
     * @return new head element of subtree
     */
    private Entry<K, V> rotateleft(Entry<K, V> q) // левый поворот вокруг q
    {
        Entry<K, V> p = q.right;
        q.right = p.left;
        p.left = q;
        q.fixheight();
        p.fixheight();
        return p;
    }

    /**
     * Test tree generation
     *
     * @param args no matter
     * @throws TreeException never
     * @exclude
     */
    public static void main(String[] args) throws TreeException {
        AvlTree<Integer, String> t = new AvlTree();
        t.put(45, "");
        t.put(4, "");
        t.put(5, "");
        t.put(9, "");
        t.put(8, "");
        t.put(6, "");
        t.put(7, "");
        t.put(56, "");
        t.put(50, "");
        t.put(3, "");
        t.put(10, "");
        t.put(60, "");
        System.out.println(t.root.toStringAll(""));
    }
}

/**
 * Tree processing exception
 */
class TreeException extends Exception {
    public TreeException(String message) {
        super(message);
    }
}

/*
Loading source file AvlTree.java...
Constructing Javadoc information...
Standard Doclet version 13.0.1
Building tree for all the packages and classes...
Generating .\ru\progwards\java2\lessons\annotation\AvlTree.html...
AvlTree.java:392: error: unknown tag: exclude
     * @exclude
       ^
AvlTree.java:403: error: unknown tag: exclude
     * @exclude
       ^
AvlTree.java:417: error: unknown tag: exclude
     * @exclude
       ^
AvlTree.java:431: error: unknown tag: exclude
     * @exclude
       ^
AvlTree.java:506: error: unknown tag: exclude
     * @exclude
       ^
AvlTree.java:518: error: unknown tag: exclude
     * @exclude
       ^
AvlTree.java:530: error: unknown tag: exclude
     * @exclude
       ^
AvlTree.java:542: error: unknown tag: exclude
     * @exclude
       ^
AvlTree.java:641: error: unknown tag: exclude
     * @exclude
       ^
Generating .\ru\progwards\java2\lessons\annotation\package-summary.html...
Generating .\ru\progwards\java2\lessons\annotation\package-tree.html...
Generating .\constant-values.html...
Building index for all the packages and classes...
Generating .\overview-tree.html...
Generating .\deprecated-list.html...
Generating .\index-all.html...
Building index for all classes...
Generating .\allclasses-index.html...
Generating .\allpackages-index.html...
Generating .\index.html...
Generating .\help-doc.html...
9 errors
 */