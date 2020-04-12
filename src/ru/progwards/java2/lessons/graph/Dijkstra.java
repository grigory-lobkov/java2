package ru.progwards.java2.lessons.graph;

import java.util.Arrays;

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
    final static int f = Integer.MAX_VALUE;
    enum State {
        /**
         * Расчет завершен
         */
        DONE,
        /**
         * Расчитан
         */
        CALCED,
        /**
         * Не рассчитан
         */
        UNDEF};

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
     * Алгоритм Дейсктры из вершины n
     * @param n Вершина, из которой надо построить маршруты
     * @return Матрицу вариантов минимальных маршрутов до всех вершин
     */
    public int[][] find(int n) {
        // массив состояний вершин
        State states[] = new State[size];
        // никого не рассчитали
        for (int i = 0; i < size; i++)
            states[i] = UNDEF;
        // первую (n-ую) вершину посчитали
        states[n] = CALCED;
        // количество завершенных
        int visitedCount = 0;

        // создадим пустой массив результатов, всем узлам - бесконечность
        int[][] result = new int[size][];
        for (int i = 0; i < size; i++) {
            result[i] = new int[size];
            Arrays.fill(result[i], f);
        }
        // в первой вершине - нуль
        result[n][n] = 0;

        while (visitedCount != size) {
            // найдем минимальную посчитанную вершину
            int minV = -1;
            for (int i = 0; i < size; i++)
                if (states[i] == CALCED) {
                    minV = i;
                    break;
                }
            // пройдем по связям этой вершины
            int[] links = graph[minV];
            for (int i = 0; i < size; i++) {
                if (links[i] < f && states[i] != DONE) {

                }
            }
        }

    }

    public static void main(String[] args) {

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
        System.out.println(Arrays.deepToString(d.find(0)));

    }

}
