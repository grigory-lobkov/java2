package ru.progwards.java2.lessons.graph;

import java.util.List;

/**
 * Нахождение минимального оставного дерева по алгоритму Борувки
 */
public class Boruvka {

    //Граф задан в виде ссылочной структуры:

    static class Node<NodeType, EdgeType> {
        NodeType info; // информация об узле
        List<Edge<NodeType, EdgeType>> in; // массив входящих ребер
        List<Edge<NodeType, EdgeType>> out; // массив исходящих ребер
    }

    static class Edge<NodeType, EdgeType> {
        EdgeType info; // информация о ребре
        Node<NodeType, EdgeType> out; // вершина, из которой исходит ребро
        Node<NodeType, EdgeType> in; // вершина, в которую можно попасть
        // по этому ребру
        double weight; // стоимость перехода
    }

    static class Graph<NodeType, EdgeType> {
        List<Node<NodeType, EdgeType>> nodes;
        List<Edge<NodeType, EdgeType>> edges;
    }

    /**
     * Вычисляет минимальное остовное дерево в виде списка дуг графа
     * @param graph
     * @param <N> Тип "Узел"
     * @param <E> Тип "Ребро"
     * @return минимальное остовное дерево в виде списка дуг графа
     */
    static <N, E> List<Boruvka.Edge> minTree(Graph<N, E> graph) {
        return null;
    }
}
