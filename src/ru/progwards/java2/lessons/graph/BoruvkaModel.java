package ru.progwards.java2.lessons.graph;

import java.util.Comparator;
import java.util.List;

/**
 * Основные компоненты для хранения графа
 */
public class BoruvkaModel {

    /**
     * Возможные состояния объекта в процессе поиска
     * Фактически используется всего два(NOT_USED, IN_USE), т.к. обработка не параллельная
     */
    enum Status {
        /**
         * Не используется (белая вершина в алгоритме Тарьяна)
         */
        NOT_USED,
        /**
         * Посещён (серая вершина в алгоритме Тарьяна)
         */
        VISITED,
        /**
         * Используется (черная вершина в алгоритме Тарьяна)
         */
        IN_USE,
        /**
         * Удаление данного графа
         */
        DELETE
    }

    /**
     * Вершины графа
     *
     * @param <NodeType> объект, подвязанный к вершине
     * @param <EdgeType> объект, подвязанный к дуге графа
     */

    static class Node<NodeType, EdgeType> {
        NodeType info; // информация об узле
        List<Edge<NodeType, EdgeType>> in; // массив входящих ребер
        List<Edge<NodeType, EdgeType>> out; // массив исходящих ребер
        Status status; // состояние узла (пометка для алгоритма)
        Graph<NodeType, EdgeType> graph; // в каком оставном дереве находится данный узел (пометка для алгоритма)

        public Node(NodeType info) {
            this.info = info;
        }

        @Override
        public String toString() {
            return "node{" + info + '}';
        }
    }

    /**
     * Дуги графа
     *
     * @param <NodeType> объект, подвязанный к вершине
     * @param <EdgeType> объект, подвязанный к дуге графа
     */
    static class Edge<NodeType, EdgeType> {
        EdgeType info; // информация о ребре
        Node<NodeType, EdgeType> out; // вершина, из которой исходит ребро
        Node<NodeType, EdgeType> in; // вершина, в которую можно попасть
        // по этому ребру
        double weight; // стоимость перехода
        int id; // идентификатор узла (уникальный ключ) (пометка для алгоритма)
        boolean processed; // была ли проанализирована дуга

        public Edge(EdgeType info, Node<NodeType, EdgeType> out, Node<NodeType, EdgeType> in, double weight) {
            this.info = info;
            this.out = out;
            this.in = in;
            this.weight = weight;
        }

        @Override
        public String toString() {
            return "edge{info=" + info + ", weight=" + weight + "}";
        }

        /**
         * Получить узел дуги, отличный от входящего узла (не используется)
         *
         * @param one
         * @return
         */
        public Node<NodeType, EdgeType> Other(Node<NodeType, EdgeType> one) {
            return one == in ? out : in;
        }

        /**
         * Получить узел дуги, не принадлежащий графу
         *
         * @param graph граф, с которым в данное ремя работаем
         * @return узел дуги, не принадлежащий графу {@code graph}
         */
        public Node<NodeType, EdgeType> Other(Graph<NodeType, EdgeType> graph) {
            return out.graph != graph ? out : (in.graph != graph ? in : null);
        }
    }

    /**
     * Граф (вершины и дуги)
     *
     * @param <NodeType> объект, подвязанный к вершине
     * @param <EdgeType> объект, подвязанный к дуге графа
     */
    static class Graph<NodeType, EdgeType> {
        List<Node<NodeType, EdgeType>> nodes;
        List<Edge<NodeType, EdgeType>> edges;
        Status status; // состояние узла (пометка для алгоритма)
        Thread t; // ссылка на рассчитывающий поток
        int id; // идентификатор узла (уникальный ключ) (пометка для алгоритма)

        public Graph(List<Node<NodeType, EdgeType>> nodes, List<Edge<NodeType, EdgeType>> edges) {
            this.nodes = nodes;
            this.edges = edges;
        }
    }

    /**
     * Компаратор для выстраивания дерева по возрастанию веса дуг
     */
    static Comparator<Edge> edgesComparator = (e1, e2) -> {
        if (e1.weight != e2.weight)
            return e1.weight < e2.weight ? -1 : 1;
        return e1.id == e2.id ? 0 : (e1.id < e2.id ? -1 : 1);
    };

}
