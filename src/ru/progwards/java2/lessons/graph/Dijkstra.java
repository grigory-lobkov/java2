package ru.progwards.java2.lessons.graph;

import java.util.Arrays;
import java.util.TreeSet;

import static ru.progwards.java2.lessons.graph.Dijkstra.State.*;

/**
 * Алгоритм Дейкстра на основе матрицы смежности
 */
public class Dijkstra {

    /**
     * Матрица смежности графа
     */
    int graph[][];
    /**
     * Количество вершин графа
     */
    int size;
    /**
     * Значение бесконечности
     */
    final static int INF = Integer.MAX_VALUE;

    Dijkstra(int[][] graph) {
        size = graph.length;
        if (size == 0)
            throw new UnsupportedOperationException("Граф должен быть не пуст.");
        for (int[] row : graph) {
            if (size != row.length)
                throw new UnsupportedOperationException("Ширина и высота матрицы должна быть одинаковой.");
        }
        this.graph = graph;
    }

    /**
     * Состояние вершины
     */
    enum State {
        // Расчет завершен
        DONE,
        // Расчёт в процессе (в очереди)
        CALC,
        // Не рассчитана
        UNDEF};

    /**
     * Вершина
     */
    class Vertex {
        /**
         * Порядковый номер (с нуля)
         */
        int n;
        /**
         * Вес (сколько стоит добраться)
         */
        int weight;
        /**
         * Состояние
         * @see State
         */
        State status;

        @Override
        public String toString() {
            return "Vertex{n=" + n + ", weight=" + weight + '}';
        }
    }

    /*
     * Алгоритм Дейсктры из вершины n
     * @param n Вершина, из которой надо построить маршруты
     * @return Матрицу вариантов минимальных маршрутов до всех вершин
     */
    public int[] find(int n) {
        // создадим пустой массив результатов, всем узлам - бесконечность
        int[] result = new int[size];
        Arrays.fill(result, INF);
        // массив весов вершин
        Vertex V[] = new Vertex[size];
        for (int i = 0; i < size; i++) {
            V[i] = new Vertex();
            V[i].weight = INF;
            V[i].n = i;
            V[i].status = UNDEF;
        }
        // вес искомой вершины
        V[n].weight = 0;
        result[n] = 0;
        // очередь вершин
        TreeSet<Vertex> vertQueue = new TreeSet<Vertex>((v1, v2) -> {
            if (v1.weight == v2.weight)
                return Integer.compare(v1.n, v2.n);
            else return v1.weight > v2.weight ? 1 : -1;
        });
        vertQueue.add(V[n]);

        // пока очередь не пуста
        while (vertQueue.size() != 0) {
            Vertex aV = vertQueue.pollFirst(); // вершина с минимальным весом
            // работа по данной вершине завершена
            aV.status = DONE;
            result[aV.n] = aV.weight;
            // пройдем по связям этой вершины
            int[] links = graph[aV.n]; // да, выгоднее было бы что-то типа связного списка - его можно было бы подготовить в конструкторе
            for (int i = 0; i < size; i++) {
                int edgeWeight = links[i];
                if (edgeWeight < INF) {
                    // с вершиной есть связь
                    Vertex bV = V[i];
                    if (bV.status != DONE) {
                        // вершина еще не расчитана до конца
                        int newWeight = edgeWeight + aV.weight;
                        if (newWeight < bV.weight) {
                            // найден меньший вес до вершины
                            if (bV.status == CALC)
                                vertQueue.remove(bV);
                            else
                                bV.status = CALC;
                            bV.weight = newWeight;
                            vertQueue.add(bV); // положим в очередь
                        }
                    }
                }
            }
        }
        return result;
    }

    public static void main(String[] args) {
        final int f = INF;

        // входная матрица графа, f = бесконечность
        int[][] matrix = {
                { 0, 7, 9, f, f,14},
                { 7, 0,10,15, f, f},
                { 9,10, 0,11, f, 2},
                { f,15,11, 0, 6, f},
                { f, f, f, 6, 0, 9},
                {14, f, 2, f, 9, 0}};

        // готовим алгоритм
        Dijkstra d = new Dijkstra(matrix);

        // выводим для нулевого узла
        //System.out.println(Arrays.deepToString(d.find(0)));
        System.out.println(Arrays.toString(d.find(0)));

    }

}
