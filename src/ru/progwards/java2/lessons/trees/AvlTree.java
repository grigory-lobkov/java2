package ru.progwards.java2.lessons.trees;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/*
Реализовать класс AvlTree - АВЛ дерево, с методами:
        2.1 public void put(KK key, VV value) - добавить пару ключ-значение, если уже такой ключ есть - заменить
        2.2 public void delete(KK key) - удалить ключ
        2.3 public VV find(KK key) - найти ключ
        2.4 public void change(KK oldKey, KK newKey) - заменить значение ключа на другое
        2.5 public void process(Consumer<Entry<KK,VV>> consumer) - прямой обход дерева
*/
public class AvlTree<K extends Comparable<K>, V> implements Map<K,V> {
    private static final String KEYEXIST = "Key already exist";
    private static final String KEYNOTEXIST = "Key not exist";

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    class Entry<KK extends Comparable<KK>, VV> {
        KK key;
        VV value;
        Entry parent;
        Entry left;
        Entry right;
        int height;  // высота
        //int balance; // баланс

        public KK getKey() {
            return key;
        }

        public VV getValue() {
            return value;
        }

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

        public String toString() {
            //return "(" + key + "," + value + ")";
            return key.toString() + " h=" + height + " b=" + bfactor();
        }

        public String toStringAll(String prefix) {
            return this.toString()
                    + (right == null ? "" : "\n" + prefix + "R: " + right.toStringAll(prefix + "⁞  "))
                    + (left == null ? "" : "\n" + prefix + "L: " + left.toStringAll(prefix + "⁞  "));
        }

        public void process(Consumer<Entry<KK, VV>> consumer) {
            if (left != null)
                left.process(consumer);
            consumer.accept(this);
            if (right != null)
                right.process(consumer);
        }

        int bfactor() {
            return treeHeight(left) - treeHeight(right);
        }

        void fixheight(Entry<KK, VV> leaf) {
            leaf.fixheight();
        }

        void fixheight() {
            //leaf.height = Math.max(leaf.recalcHeight(leaf.left), leaf.recalcHeight(leaf.right)) + 1;
            int h1 = treeHeight(this.left);
            int h2 = treeHeight(this.right);
            this.height = (h1 > h2 ? h1 : h2) + 1;
            //System.out.println("\nfixheight("+this.key+") h="+this.height);
            //System.out.println(root.toStringAll(""));
        }
        /*
        private void rotateSmallRight() {
            System.out.println(root.toStringAll(""));
            System.out.println("rotateSmallRight(" + key + ")");
            Entry<K, V> b = left;
            Entry<K, V> c = b.right;
            left = c;
            b.right = this;
            b.parent = parent;
            if (parent == null)
                root = b;
            parent = b;
            c.parent = this;
            b.fixheight();
            c.fixheight();
            System.out.println(root.toStringAll(""));
        }

        private void rotateSmallLeft() {
            System.out.println(root.toStringAll(""));
            System.out.println("rotateSmallLeft(" + key + ")");
            Entry<K, V> b = right;
            Entry<K, V> c = b.left;
            right = c;
            b.left = this;
            b.parent = parent;
            parent = b;
            c.parent = this;
            b.fixheight();
            c.fixheight();
            System.out.println(root.toStringAll(""));
        }

        private void rotateBigRight() {
            System.out.println(root.toStringAll(""));
            System.out.println("rotateBigRight(" + key + ")");
            Entry<K, V> b = left;
            Entry<K, V> c = b.right;
            Entry<K, V> n = c.right;
            Entry<K, V> m = c.left;
            left = n;
            b.right = m;
            c.right = this;
            c.left = b;
            c.parent = parent;
            if (parent == null)
                root = c;
            parent = c;
            b.parent = c;
            if (n != null)
                n.parent = this;
            if (m != null)
                m.parent = b;
            System.out.println(root.toStringAll(""));
        }

        private void rotateBigLeft() {
            System.out.println(root.toStringAll(""));
            System.out.println("rotateBigLeft(" + key + ")");
            Entry<KK, VV> b = right;
            Entry<KK, VV> c = b.left;
            Entry<KK, VV> n = c.left;
            Entry<KK, VV> m = c.right;
            right = n;
            b.left = m;
            c.left = this;
            c.right = b;
            c.parent = parent;
            parent = c;
            b.parent = c;
            n.parent = this;
            m.parent = b;
            System.out.println(root.toStringAll(""));
        }
        */
        private Entry<K, V> rotateright() // правый поворот вокруг p
        {
            Entry<K, V> q = this.left;
            this.left = q.right;
            q.right = this;
            this.fixheight();
            q.fixheight();
            return q;
        }

        private Entry<K, V> rotateleft() // левый поворот вокруг q
        {
            Entry<K, V> p = this.right;
            this.right = p.left;
            p.left = this;
            this.fixheight();
            p.fixheight();
            return p;
        }
    }

    protected Entry<K, V> root;

    public V find(K key) {
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

    public Entry<K, V> put(Entry<K, V> leaf) {
        if (root == null)
            root = leaf;
        else {
            try {
                root.find(leaf.key).add(leaf);
            } catch (TreeException e) {
                return null;
            }
        }
        /*if(leaf.parent!=null) {
            balance(leaf.parent);
            if(leaf.parent!=null && leaf.parent.parent!=null) {
                balance(leaf.parent.parent);
            }
        }*/
        //balance(leaf);
        //balance(root);
        if (leaf.parent != null && leaf.parent.parent != null) {
            balance(leaf.parent.parent);
        }
        return leaf;
    }

    /*    private void balance(Entry<K, V> leaf) {
            leaf.fixheight(leaf);
            int bf = leaf.bfactor();
            System.out.println("\nbalance("+leaf.key+") bf="+bf);
            if (bf == 2) {
                //if (leaf.right!=null && leaf.right.bfactor() < 0)
                //    leaf.right.rotateSmallRight();
                //leaf.rotateSmallLeft();
                //leaf.rotateBigRight();
                Entry<K, V> b = leaf.left;
                if(b!=null) {
                    if (b.bfactor() < 0) {
                        leaf.rotateBigRight();
                        //leaf.rotateSmallRight(); // когда (высота b-поддерева — высота R) = 2 и высота С <= высота L.
                    } else {
                        //leaf.rotateBigRight();
                    }
                }
            } else if (bf == -2) {
                //if (leaf.left!=null && leaf.left.bfactor() > 0)
                //   leaf.left.rotateSmallLeft();
                //leaf.rotateSmallRight();
                //leaf.rotateBigLeft();
                Entry<K, V> b = leaf.right;
                if(b!=null) {
                    if (b.bfactor() >= 0) {
                        //leaf.rotateSmallLeft(); // когда (высота b-поддерева — высота R) = 2 и высота С <= высота L.
                    } else {
                        //leaf.rotateBigLeft();
                    }
                }
            }
        }*/
    private Entry<K, V> balance(Entry<K, V> leaf) {
        leaf.fixheight();
        int bf = leaf.bfactor();
        //System.out.println("\nbalance(" + leaf.key + ") bf=" + bf);
        if (bf == 2) {
            if (leaf.right.bfactor() < 0)
                leaf.right = leaf.right.rotateright();
            return leaf.rotateleft();
        } else if (bf == -2) {
            if (leaf.left.bfactor() > 0)
                leaf.left = leaf.left.rotateleft();
            return leaf.rotateright();
        }
        return leaf;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public V get(Object key) {
        return null;
    }

    public V put(K key, V value) {
        put(new Entry<>(key, value));
        return value;
    }

    @Override
    public V remove(Object key) {
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {

    }

    @Override
    public void clear() {

    }

    @Override
    public Set<K> keySet() {
        return null;
    }

    @Override
    public Collection<V> values() {
        return null;
    }

    public void delete(K key) throws TreeException {
        internaldDelete(key);
    }

    int treeHeight(Entry<K, V> leaf) {
        return leaf == null ? 0 : leaf.height;
    }

    public Entry<K, V> internaldDelete(K key) throws TreeException {
        if (root == null)
            throw new TreeException(KEYNOTEXIST);

        Entry found = root.find(key);
        int cmp = found.key.compareTo(key);
        if (cmp != 0)
            throw new TreeException(KEYNOTEXIST);
        if (found.parent == null) {
            if (found.right != null) {
                root = found.right;
                if (found.left != null)
                    put(found.left);
            } else if (found.left != null)
                root = found.left;
            else
                root = null;
        } else
            found.delete();
        return found;
    }

    public void change(K oldKey, K newKey) throws TreeException {
        Entry<K, V> current = internaldDelete(oldKey);
        current.key = newKey;
        put(current);
    }

    public void process(Consumer<Entry<K, V>> consumer) {
        if (root != null)
            root.process(consumer);
    }

    /*public TreeIterator<KK, VV> getIterator() {
        return new TreeIterator(this);
    }*/

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