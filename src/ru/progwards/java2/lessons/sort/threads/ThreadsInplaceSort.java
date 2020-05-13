package ru.progwards.java2.lessons.sort.threads;

import ru.progwards.java2.lessons.sort.simple.QuickSort;
import ru.progwards.java2.lessons.sort.simple.SortTest;

import java.util.concurrent.*;

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
public class ThreadsInplaceSort<T extends Comparable> {

    final int FIRST_BLOCK_MAX_SIZE = 20_000_000/2; // Максимальный размер сегмента при первичном разбиении
    final int MAX_JOIN_SEGMENTS = 2; // Сколько сегментов одновременно мы можем объединять (только два)
    final int POOL_SIZE = 4; // Размер пула очередей на выполнение работ

    Block[] segments;
    Comparable[] data;
    final TriConsumer<Comparable[], Integer, Integer> oneBlockSorter;
    final TriConsumer<Comparable[], Integer, Integer> mergeSorter;

    public ThreadsInplaceSort(Comparable[] data,
                              TriConsumer<Comparable[], Integer, Integer> oneBlockSorter,
                              TriConsumer<Comparable[], Integer, Integer> mergeSorter) {
        this.data = data;
        this.oneBlockSorter = oneBlockSorter;
        this.mergeSorter = mergeSorter;
    }

    /**
     * Вспомогательный класс, который считывает данные, сортирует и возвращает результат в Future
     */
    class DataSplitSorter implements Callable<Block> {
        final int idx;
        final int size;

        public DataSplitSorter(int idx, int size) {
            this.idx = idx;
            this.size = size;
        }

        @Override
        public Block call() {
            oneBlockSorter.accept(data, idx, idx+size-1);
            return new Block(idx,size);
        }
    }

    /**
     * Разбить массив на кучу мелких подмассивов для многопоточного объединения
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private void splitAndSort() throws ExecutionException, InterruptedException {
        // сколько в одном сегменте объектов
        //int size = Math.min(Math.min(FIRST_BLOCK_MAX_SIZE, (data.length+POOL_SIZE-1) / POOL_SIZE), (data.length + MAX_JOIN_SEGMENTS - 1) / MAX_JOIN_SEGMENTS);
        int size = Math.min(FIRST_BLOCK_MAX_SIZE, data.length);
        // сколько получится всего сегментов
        int count = (data.length + size - 1) / size;

        segments = new Block[count];
        ExecutorService service = Executors.newFixedThreadPool(POOL_SIZE);
        Future<Block>[] futures = new Future[count];

        try {
            for (int i = 0; i < count; i++) {
                futures[i] = service.submit(new DataSplitSorter(i * size, i * size+size>data.length?data.length-i*size:size));
            }
            for (int i = 0; i < count; i++) {
                segments[i] = futures[i].get();
            }
        } finally {
            service.shutdown();
        }
    }

    /**
     * Слияние сегментов сортировкой
     */
    class DataMergeSorter implements Callable<Block> {
        Block b1;
        Block b2;

        public DataMergeSorter(Block b1, Block b2) {
            this.b1 = b1;
            this.b2 = b2;
        }

        @Override
        public Block call() {
            mergeSorter.accept(data, b1.idx, b2.last);
            return new Block(b1.idx, b1.size+b2.size);
        }
    }

    /**
     * Слияние двух сегментов в один проход
     */
    class DataMergeSorter2 implements Callable<Block> {
        Block b1;
        Block b2;

        public DataMergeSorter2(Block b1, Block b2) {
            this.b1 = b1;
            this.b2 = b2;
        }

        @Override
        public Block call() {
            int size = b1.size + b2.size;
            final Comparable[] result = new Comparable[size];
            int i = 0;
            int i1 = b1.idx;
            int i2 = b2.idx;
            while (true) {
                if (data[i1].compareTo(data[i2]) < 0) {
                    result[i++] = data[i1++];
                    if (i1 > b1.last) break;
                } else {
                    result[i++] = data[i2++];
                    if (i2 > b2.last) break;
                }
            }
            if (i2 > b2.last) {
                System.arraycopy(data, i1, result, i, b1.last - i1 + 1);
            } else {
                System.arraycopy(data, i2, result, i, b2.last - i2 + 1);
            }
            System.arraycopy(result, 0, data, b1.idx, size);
            return new Block(b1.idx, b1.size + b2.size);
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

                //System.out.println("checkMerge(), size=" + segments.length + ", step=" + step + ", count->" + count);

                Block[] newSegments = new Block[count];
                Future<Block>[] futures = new Future[count];

                for (int i = 0; i < count; i++) {
                    int size = i * step + step > segments.length ? segments.length - i * step : step;
                    if(size==2) {
                        futures[i] = service.submit(new DataMergeSorter2(segments[i * step], segments[i * step + 1]));
                    } else {
                        count--;
                        newSegments[i] = segments[i * step];
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
        //System.out.println("merge(), count=" + segments.length);

        ExecutorService service = Executors.newSingleThreadExecutor();
        try {
            Future<Block> future = service.submit(new DataMergeSorter2(segments[0], segments[1]));
            future.get();
        } finally {
            service.shutdown();
        }
    }

    public static <T extends Comparable<T>> void sort(T[] a) throws ExecutionException, InterruptedException {
        TriConsumer<Comparable[], Integer, Integer> oneBlockSorter = (m,f,t) -> QuickSort.sortHoare(m, f, t);
        TriConsumer<Comparable[], Integer, Integer> mergeSorter = (m,f,t) -> QuickSort.sortHoare(m, f, t);
        //TriConsumer<Comparable[], Integer, Integer> mergeSorter = (m,f,t) -> InsertionSort.sortInLimits(m, f, t);

        ThreadsInplaceSort<Integer> s = new ThreadsInplaceSort(a, oneBlockSorter, mergeSorter);
        s.splitAndSort();
        s.checkMerge();
        s.merge();
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Integer[] org = new Integer[20_000_000];
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

class Block {
    Integer idx;
    Integer size;
    Integer last;
    public Block(int idx, int size) {
        this.idx = idx;
        this.size = size;
        last = idx+size-1;
    }
}

@FunctionalInterface
interface TriConsumer<A,B,C> {
    void accept(A a, C b, C c);
}
/*
final int FIRST_BLOCK_MAX_SIZE = 1_000_000; // Максимальный размер сегмента при первичном разбиении
final int MAX_JOIN_SEGMENTS = 2; // Сколько сегментов одновременно мы можем объединять (только два)
final int POOL_SIZE = 4; // Размер пула очередей на выполнение работ

Execution time: 26 266 ms
Execution time: 9 032 ms
HoaraSort execution time: 4 642 ms

final int FIRST_BLOCK_MAX_SIZE = 100_000;

Execution time: 22 007 ms
Execution time: 11 499 ms
HoaraSort execution time: 4 896 ms

final int FIRST_BLOCK_MAX_SIZE = 20_000_000/4;

Execution time: 18 098 ms
Execution time: 6 476 ms
HoaraSort execution time: 3 716 ms

final int FIRST_BLOCK_MAX_SIZE = 20_000_000/2;

Execution time: 7 888 ms
Execution time: 16 033 ms
HoaraSort execution time: 4 409 ms

К сожалению, HoaraSort всё-таки бьёт многопоточность.
Не смог придумать, как сливать два подмассива in-place: пришлось сливать в промежуточный массив, а потом копировать в основной - это быстрее, чем вызывать любой алгоритм сортировки для объединения данных.
*/