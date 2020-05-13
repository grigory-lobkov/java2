package ru.progwards.java2.lessons.sort.threads;

import ru.progwards.java2.lessons.sort.simple.InsertionSort;
import ru.progwards.java2.lessons.sort.simple.QuickSort;
import ru.progwards.java2.lessons.sort.simple.SortTest;

import java.util.concurrent.*;
import java.util.function.Consumer;

/*
Класс должен реализовывать многопоточный алгоритм сортировки слиянием. Алгоритм следующий:


Разбить массив на N блоков

Отсортировать каждый блок используя любой алгоритм in-place сортировки, например quick sort

Слить блоки в несколько потоков - попарно. На каждом шаге количество работающих потоков
в 2 раза меньше, чем количество блоков. При нечетном количестве блоков, оставить один не слитым.

Сигнатура метода  public static<T extends Comparable<T>> void sort(T[] a)

P.S. Рекомендую алгоритм слияния делать через очередь/стэк, аналогично tim sort, тогда процесс
синхронизации потоков в момент слияния сильно упростится.
*/
public class ThreadsSort<T extends Comparable> {


    final int FIRST_BLOCK_MAX_SIZE = 100_000; // Максимальный размер сегмента при первичном разбиении
    final int MAX_JOIN_SEGMENTS = 2; // Сколько сегментов одновременно мы можем объединять (от двух)
    final int POOL_SIZE = 4; // Размер пула очередей на выполнение работ

    Comparable[] data;
    Comparable[][] segments;
    final Consumer<Comparable[]> oneBlockSorter;
    final Consumer<Comparable[]> mergeSorter;

    public ThreadsSort(Comparable[] data, Consumer<Comparable[]> oneBlockSorter, Consumer<Comparable[]> mergeSorter) {
        this.data = data;
        this.oneBlockSorter = oneBlockSorter;
        this.mergeSorter = mergeSorter;
    }

    /**
     * Вспомогательный класс, который считывает данные, сортирует и возвращает результат в Future
     */
    class DataSplitSorter implements Callable<Comparable[]> {
        final int idx;
        final int size;

        public DataSplitSorter(int idx, int size) {
            this.idx = idx;
            this.size = size + idx > data.length ? data.length - idx : size;
        }

        @Override
        public Comparable[] call() {
            final Comparable[] result = new Comparable[size];
            System.arraycopy(data, idx, result, 0, size);
            oneBlockSorter.accept(result);
            return result;
        }
    }

    /**
     * Разбить массив на кучу мелких подмассивов для многопоточного объединения
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private void splitAndSort() throws ExecutionException, InterruptedException {
        System.out.println("splitAndSort()");
        // сколько в одном сегменте объектов
        int size = Math.min(Math.min(FIRST_BLOCK_MAX_SIZE, (data.length + POOL_SIZE - 1) / POOL_SIZE), (data.length + MAX_JOIN_SEGMENTS - 1) / MAX_JOIN_SEGMENTS);
        // сколько получится всего сегментов
        int count = (data.length + size - 1) / size;

        segments = new Comparable[count][];
        ExecutorService service = Executors.newFixedThreadPool(POOL_SIZE);
        Future<Comparable[]>[] futures = new Future[count];

        try {
            for (int i = 0; i < count; i++) {
                futures[i] = service.submit(new DataSplitSorter(i * size, i * size + size > data.length ? data.length - i * size : size));
            }
            for (int i = 0; i < count; i++) {
                segments[i] = futures[i].get();
            }
        } finally {
            service.shutdown();
        }
    }

    /**
     * Вспомогательный класс для многопутевого слияния
     * Обеспечивает поток объектов
     */
    class mergeSource implements Comparable {
        Comparable[] a;
        Comparable nextValue;
        public boolean hasNext;
        int nextIdx;

        mergeSource(Comparable[] a) {
            this.a = a;
            hasNext = true;
            nextValue = a[0];
            nextIdx = 1;
        }

        public Comparable get() {
            Comparable result = nextValue;
            if (nextIdx == a.length) {
                hasNext = false;
                nextValue = null;
            } else {
                nextValue = a[nextIdx++];
            }
            return result;
        }

        @Override
        public int compareTo(Object o) {
            return nextValue.compareTo(((mergeSource) o).nextValue);
        }
    }

    /**
     * Слияние нескольких массивов, переданных матрицей в конструктор
     */
    class DataMergeSorter implements Callable<Comparable[]> {
        Comparable[][] as;

        public DataMergeSorter(Comparable[][] as) {
            this.as = as;
        }

        @Override
        public Comparable[] call() {
            if (as.length == 1) return as[0];
            int size = 0;
            Comparable[] sources = new Comparable[as.length];
            for (int i = 0; i < as.length; i++) {
                size += as[i].length;
                sources[i] = new mergeSource(as[i]);
            }
            final Comparable[] result = new Comparable[size];
            int i = 0;
            mergeSorter.accept(sources);
            while (sources.length > 0) {
                mergeSource topSource = (mergeSource) sources[0];
                result[i++] = topSource.get();
                if (topSource.hasNext) {
                    mergeSorter.accept(sources);
                } else {
                    int newLen = sources.length - 1;
                    Comparable[] newSource = new Comparable[newLen];
                    System.arraycopy(sources, 1, newSource, 0, newLen);
                    sources = newSource;
                }
            }
            return result;
        }
    }

    /**
     * Слияние двух массивов
     */
    class DataMergeSorter2 implements Callable<Comparable[]> {
        Comparable[] a1;
        Comparable[] a2;

        public DataMergeSorter2(Comparable[] a1, Comparable[] a2) {
            this.a1 = a1;
            this.a2 = a2;
        }

        @Override
        public Comparable[] call() {
            int size = a1.length + a2.length;
            final Comparable[] result = new Comparable[size];
            int i = 0;
            int i1 = 0;
            int i2 = 0;
            while (true) {
                if (a1[i1].compareTo(a2[i2]) < 0) {
                    result[i++] = a1[i1++];
                    if (i1 == a1.length) break;
                } else {
                    result[i++] = a2[i2++];
                    if (i2 == a2.length) break;
                }
            }
            if (i1 < a1.length) {
                System.arraycopy(a1, i1, result, i, a1.length - i1);
            } else {
                System.arraycopy(a2, i2, result, i, a2.length - i2);
            }
            return result;
        }
    }

    /**
     * Проверка, можем ли мы делать окончательное слияние. Если нет - будем объединять пока не сможем
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private void checkMerge() throws ExecutionException, InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(POOL_SIZE);
        try {
            while (segments.length > MAX_JOIN_SEGMENTS) {
                // по сколько сегментов объединяем одновременно
                int step = Math.min(MAX_JOIN_SEGMENTS, (segments.length + MAX_JOIN_SEGMENTS - 1) / MAX_JOIN_SEGMENTS);
                // новое количество сегментов
                int count = (segments.length + step - 1) / step;

                System.out.println("checkMerge(), size=" + segments.length + ", step=" + step + ", count->" + count);

                Comparable[][] newSegments = new Comparable[count][];
                Future<Comparable[]>[] futures = new Future[count];

                for (int i = 0; i < count; i++) {
                    int size = i * step + step > segments.length ? segments.length - i * step : step;
                    if (size == 2) {
                        futures[i] = service.submit(new DataMergeSorter2(segments[i * step], segments[i * step + 1]));
                    } else {
                        Comparable[][] toMerge = new Comparable[size][];
                        for (int k = 0; k < size; k++)
                            toMerge[k] = segments[i * step + k];
                        futures[i] = service.submit(new DataMergeSorter(toMerge));
                    }
                }

                for (int i = 0; i < count; i++) {
                    newSegments[i] = futures[i].get();
                }

                segments = newSegments;
            }
        } finally {
            service.shutdown();
        }
    }


    private void merge() throws ExecutionException, InterruptedException {
        System.out.println("merge(), count=" + segments.length);

        ExecutorService service = Executors.newSingleThreadExecutor();
        try {
            Future<Comparable[]> future = service.submit(new DataMergeSorter(segments));
            data = future.get();
        } finally {
            service.shutdown();
        }
    }

    public static <T extends Comparable<T>> void sort(T[] a) throws ExecutionException, InterruptedException {
        Consumer<Comparable[]> oneBlockSorter = m -> QuickSort.sortHoare(m, 0, m.length - 1);
        Consumer<Comparable[]> mergeSorter = m -> InsertionSort.sortZeroQuick(m);

        ThreadsSort<Integer> s = new ThreadsSort(a, oneBlockSorter, mergeSorter);
        s.splitAndSort();
        s.checkMerge();
        s.merge();
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Integer[] org = new Integer[2_000_000];
        SortTest.fill(org);
        System.out.println("Preparation done.");
        long start = System.currentTimeMillis();

        sort(org);

        String duration = String.valueOf(System.currentTimeMillis() - start);
        if (duration.length() > 3)
            duration = duration.substring(0, duration.length() - 3) + " " + duration.substring(duration.length() - 3);
        System.out.println("Execution time: " + duration + " ms");

        start = System.currentTimeMillis();

        sort(org);

        duration = String.valueOf(System.currentTimeMillis() - start);
        if (duration.length() > 3)
            duration = duration.substring(0, duration.length() - 3) + " " + duration.substring(duration.length() - 3);
        System.out.println("Execution time: " + duration + " ms");

        start = System.currentTimeMillis();

        QuickSort.sort2(org);

        duration = String.valueOf(System.currentTimeMillis() - start);
        if (duration.length() > 3)
            duration = duration.substring(0, duration.length() - 3) + " " + duration.substring(duration.length() - 3);
        System.out.println("HoaraSort execution time: " + duration + " ms");
    }

}

/*
final int MAX_JOIN_SEGMENTS = 10; // Сколько сегментов одновременно мы можем объединять (от двух)
final int POOL_SIZE = 4; // Размер пула очередей на выполнение работ

merge(), count=10
Execution time: 3 215 ms
merge(), count=10
Execution time: 3 358 ms

final int MAX_JOIN_SEGMENTS = 1000; // Сколько сегментов одновременно мы можем объединять (от двух)
final int POOL_SIZE = 4; // Размер пула очередей на выполнение работ

merge(), count=1000
Execution time: 3 816 ms
merge(), count=1000
Execution time: 10 548 ms

final int MAX_JOIN_SEGMENTS = 2; // Сколько сегментов одновременно мы можем объединять (от двух)
final int POOL_SIZE = 4; // Размер пула очередей на выполнение работ

checkMerge(), size=4, step=2, count->2
merge(), count=2
Execution time: 3 413 ms
checkMerge(), size=4, step=2, count->2
merge(), count=2
Execution time: 3 247 ms

final int FIRST_BLOCK_MAX_SIZE = 10_000; // Максимальный размер сегмента при первичном разбиении
final int MAX_JOIN_SEGMENTS = 6; // Сколько сегментов одновременно мы можем объединять (от двух)
final int POOL_SIZE = 4; // Размер пула очередей на выполнение работ

checkMerge(), size=1000, step=6, count->167
checkMerge(), size=167, step=6, count->28
checkMerge(), size=28, step=5, count->6
merge(), count=6
Execution time: 9 426 ms
checkMerge(), size=1000, step=6, count->167
checkMerge(), size=167, step=6, count->28
checkMerge(), size=28, step=5, count->6
merge(), count=6
Execution time: 2 955 ms

final int FIRST_BLOCK_MAX_SIZE = 10_000; // Максимальный размер сегмента при первичном разбиении
final int MAX_JOIN_SEGMENTS = 2; // Сколько сегментов одновременно мы можем объединять (от двух)
final int POOL_SIZE = 4; // Размер пула очередей на выполнение работ

checkMerge(), size=1000, step=2, count->500
checkMerge(), size=500, step=2, count->250
checkMerge(), size=250, step=2, count->125
checkMerge(), size=125, step=2, count->63
checkMerge(), size=63, step=2, count->32
checkMerge(), size=32, step=2, count->16
checkMerge(), size=16, step=2, count->8
checkMerge(), size=8, step=2, count->4
checkMerge(), size=4, step=2, count->2
merge(), count=2
Execution time: 10 582 ms
checkMerge(), size=1000, step=2, count->500
checkMerge(), size=500, step=2, count->250
checkMerge(), size=250, step=2, count->125
checkMerge(), size=125, step=2, count->63
checkMerge(), size=63, step=2, count->32
checkMerge(), size=32, step=2, count->16
checkMerge(), size=16, step=2, count->8
checkMerge(), size=8, step=2, count->4
checkMerge(), size=4, step=2, count->2
merge(), count=2
Execution time: 5 255 ms

DataMergeSorter2()

checkMerge(), size=1000, step=2, count->500
checkMerge(), size=500, step=2, count->250
checkMerge(), size=250, step=2, count->125
checkMerge(), size=125, step=2, count->63
checkMerge(), size=63, step=2, count->32
checkMerge(), size=32, step=2, count->16
checkMerge(), size=16, step=2, count->8
checkMerge(), size=8, step=2, count->4
checkMerge(), size=4, step=2, count->2
merge(), count=2
Execution time: 9 065 ms
checkMerge(), size=1000, step=2, count->500
checkMerge(), size=500, step=2, count->250
checkMerge(), size=250, step=2, count->125
checkMerge(), size=125, step=2, count->63
checkMerge(), size=63, step=2, count->32
checkMerge(), size=32, step=2, count->16
checkMerge(), size=16, step=2, count->8
checkMerge(), size=8, step=2, count->4
checkMerge(), size=4, step=2, count->2
merge(), count=2
Execution time: 4 169 ms


final int FIRST_BLOCK_MAX_SIZE = 100_000; // Максимальный размер сегмента при первичном разбиении
final int MAX_JOIN_SEGMENTS = 2; // Сколько сегментов одновременно мы можем объединять (от двух)
final int POOL_SIZE = 4; // Размер пула очередей на выполнение работ
10_000_000
Execution time: 8 948 ms
Execution time: 4 102 ms
HoaraSort execution time: 4 020 ms
20_000_000
Execution time: 17 808 ms
Execution time: 7 653 ms
HoaraSort execution time: 9 110 ms

*/