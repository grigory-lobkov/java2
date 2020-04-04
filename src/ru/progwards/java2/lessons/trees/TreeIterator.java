package ru.progwards.java2.lessons.trees;

import javax.swing.text.html.HTMLDocument;
import java.util.*;

// Для обычного BinaryTree из примера в лекциях сделать итератор,
// который позволяет в обычном for получить прямой обход дерева.
// В самом дереве дополнительную информацию для этого хранить нельзя,
// все хранить только в итераторе. В BinaryTree добавить метод public TreeIterator<K,V> getIterator()

public class TreeIterator<K extends Comparable<K>, V> implements Iterator<BinaryTree.TreeLeaf> {
    BinaryTree<K, V> tree;
    Deque<BinaryTree.TreeLeaf> parents; // стэк родителей
    int parentNow;
    BinaryTree.TreeLeaf next;

    public TreeIterator(BinaryTree<K, V> tree) {
        this.tree = tree;
        next = tree.root;
        parentNow = -1;
        parents = new ArrayDeque<>();
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public BinaryTree.TreeLeaf next() {
        if (next == null) throw new NoSuchElementException("BinaryTree TreeIterator");
        BinaryTree.TreeLeaf result = next;

        if (result.left != null) {
            parents.offerLast(result);
            next = result.left;
        } else if (result.right != null) {
            parents.offerLast(result);
            next = result.right;
        } else {
            stepBack(); // возвращаемся обратно
        }
        return result;
    }

    private void stepBack() {
        BinaryTree.TreeLeaf parent = parents.peekLast();
        while (parent != null && (parent.right == next || parent.right==null)) {
            next = parents.pollLast();
            parent = parents.peekLast();
        }
        next = parent == null ? null : parent.right;
    }

    public static void main(String[] args) throws TreeException {
        BinaryTree<Integer, String> bt = new BinaryTree();
        bt.add(45,"");
        bt.add(4,"");
        bt.add(5,"");
        bt.add(9,"");
        bt.add(8,"");
        bt.add(6,"");
        bt.add(7,"");
        bt.add(56,"");
        bt.add(50,"");
        bt.add(3,"");
        bt.add(10,"");
        bt.add(60,"");
        Iterator i = bt.getIterator();
        while (i.hasNext()) {
            System.out.println(i.next());
        }
    }
}
