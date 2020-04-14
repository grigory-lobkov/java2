package ru.progwards.java2.lessons.graph;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import static ru.progwards.java2.lessons.graph.BoruvkaModel.*;

/**
 * Генерация тестовых деревьев
 */

public class BoruvkaGenerator {

    public static Graph<String, String> simpleGraph() {
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
        return new Graph<String, String>(
                List.of(F, E, C, B, D, A, G),
                List.of(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11));
    }

    public static Graph<String, String> genGraph(int nodesCount, int edgesCount) {
        List<Node<String, String>> nodes = new ArrayList<>(nodesCount);
        for (int i= 0; i<nodesCount; i++) {
            Node<String, String> n = new Node<>("n"+i+"n");
            n.in = new ArrayList<>();
            n.out = new ArrayList<>();
            nodes.add(n);
        }
        SecureRandom random = new SecureRandom();
        List<Edge<String, String>> edges = new ArrayList<>(edgesCount);
        for (int i= 0; i<edgesCount; i++) {
            Node n1 = nodes.get(random.nextInt(nodesCount));
            Node n2 = nodes.get(random.nextInt(nodesCount));
            Edge<String, String> e = new Edge<>(n1.info+"-"+n2.info,n1,n2,random.nextInt(edgesCount));
            n1.out.add(e);
            n2.in.add(e);
            edges.add(e);
        }
        return new Graph<String, String>(nodes, edges);
    }

    public static void main(String[] args) {
        Graph<String, String> graph = simpleGraph();
        List<Edge<String, String>> result = Boruvka.minTree(graph);
        System.out.println(result);
    }
}
