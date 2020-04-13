package ru.progwards.java2.lessons.graph;

import java.util.*;

import static ru.progwards.java2.lessons.graph.Boruvka.Status.*;

/**
 * Нахождение минимального оставного дерева по алгоритму Борувки
 */
public class Boruvka<N, E> {

    /**
     * Брать ли дуги, исходящие из узла
     * Получится, мы сможем из какой-то одной вершины пройти до всех остальным по исходящим дугам
     */
    final boolean takeOuts = true;

    /**
     * Брать ли дуги, входящие в узел
     * Если будет установлен {@code takeOuts} и {@code takeIns} то может не найтись вершины, из готорой сможем обойти весь граф по стрелкам
     * Если оба параметра будут {@code false}, то дуги найдены не будут
     * Если {@true} имеет только один параметр, очень важно, с какой веришины начинаем обход(от расположения в графе), т.к. дерево может получиться не минимальным
     *
     * @see Boruvka#takeOuts
     */
    final boolean takeIns = true;

    /**
     * Выводить ли отладочную информацию в процессе обработки
     */
    final boolean writeDebugInfo = false;

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
        IN_USE
    }

    ;

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
            return "Node{" + info + '}';
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
            return "Edge{info=" + info + ", weight=" + weight + "}";
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

        public Graph(List<Node<NodeType, EdgeType>> nodes, List<Edge<NodeType, EdgeType>> edges) {
            this.nodes = nodes;
            this.edges = edges;
        }
    }

    /**
     * Вычисляет минимальное остовное дерево в виде списка дуг графа
     *
     * @param graph
     * @param <NodeType> Тип "Узел"
     * @param <EdgeType> Тип "Ребро"
     * @return минимальное остовное дерево в виде списка дуг графа
     */
    static <NodeType, EdgeType> List<Edge<NodeType, EdgeType>> minTree(Graph<NodeType, EdgeType> graph) {
        Boruvka<NodeType, EdgeType> alg = new Boruvka<NodeType, EdgeType>();
        return alg.getMinEdgeTree(graph);
    }

    /**
     * Вычислить дуги, требующиеся для построения минимального остовного дерева
     *
     * @return список дуг, требующихся для построения минимального остовного дерева
     */
    private List<Edge<N, E>> getMinEdgeTree(Graph<N, E> graph) {
        List<Edge<N, E>> result = new ArrayList<Edge<N, E>>(graph.nodes.size());
        ArrayList<Graph<N, E>> graphs = new ArrayList<Graph<N, E>>(graph.nodes.size());
        //Runnable[] runnables = new Runnable[graph.nodes.size()];
        //Thread[] threads = new Thread[graph.nodes.size()];

        // генерация оставных деревьев
        int id = 0;
        for (Node n : graph.nodes) {
            n.status = NOT_USED; // белый
            Graph<N, E> g = new Graph<N, E>(new ArrayList<Node<N, E>>(), List.of());
            n.graph = g;
            g.nodes.add(n);
            graphs.add(g);
        }
        // проидентифицируем дуги
        id = 0;
        for (Edge e : graph.edges)
            e.id = id++;

        /*int j = 0;
        for (Node n : graph.nodes) {
            runnables[j] = new ThreadDFS(n);
            threads[j] = new Thread(runnables[j]);
            j++;
        }
        for (int i =0; i<threads.length; i++)
            try {
                threads[i].join();
                result.addAll(((ThreadDFS)runnables[i]).result);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        // не знаю, как перевести на потоки, т.к. понадобится прибивать поток, который рассчитывает подсоединяемый лес, либо свой собственный - гемор :)
        for (Graph<N, E> g : graphs)
            if (checkGraphNotUsed(g))
                Find(result, g);
        return result;
    }

    /**
     * Проверить, рассчитаны ли какие-либо элементы из заданного графа
     *
     * @param g проверяемый граф
     * @return Истина, если ни один из элементов графа не рассчитывался
     */
    private boolean checkGraphNotUsed(Graph<N, E> g) {
        for (Node n : g.nodes)
            if (n.status != NOT_USED)
                return false;
        return g.nodes.size() > 0;
    }

    /*class ThreadDFS implements Runnable {
        Graph graph;
        ArrayList<Edge<N, E>> result;
        public ThreadDFS(Node startNode) {
            this.startNode = startNode;
        }
        @Override
        public void run() {
            if(startNode.status == NOT_USED)
                if(checkGraphNotUsed(graph))
                    Find(result, graph);
        }
    }*/

    /**
     * Компаратор для выстраивания дерева по возрастанию веса дуг
     */
    Comparator<Edge<N, E>> edgesComparator = (e1, e2) -> {
        if (e1.weight != e2.weight)
            return e1.weight < e2.weight ? -1 : 1;
        return e1.id == e2.id ? 0 : (e1.id < e2.id ? -1 : 1);
    };

    /**
     * Генерация оставного леса из дерева подсоединением других оставных лесов
     * В первую очередь соединяемся по дугам с минимальным весом
     *
     * @param result список дуг оставного леса (пополняется)
     * @param g      оставной лес, который анализируем сейчас (растёт по мере объединения)
     */
    private void Find(List<Edge<N, E>> result, Graph<N, E> g) {
        Node baseNode = g.nodes.get(0);
        if (writeDebugInfo) System.out.println("Find(" + baseNode + ")");
        baseNode.status = VISITED; // серый

        // собираем дерево из исходящих дуг
        TreeSet<Edge<N, E>> referenced = new TreeSet<Edge<N, E>>(edgesComparator);
        for (Node<N, E> n : g.nodes) {
            if (takeOuts)
                for (Edge<N, E> e : n.out)
                    referenced.add(e);
            if (takeIns)
                for (Edge<N, E> e : n.in)
                    referenced.add(e);
        }

        // перебираем по порядку все дуги
        while (!referenced.isEmpty()) {
            if (writeDebugInfo) {
                System.out.print("References in queue: ");
                referenced.stream().forEach(System.out::print);
                System.out.println();
            }
            Edge<N, E> e = referenced.pollFirst();
            e.processed = true;
            Node<N, E> n = e.Other(g); // узел не в графе
            if (n != null) {
                Merge(g, n.graph, referenced);
                result.add(e);
            }
        }
        baseNode.status = IN_USE; // черный
    }

    /**
     * Объединяем два графа в один, который указан первым
     *
     * @param g1 граф-приёмник
     * @param g2 граф-источник (опустошаемый)
     */
    private void Merge(Graph<N, E> g1, Graph<N, E> g2, TreeSet<Edge<N, E>> referenced) {
        if (writeDebugInfo) System.out.println("Merge(" + g1.nodes.get(0) + "," + g2.nodes.get(0) + ")");
        for (Node<N, E> n : g2.nodes) {
            //boolean isNewRefs = n.status != IN_USE;
            // добавим новые дуги в сравнение, если узел не обработан
            if (n.status != IN_USE) {
                if (takeOuts)
                    for (Edge e : n.out)
                        if (!e.processed) {
                            referenced.add(e);
                            if (writeDebugInfo) System.out.print("  add " + e);
                        }
                if (takeIns)
                    for (Edge e : n.in)
                        if (!e.processed) {
                            referenced.add(e);
                            if (writeDebugInfo) System.out.print("  add " + e);
                        }
                if (writeDebugInfo) System.out.println();

                n.status = IN_USE;
            }
            // перенесем узел в первый граф
            g1.nodes.add(n);
            n.graph = g1;
        }
        // очистим граф-источник
        g2.nodes.clear();
    }

    /**
     * Пример обработки дерева из 7 узлов
     *
     * @param args не используется
     */
    public static void main(String[] args) {
        Node<String, String> A = new Node<>("A");
        Node<String, String> B = new Node<>("B");
        Node<String, String> C = new Node<>("C");
        Node<String, String> D = new Node<>("D");
        Node<String, String> E = new Node<>("E");
        Node<String, String> F = new Node<>("F");
        Node<String, String> G = new Node<>("G");
        Edge<String, String> e1 = new Edge<>("a-b", A, B, 0);
        Edge<String, String> e2 = new Edge<>("a-c", A, C, 2);
        Edge<String, String> e3 = new Edge<>("a-d", A, D, 7);
        Edge<String, String> e4 = new Edge<>("b-e", B, E, 10);
        Edge<String, String> e5 = new Edge<>("b-g", B, G, 5);
        Edge<String, String> e6 = new Edge<>("c-d", C, D, 5);
        Edge<String, String> e7 = new Edge<>("c-f", C, F, 1);
        Edge<String, String> e8 = new Edge<>("d-g", D, G, 0);
        Edge<String, String> e9 = new Edge<>("e-d", E, D, 0);
        Edge<String, String> e10 = new Edge<>("f-g", F, G, 4);
        Edge<String, String> e11 = new Edge<>("g-e", G, E, 0);
        A.out = List.of(e1, e2, e3);
        A.in = List.of();
        B.out = List.of(e4, e5);
        B.in = List.of(e1);
        C.out = List.of(e6, e7);
        C.in = List.of(e2);
        D.out = List.of(e8);
        D.in = List.of(e3, e6, e9);
        E.out = List.of(e9);
        E.in = List.of(e4, e11);
        F.out = List.of(e10);
        F.in = List.of(e7);
        G.out = List.of(e11);
        G.in = List.of(e5, e8, e10);
        Graph<String, String> graph = new Graph<>(
                List.of(F, E, C, B, D, A, G),
                List.of(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11)
        );
        List<Edge<String, String>> result = minTree(graph);
        System.out.println(result);
    }
}
/*
[Edge{info=c-f, weight=1.0},
 Edge{info=a-c, weight=2.0},
 Edge{info=a-b, weight=0.0},
 Edge{info=f-g, weight=4.0},
 Edge{info=d-g, weight=0.0},
 Edge{info=e-d, weight=0.0}]
*/
