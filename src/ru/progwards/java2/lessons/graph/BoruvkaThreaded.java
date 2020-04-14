package ru.progwards.java2.lessons.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.Semaphore;

import static ru.progwards.java2.lessons.graph.BoruvkaModel.*;
import static ru.progwards.java2.lessons.graph.BoruvkaModel.Status.*;

/**
 * Нахождение минимального оставного дерева по алгоритму Борувки
 *
 * Многопоточная обработка - для каждого узла создается отдельный поток,
 * количество потоков ограничено {@code separateThreads}, потоки самоуничтожаются, если их съедает соседний растущий граф
 *
 * Вариант пригоден только если при работе алгоритма есть какие-то накладные расходы, порядка 1мс на ответ
 * А так - он с треском проигрывает однопоточным не конкурирующим алгоритмам (в 3-10 раз)
 */
public class BoruvkaThreaded<N, E> implements IBoruvka<N, E> {

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
     * @see BoruvkaThreaded#takeOuts
     */
    final boolean takeIns = true;

    /**
     * Выводить ли отладочную информацию в процессе обработки
     */
    final boolean writeDebugInfo = false;

    final int separateThreads = 4;


    /**
     * Вычисляет минимальное остовное дерево в виде списка дуг графа
     *
     * @param graph
     * @param <NodeType> Тип "Узел"
     * @param <EdgeType> Тип "Ребро"
     * @return минимальное остовное дерево в виде списка дуг графа
     */
    static <NodeType, EdgeType> List<Edge<NodeType, EdgeType>> minTree(Graph<NodeType, EdgeType> graph) {
        BoruvkaThreaded<NodeType, EdgeType> alg = new BoruvkaThreaded<NodeType, EdgeType>();
        return alg.getMinEdgeTree(graph);
    }

    /**
     * Вычислить дуги, требующиеся для построения минимального остовного дерева
     *
     * @return список дуг, требующихся для построения минимального остовного дерева
     */
    public List<Edge<N, E>> getMinEdgeTree(Graph<N, E> graph) {
        List<Edge<N, E>> result = new ArrayList<Edge<N, E>>(graph.nodes.size());
        ArrayList<Graph<N, E>> graphs = new ArrayList<Graph<N, E>>(graph.nodes.size());
        Runnable[] runnables = new Runnable[graph.nodes.size()];
        Thread[] threads = new Thread[graph.nodes.size()];

        // генерация оставных деревьев
        for (Node n : graph.nodes) {
            Graph<N, E> g = new Graph<N, E>(new ArrayList<Node<N, E>>(), List.of());
            n.graph = g;
            g.nodes.add(n);
            g.status = NOT_USED;
            graphs.add(g);
        }
        // проидентифицируем дуги
        int id = 0;
        for (Edge e : graph.edges)
            e.id = id++;

        int j = 0;
        for (Graph<N, E> g : graphs) {
            g.id = j;
            j++;
        }

        if (writeDebugInfo) System.out.println("Starting threads...");
        // будем перебирать все оставные деревья с шагом равным "всего"/"количество потоков"
        // и запускать потоки, если дерево еще не объединено
        j = 0;
        int size = graphs.size();
        int i = size / (separateThreads > 0 ? separateThreads : 10);
        while (true) {
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            j = (j + i) % size;
            int whenDone = size;
            Graph<N, E> g = graphs.get(j);
            while (graphs.get(j).status != NOT_USED) {
                j = (j + 1) % size;
                if (--whenDone <= 0) break;
                g = graphs.get(j);
            }
            if (whenDone <= 0) break;
            runnables[j] = new ThreadDFS(g);
            threads[j] = new Thread(runnables[j]);
            threads[j].start();
            if (writeDebugInfo)
                System.out.println(threads[j].getName() + " started");
        }

        if (writeDebugInfo) System.out.println("Waiting threads finish...");
        for (int k = 0; k < threads.length; k++)
            if (threads[k] != null)
                try {
                    threads[k].join();
                    result.addAll(((ThreadDFS) runnables[k]).result);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        return result;
    }

    Semaphore semaphore = new Semaphore(separateThreads);

    /**
     * Поток рассчета оставного графа
     */
    class ThreadDFS implements Runnable {
        Graph<N, E> graph;
        List<Edge<N, E>> result = new ArrayList<Edge<N, E>>();

        public ThreadDFS(Graph<N, E> graph) {
            this.graph = graph;
        }

        @Override
        public void run() {
            if (graph.status == NOT_USED)
                Find(result, graph);
            semaphore.release();
            if (writeDebugInfo)
                System.out.println(Thread.currentThread().getName() + " stopped");
        }
    }

    /**
     * Генерация оставного леса из дерева подсоединением других оставных лесов
     * В первую очередь соединяемся по дугам с минимальным весом
     *
     * @param result список дуг оставного леса (пополняется)
     * @param g      оставной лес, который анализируем сейчас (растёт по мере объединения)
     */
    private void Find(List<Edge<N, E>> result, Graph<N, E> g) {
        synchronized (g.status) {
            if (g.status != NOT_USED) return;
            g.status = VISITED; // серый
        }
        if (writeDebugInfo) System.out.println(Thread.currentThread().getName() + " Find(" + g.nodes.get(0) + ") g.id="+g.id);

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
            /*if (writeDebugInfo) {
                String log="";
                log += Thread.currentThread().getName() + " References in queue: ";
                for(Edge<N, E> e: referenced)
                    log += " " + e;
                System.out.println(log);
            }*/
            Edge<N, E> e = referenced.pollFirst();
            Node<N, E> n = e.Other(g); // узел не в графе g
            if (g.status == DELETE) return; // если наш граф g удален, сразу выходим, т.к. Other(g) мог вернуть узел третьего графа, к которому нас подсоединили
            if (n != null) {
                while (true) {
                    try {
                        Thread.sleep(0);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    int mResult = Merge(g, n.graph, referenced);
                    if (mResult == 3) {
                        if (writeDebugInfo) System.out.println(Thread.currentThread().getName() + " deleted");
                        return; // нас удалили, выходим
                    }
                    else if (mResult == 2) continue; // подсоединяемый граф удален, попробуем провести связку ещё раз
                    break;
                }
                result.add(e);
            }
            e.processed = true;
        }
        synchronized (g.status) {
            g.status = IN_USE; // черный
        }
    }

    /**
     * Объединяем два графа в один, который указан первым
     *
     * @param g1 граф-приёмник
     * @param g2 граф-источник (опустошаемый)
     */
    private int Merge(Graph<N, E> g1, Graph<N, E> g2, TreeSet<Edge<N, E>> referenced) {
        synchronized (BoruvkaThreaded.this) {
            synchronized (g2.status) {
                synchronized (g1.status) {
                    if (writeDebugInfo) System.out.println(Thread.currentThread().getName() + " syncMerge(" + g1.id +"("+g1.nodes.size()+") " +g1.status+ "," + g2.id+"("+g1.nodes.size()+") " +g2.status + ")");
                    if (g1.status == DELETE || g1.nodes.size() == 0)
                        return 3; // наш поток удалён, прекращаем работу
                    if (g2.status == DELETE || g2.nodes.size() == 0)
                        return 2; // подсоединяемый граф удален, повторить поиск по ребру
                    else if (g2.status == NOT_USED)
                        g2.status = DELETE; // раз еще не работали, меняем статус, что уже удален
                    else if (g2.status == IN_USE)
                        g2.status = DELETE; // с графом уже отработали, метим на удаление
                    g1.status = DELETE; // монополизируем оба графа
                    g2.status = DELETE; // монополизируем оба графа
                }
            }
        }
        if (writeDebugInfo) System.out.println(Thread.currentThread().getName() + " Merge(" + g1.nodes.get(0) + "," + g2.nodes.get(0) + ")");
        for (Node<N, E> n : g2.nodes) {
            //boolean isNewRefs = n.status != IN_USE;
            // добавим новые дуги в сравнение, если узел не обработан
            if (n.status != IN_USE) {
                if (takeOuts)
                    for (Edge e : n.out)
                        if (!e.processed) {
                            referenced.add(e);
                            //if (writeDebugInfo) System.out.println("  "+Thread.currentThread().getName() + " add " + e);
                        }
                if (takeIns)
                    for (Edge e : n.in)
                        if (!e.processed) {
                            referenced.add(e);
                            //if (writeDebugInfo) System.out.println("  "+Thread.currentThread().getName() + " add " + e);
                        }

                n.status = IN_USE;
            }
            // перенесем узел в первый граф
            g1.nodes.add(n);
            n.graph = g1;
        }
        // очистим граф-источник
        g2.nodes.clear();
        g1.status = VISITED;
        //g2.status = IN_USE;
        return 1;
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
                //List.of(F, E, C, B, D, A, G),
                List.of(A, B, C, D, E, F, G),
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
/*
[edge{info=a-b, weight=0.0},
 edge{info=a-c, weight=2.0},
 edge{info=b-g, weight=5.0},
 edge{info=c-f, weight=1.0},
 edge{info=e-d, weight=0.0},
 edge{info=d-g, weight=0.0}]
*/
