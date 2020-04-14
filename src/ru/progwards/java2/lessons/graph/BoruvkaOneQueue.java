package ru.progwards.java2.lessons.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import static ru.progwards.java2.lessons.graph.BoruvkaModel.*;
import static ru.progwards.java2.lessons.graph.BoruvkaModel.Status.*;

/**
 * Нахождение минимального оставного дерева по алгоритму Борувки
 *
 * Данный алгоритм просто находит рёбра наименьшего веса, чтобы коснуться всех вершин - он не генерирует связный граф
 * Алгоритм ошибочен
 */
public class BoruvkaOneQueue<N, E> implements IBoruvka<N, E> {

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
     * @see BoruvkaOneQueue#takeOuts
     */
    final boolean takeIns = true;

    /**
     * Выводить ли отладочную информацию в процессе обработки
     */
    final boolean writeDebugInfo = false;

    /**
     * Вычисляет минимальное остовное дерево в виде списка дуг графа
     *
     * @param graph
     * @param <NodeType> Тип "Узел"
     * @param <EdgeType> Тип "Ребро"
     * @return минимальное остовное дерево в виде списка дуг графа
     */
    static <NodeType, EdgeType> List<Edge<NodeType, EdgeType>> minTree(Graph<NodeType, EdgeType> graph) {
        BoruvkaOneQueue<NodeType, EdgeType> alg = new BoruvkaOneQueue<NodeType, EdgeType>();
        return alg.getMinEdgeTree(graph);
    }
    
    /**
     * Вычислить дуги, требующиеся для построения минимального остовного дерева
     *
     * @return список дуг, требующихся для построения минимального остовного дерева
     */
    public List<Edge<N, E>> getMinEdgeTree(Graph<N, E> graph) {
        List<Edge<N, E>> result = new ArrayList<Edge<N, E>>(graph.nodes.size());

        // генерация оставных деревьев
        for (Node n : graph.nodes) {
            n.status = NOT_USED; // белый
            n.graph = null;
        }
        // проидентифицируем дуги
        int id = 0;
        for (Edge e : graph.edges)
            e.id = id++;

        Find(result, graph);
        return result;
    }

    /**
     * Генерация оставного леса из дерева подсоединением других оставных лесов
     * В первую очередь соединяемся по дугам с минимальным весом
     *
     * @param result список дуг оставного леса (пополняется)
     * @param g      оставной лес, который анализируем сейчас (растёт по мере объединения)
     */
    private void Find(List<Edge<N, E>> result, Graph<N, E> g) {
        if (writeDebugInfo) System.out.println("Find()");

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
            Node<N, E> n = e.Other(g); // узел не в графе
            if (n != null) {
                if (writeDebugInfo) System.out.println("Add "+e);
                n.graph = g;
                n.status = IN_USE;
                n = e.Other(n);
                if(n!=null) {
                    n.graph = g;
                    n.status = IN_USE;
                }
                result.add(e);
            }
        }
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
