package ru.progwards.java2.lessons.sort.threads;

import ru.progwards.java2.lessons.sort.simple.SelectionSort;
import ru.progwards.java2.lessons.sort.simple.SortTest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ThreadTest {

    static final int TEST_COUNT = 2_000;
    static final int POOL_SIZE = 5;

    public static Integer[] org = new Integer[1000];

    static class RunSort implements Runnable {
        @Override
        public void run() {
            Integer[] a = SortTest.copy(ThreadTest.org);
            SelectionSort.sort(a);
        }
    }

    static void sortTest() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < TEST_COUNT; i++) {
            new RunSort().run();
        }
        long stop = System.currentTimeMillis();
        System.out.println(stop - start);
    }

    static void sortThreads() throws InterruptedException {
        Thread[] t = new Thread[TEST_COUNT];

        long start = System.currentTimeMillis();
        for (int i = 0; i < TEST_COUNT; i++) {
            t[i] = new Thread(new RunSort());
            t[i].start();
        }
        for (int i = 0; i < TEST_COUNT; i++) {
            t[i].join();
        }
        long stop = System.currentTimeMillis();
        System.out.println(stop - start + "  -- sortThreads()");
    }

    static void sortPool() throws InterruptedException {
        long start = System.currentTimeMillis();
        ExecutorService service = Executors.newFixedThreadPool(POOL_SIZE);
        for (int i = 0; i < TEST_COUNT; i++) {
            service.submit(new RunSort());
        }
        service.shutdown();
        service.awaitTermination(1000, TimeUnit.SECONDS);
        long stop = System.currentTimeMillis();
        System.out.println(stop - start + "  -- sortPool()");
    }

    static void sortPool2() throws InterruptedException {
        long start = System.currentTimeMillis();
        ThreadPool pool = new ThreadPool(POOL_SIZE);
        for (int i = 0; i < TEST_COUNT; i++) {
            pool.execute(new RunSort());
        }
        pool.shutDown(true);
        long stop = System.currentTimeMillis();
        System.out.println(stop - start + "  -- sortPool2()");
    }

    public static void main(String[] args) throws InterruptedException {
        SortTest.fill(org);

        //simpleTest();
        //sortTest();
        sortPool2();
        sortPool();
        sortThreads();

        System.out.println();
        sortPool2();
        sortPool();
        sortThreads();
    }
}
/* TEST_COUNT = 2_000, POOL_SIZE=5
3042  -- sortPool2()
2320  -- sortPool()
2225  -- sortThreads()

1198  -- sortPool2()
1172  -- sortPool()
1430  -- sortThreads()
*/
/* TEST_COUNT = 100_000, POOL_SIZE=5
63959  -- sortPool2()
56655  -- sortPool()
86907  -- sortThreads()

56783  -- sortPool2()
56244  -- sortPool()
83586  -- sortThreads()
*/
/* TEST_COUNT = 20_000, POOL_SIZE=5
15511  -- sortPool2()
11347  -- sortPool()
17036  -- sortThreads()

12366  -- sortPool2()
11921  -- sortPool()
16279  -- sortThreads()
*/
/* TEST_COUNT = 20_000, POOL_SIZE=10
16023  -- sortPool2()
11394  -- sortPool()
12330  -- sortPool2()
11448  -- sortPool()
*/
/* TEST_COUNT = 20_000, POOL_SIZE=20
15796  -- sortPool2()
11153  -- sortPool()
12177  -- sortPool2()
11112  -- sortPool()
*/
/* TEST_COUNT = 20_000, POOL_SIZE=100
15098  -- sortPool2()
11876  -- sortPool()
11913  -- sortPool2()
11632  -- sortPool()
*/
/* TEST_COUNT = 20_000, POOL_SIZE=5
15367  -- sortPool2()
11230  -- sortPool()
12184  -- sortPool2()
11756  -- sortPool()
*/
/* TEST_COUNT = 20_000, POOL_SIZE=4  (CORE COUNT)
15409  -- sortPool2()
11067  -- sortPool()
11796  -- sortPool2()
10949  -- sortPool()
*/
/* TEST_COUNT = 20_000, POOL_SIZE=3
15550  -- sortPool2()
11043  -- sortPool()
11317  -- sortPool2()
10862  -- sortPool()
*/
/* TEST_COUNT = 20_000, POOL_SIZE=2
21742  -- sortPool2()
15992  -- sortPool()
16801  -- sortPool2()
16319  -- sortPool()
*/
/* TEST_COUNT = 20_000, POOL_SIZE=1
29336  -- sortPool2()
21425  -- sortPool()
22577  -- sortPool2()
20816  -- sortPool()
*/