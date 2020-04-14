package ru.progwards.java2.lessons.graph;

import java.util.List;

import static ru.progwards.java2.lessons.graph.BoruvkaGenerator.genGraph;
import static ru.progwards.java2.lessons.graph.BoruvkaGenerator.simpleGraph;

/**
 * Нагрузочное тестирование классов, замеры времени и памяти
 * В данном случае: алгоритма Борувки
 */
public class BoruvkaTester {

    long startMem;
    long stepMem;
    long spentTime;
    long stepTime;
    long serviceTime;

    private static long SLEEP_INTERVAL = 100;

    private long getMemoryUse() {
        collectGarbage();
        collectGarbage();
        long totalMemory = Runtime.getRuntime().totalMemory();
        collectGarbage();
        collectGarbage();
        long freeMemory = Runtime.getRuntime().freeMemory();
        return (totalMemory - freeMemory);
    }

    private void collectGarbage() {
        try {
            System.gc();
            Thread.currentThread().sleep(SLEEP_INTERVAL);
            System.runFinalization();
            Thread.currentThread().sleep(SLEEP_INTERVAL);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    void statStart(String name) {
        startMem = getMemoryUse();
        stepMem = startMem;
        spentTime = 0;
        serviceTime = 0;
        System.out.println("\nStatistic " + name + " started.");
        stepTime = System.nanoTime();
    }

    void statStep(String name) {
        long time = System.nanoTime();
        long mem = getMemoryUse();
        long spentStep = time - stepTime;
        spentTime += spentStep;
        String step = name + ": " + " ".repeat(Math.max(7 - name.length(), 0)) + (mem - stepMem) / 1024 + " kb " + spentStep / 1000 + " mcs";
        System.out.println(step + " ".repeat(Math.max(30 - step.length(), 0)) + " Total: " + mem / 1024 + " kb " + spentTime / 1000 + " mcs");
        stepMem = mem;
        stepTime = System.nanoTime();
        serviceTime = stepTime - time;
    }

    void statEnd(String name) {
        System.out.println(name + " (finished), serviceTime " + serviceTime / 1000 + " mcs");
    }

    void test(Class clazz, BoruvkaModel.Graph graph) throws Exception {
        statStart("test(" + clazz.getSimpleName() + ")");
        IBoruvka b = (IBoruvka) clazz.getDeclaredConstructor().newInstance();
        statStep("Created");
        List<BoruvkaModel.Edge<String, String>> result = b.getMinEdgeTree(graph);
        statStep("Done");
        double weight = 0;
        for (BoruvkaModel.Edge<String, String> e : result)
            weight += e.weight;
        statEnd("test(" + clazz.getSimpleName() + ") size=" + result.size() + " weight=" + weight);
    }

    public static void main(String[] args) throws Exception {
        BoruvkaTester t = new BoruvkaTester();
        t.test_one(simpleGraph());
        t.test_one(genGraph(50, 500));
        t.test_one(genGraph(100, 1_000));
        t.test_one(genGraph(1_000, 10_000));
        t.test_one(genGraph(10_000, 100_000));
        //t.test_one(genGraph(100_000, 500_000));
    }
    
    private void test_one(BoruvkaModel.Graph graph) throws Exception {
        System.out.println("\nGRAPH nodes=" + graph.nodes.size() + " edges=" + graph.edges.size());
        test(BoruvkaDequeue.class, graph);
        test(BoruvkaDequeue.class, graph);
        test(BoruvkaDequeue.class, graph);
    }

}

/* 2 THREADS + 1ms wait

Statistic test(BoruvkaThreaded) started.
Created: 419 kb 771 mcs        Total: 1845 kb 771 mcs
Done:    57 kb 21196 mcs       Total: 1903 kb 21967 mcs
test(BoruvkaThreaded) size=6 weight=7.0 (finished), serviceTime 871474 mcs

Statistic test(BoruvkaOneQueue) started.
Created: 0 kb 460 mcs          Total: 1941 kb 460 mcs
Done:    0 kb 5815 mcs         Total: 1941 kb 6275 mcs
test(BoruvkaOneQueue) size=4 weight=1.0 (finished), serviceTime 871643 mcs

Statistic test(Boruvka) started.
Created: 0 kb 524 mcs          Total: 1942 kb 524 mcs
Done:    0 kb 7799 mcs         Total: 1942 kb 8324 mcs
test(Boruvka) size=6 weight=15.0 (finished), serviceTime 859868 mcs

GRAPH nodes=50 edges=500

Statistic test(BoruvkaOneQueue) started.
Created: 0 kb 156 mcs          Total: 2124 kb 156 mcs
Done:    0 kb 50149 mcs        Total: 2124 kb 50306 mcs
test(BoruvkaOneQueue) size=37 weight=896.0 (finished), serviceTime 882598 mcs

Statistic test(Boruvka) started.
Created: 0 kb 83 mcs           Total: 2124 kb 83 mcs
Done:    0 kb 59195 mcs        Total: 2125 kb 59278 mcs
test(Boruvka) size=49 weight=1285.0 (finished), serviceTime 885520 mcs

Statistic test(BoruvkaThreaded) started.
Created: 0 kb 86 mcs           Total: 2124 kb 86 mcs
Done:    0 kb 50465 mcs        Total: 2125 kb 50552 mcs
test(BoruvkaThreaded) size=47 weight=4667.0 (finished), serviceTime 884020 mcs

GRAPH nodes=100 edges=1000

Statistic test(BoruvkaOneQueue) started.
Created: 0 kb 107 mcs          Total: 2185 kb 107 mcs
Done:    0 kb 84012 mcs        Total: 2185 kb 84120 mcs
test(BoruvkaOneQueue) size=77 weight=4733.0 (finished), serviceTime 874164 mcs

Statistic test(Boruvka) started.
Created: 0 kb 72 mcs           Total: 2185 kb 72 mcs
Done:    0 kb 112563 mcs       Total: 2186 kb 112636 mcs
test(Boruvka) size=99 weight=6070.0 (finished), serviceTime 888416 mcs

Statistic test(BoruvkaThreaded) started.
Created: 0 kb 110 mcs          Total: 2185 kb 110 mcs
Done:    0 kb 78875 mcs        Total: 2186 kb 78985 mcs
test(BoruvkaThreaded) size=98 weight=27981.0 (finished), serviceTime 872853 mcs

GRAPH nodes=1000 edges=10000

Statistic test(BoruvkaOneQueue) started.
Created: 0 kb 72 mcs           Total: 3307 kb 72 mcs
Done:    3 kb 878714 mcs       Total: 3311 kb 878787 mcs
test(BoruvkaOneQueue) size=745 weight=426887.0 (finished), serviceTime 920201 mcs

Statistic test(Boruvka) started.
Created: 0 kb 132 mcs          Total: 3307 kb 132 mcs
Done:    8 kb 1141820 mcs      Total: 3316 kb 1141953 mcs
test(Boruvka) size=999 weight=584213.0 (finished), serviceTime 910577 mcs

Statistic test(BoruvkaThreaded) started.
Created: 0 kb 111 mcs          Total: 3312 kb 111 mcs
Done:    -25 kb 691533 mcs     Total: 3287 kb 691644 mcs
test(BoruvkaThreaded) size=991 weight=2531243.0 (finished), serviceTime 917091 mcs

GRAPH nodes=10000 edges=100000

Statistic test(BoruvkaOneQueue) started.
Created: 0 kb 73 mcs           Total: 14725 kb 73 mcs
Done:    39 kb 8655657 mcs     Total: 14764 kb 8655730 mcs
test(BoruvkaOneQueue) size=7493 weight=4.3905939E7 (finished), serviceTime 1089726 mcs

Statistic test(Boruvka) started.
Created: 0 kb 94 mcs           Total: 14725 kb 94 mcs
Done:    94 kb 11090437 mcs    Total: 14819 kb 11090532 mcs
test(Boruvka) size=9999 weight=6.0401393E7 (finished), serviceTime 1136337 mcs

Statistic test(BoruvkaThreaded) started.
Created: 0 kb 80 mcs           Total: 14780 kb 80 mcs
Done:    -280 kb 7924407 mcs   Total: 14499 kb 7924488 mcs
test(BoruvkaThreaded) size=9995 weight=2.44613851E8 (finished), serviceTime 1140250 mcs

GRAPH nodes=100000 edges=1000000

Statistic test(BoruvkaOneQueue) started.
Created: 0 kb 97 mcs           Total: 126413 kb 97 mcs
Done:    390 kb 87156850 mcs   Total: 126803 kb 87156947 mcs
test(BoruvkaOneQueue) size=74928 weight=4.350872675E9 (finished), serviceTime 1846606 mcs

*/

/* 4 THREADS + 1ms wait

Statistic test(BoruvkaOneQueue) started.
Created: -284 kb 213 mcs       Total: 1849 kb 213 mcs
Done:    53 kb 16039 mcs       Total: 1903 kb 16253 mcs
test(BoruvkaOneQueue) size=4 weight=1.0 (finished), serviceTime 859685 mcs

Statistic test(Boruvka) started.
Created: 0 kb 151 mcs          Total: 1942 kb 151 mcs
Done:    0 kb 5693 mcs         Total: 1942 kb 5845 mcs
test(Boruvka) size=6 weight=7.0 (finished), serviceTime 863410 mcs

Statistic test(BoruvkaThreaded) started.
Created: 1 kb 2449 mcs         Total: 1944 kb 2449 mcs
Done:    1 kb 9630 mcs         Total: 1945 kb 12079 mcs
test(BoruvkaThreaded) size=6 weight=13.0 (finished), serviceTime 857454 mcs

GRAPH nodes=50 edges=500

Statistic test(BoruvkaOneQueue) started.
Created: 0 kb 120 mcs          Total: 2125 kb 120 mcs
Done:    0 kb 52064 mcs        Total: 2125 kb 52184 mcs
test(BoruvkaOneQueue) size=39 weight=1250.0 (finished), serviceTime 877782 mcs

Statistic test(Boruvka) started.
Created: 0 kb 79 mcs           Total: 2125 kb 79 mcs
Done:    0 kb 64087 mcs        Total: 2125 kb 64166 mcs
test(Boruvka) size=49 weight=1566.0 (finished), serviceTime 889049 mcs

Statistic test(BoruvkaThreaded) started.
Created: 0 kb 85 mcs           Total: 2125 kb 85 mcs
Done:    0 kb 32541 mcs        Total: 2124 kb 32627 mcs
test(BoruvkaThreaded) size=47 weight=3248.0 (finished), serviceTime 864311 mcs

GRAPH nodes=100 edges=1000

Statistic test(BoruvkaOneQueue) started.
Created: 0 kb 103 mcs          Total: 2186 kb 103 mcs
Done:    0 kb 85688 mcs        Total: 2186 kb 85791 mcs
test(BoruvkaOneQueue) size=74 weight=3635.0 (finished), serviceTime 877935 mcs

Statistic test(Boruvka) started.
Created: 0 kb 108 mcs          Total: 2186 kb 108 mcs
Done:    1 kb 122737 mcs       Total: 2187 kb 122845 mcs
test(Boruvka) size=99 weight=5189.0 (finished), serviceTime 889378 mcs

Statistic test(BoruvkaThreaded) started.
Created: 0 kb 99 mcs           Total: 2187 kb 99 mcs
Done:    -10 kb 46521 mcs      Total: 2176 kb 46621 mcs
test(BoruvkaThreaded) size=97 weight=12918.0 (finished), serviceTime 875643 mcs

GRAPH nodes=1000 edges=10000

Statistic test(BoruvkaOneQueue) started.
Created: 0 kb 69 mcs           Total: 3308 kb 69 mcs
Done:    3 kb 941438 mcs       Total: 3312 kb 941508 mcs
test(BoruvkaOneQueue) size=751 weight=440896.0 (finished), serviceTime 933822 mcs

Statistic test(Boruvka) started.
Created: 0 kb 118 mcs          Total: 3308 kb 118 mcs
Done:    8 kb 1202148 mcs      Total: 3317 kb 1202267 mcs
test(Boruvka) size=999 weight=615450.0 (finished), serviceTime 932681 mcs

Statistic test(BoruvkaThreaded) started.
Created: 0 kb 78 mcs           Total: 3313 kb 78 mcs
Done:    -24 kb 373193 mcs     Total: 3289 kb 373272 mcs
test(BoruvkaThreaded) size=991 weight=1894680.0 (finished), serviceTime 915485 mcs

GRAPH nodes=10000 edges=100000

Statistic test(BoruvkaOneQueue) started.
Created: 0 kb 73 mcs           Total: 14725 kb 73 mcs
Done:    39 kb 8881674 mcs     Total: 14765 kb 8881747 mcs
test(BoruvkaOneQueue) size=7526 weight=4.3919432E7 (finished), serviceTime 1085425 mcs

Statistic test(Boruvka) started.
Created: 0 kb 74 mcs           Total: 14725 kb 74 mcs
Done:    94 kb 10973431 mcs    Total: 14820 kb 10973506 mcs
test(Boruvka) size=9999 weight=6.0087893E7 (finished), serviceTime 1073551 mcs

Statistic test(BoruvkaThreaded) started.
Created: 0 kb 113 mcs          Total: 14780 kb 113 mcs
Done:    -277 kb 4015889 mcs   Total: 14503 kb 4016003 mcs
test(BoruvkaThreaded) size=9959 weight=1.88071639E8 (finished), serviceTime 1106020 mcs

*/



/* Boruvka

GRAPH nodes=7 edges=11

Statistic test(Boruvka) started.
Created: 418 kb 229 mcs        Total: 1802 kb 229 mcs
Done:    62 kb 11438 mcs       Total: 1865 kb 11667 mcs
test(Boruvka) size=6 weight=7.0 (finished), serviceTime 871889 mcs

Statistic test(Boruvka) started.
Created: 0 kb 70 mcs           Total: 1935 kb 70 mcs
Done:    0 kb 649 mcs          Total: 1935 kb 720 mcs
test(Boruvka) size=6 weight=15.0 (finished), serviceTime 874411 mcs

Statistic test(Boruvka) started.
Created: 0 kb 70 mcs           Total: 1935 kb 70 mcs
Done:    0 kb 441 mcs          Total: 1935 kb 512 mcs
test(Boruvka) size=6 weight=15.0 (finished), serviceTime 873442 mcs

GRAPH nodes=50 edges=500

Statistic test(Boruvka) started.
Created: 0 kb 171 mcs          Total: 2111 kb 171 mcs
Done:    0 kb 12629 mcs        Total: 2112 kb 12801 mcs
test(Boruvka) size=49 weight=1488.0 (finished), serviceTime 882334 mcs

Statistic test(Boruvka) started.
Created: 0 kb 83 mcs           Total: 2112 kb 83 mcs
Done:    0 kb 2547 mcs         Total: 2112 kb 2630 mcs
test(Boruvka) size=49 weight=9857.0 (finished), serviceTime 887724 mcs

Statistic test(Boruvka) started.
Created: 0 kb 81 mcs           Total: 2112 kb 81 mcs
Done:    0 kb 1562 mcs         Total: 2112 kb 1643 mcs
test(Boruvka) size=49 weight=9857.0 (finished), serviceTime 883364 mcs

GRAPH nodes=100 edges=1000

Statistic test(Boruvka) started.
Created: 0 kb 103 mcs          Total: 2173 kb 103 mcs
Done:    1 kb 5094 mcs         Total: 2175 kb 5198 mcs
test(Boruvka) size=99 weight=6152.0 (finished), serviceTime 887967 mcs

Statistic test(Boruvka) started.
Created: 0 kb 102 mcs          Total: 2174 kb 102 mcs
Done:    0 kb 1296 mcs         Total: 2175 kb 1399 mcs
test(Boruvka) size=99 weight=42833.0 (finished), serviceTime 887568 mcs

Statistic test(Boruvka) started.
Created: 0 kb 76 mcs           Total: 2174 kb 76 mcs
Done:    0 kb 1283 mcs         Total: 2175 kb 1359 mcs
test(Boruvka) size=99 weight=42833.0 (finished), serviceTime 887520 mcs

GRAPH nodes=1000 edges=10000

Statistic test(Boruvka) started.
Created: 0 kb 72 mcs           Total: 3300 kb 72 mcs
Done:    8 kb 29812 mcs        Total: 3309 kb 29885 mcs
test(Boruvka) size=999 weight=579202.0 (finished), serviceTime 909720 mcs

Statistic test(Boruvka) started.
Created: 0 kb 104 mcs          Total: 3305 kb 104 mcs
Done:    4 kb 20471 mcs        Total: 3309 kb 20576 mcs
test(Boruvka) size=999 weight=4260517.0 (finished), serviceTime 915302 mcs

Statistic test(Boruvka) started.
Created: 0 kb 29 mcs           Total: 3306 kb 29 mcs
Done:    3 kb 18060 mcs        Total: 3310 kb 18090 mcs
test(Boruvka) size=999 weight=4260517.0 (finished), serviceTime 927228 mcs

GRAPH nodes=10000 edges=100000

Statistic test(Boruvka) started.
Created: 0 kb 76 mcs           Total: 14739 kb 76 mcs
Done:    94 kb 222503 mcs      Total: 14833 kb 222579 mcs
test(Boruvka) size=9999 weight=5.9729806E7 (finished), serviceTime 1082317 mcs

Statistic test(Boruvka) started.
Created: 0 kb 73 mcs           Total: 14794 kb 73 mcs
Done:    0 kb 348630 mcs       Total: 14793 kb 348704 mcs
test(Boruvka) size=9999 weight=4.36501051E8 (finished), serviceTime 1078798 mcs

Statistic test(Boruvka) started.
Created: 0 kb 97 mcs           Total: 14754 kb 97 mcs
Done:    59 kb 308463 mcs      Total: 14814 kb 308560 mcs
test(Boruvka) size=9999 weight=4.36501051E8 (finished), serviceTime 1123522 mcs

*/


/* BoruvkaDequeue

GRAPH nodes=7 edges=11

Statistic test(BoruvkaDequeue) started.
Created: 418 kb 252 mcs        Total: 1802 kb 252 mcs
Done:    62 kb 4417 mcs        Total: 1865 kb 4669 mcs
test(BoruvkaDequeue) size=6 weight=7.0 (finished), serviceTime 832921 mcs

Statistic test(BoruvkaDequeue) started.
Created: 0 kb 72 mcs           Total: 1934 kb 72 mcs
Done:    0 kb 133 mcs          Total: 1934 kb 206 mcs
test(BoruvkaDequeue) size=6 weight=15.0 (finished), serviceTime 862496 mcs

Statistic test(BoruvkaDequeue) started.
Created: 0 kb 69 mcs           Total: 1934 kb 69 mcs
Done:    0 kb 356 mcs          Total: 1934 kb 426 mcs
test(BoruvkaDequeue) size=6 weight=15.0 (finished), serviceTime 872769 mcs

GRAPH nodes=50 edges=500

Statistic test(BoruvkaDequeue) started.
Created: 0 kb 86 mcs           Total: 2111 kb 86 mcs
Done:    0 kb 8428 mcs         Total: 2112 kb 8514 mcs
test(BoruvkaDequeue) size=49 weight=1333.0 (finished), serviceTime 884624 mcs

Statistic test(BoruvkaDequeue) started.
Created: 0 kb 113 mcs          Total: 2112 kb 113 mcs
Done:    0 kb 1402 mcs         Total: 2112 kb 1515 mcs
test(BoruvkaDequeue) size=49 weight=9076.0 (finished), serviceTime 877385 mcs

Statistic test(BoruvkaDequeue) started.
Created: 0 kb 83 mcs           Total: 2112 kb 83 mcs
Done:    0 kb 1196 mcs         Total: 2112 kb 1279 mcs
test(BoruvkaDequeue) size=49 weight=9076.0 (finished), serviceTime 884524 mcs

GRAPH nodes=100 edges=1000

Statistic test(BoruvkaDequeue) started.
Created: 0 kb 74 mcs           Total: 2173 kb 74 mcs
Done:    1 kb 6710 mcs         Total: 2175 kb 6784 mcs
test(BoruvkaDequeue) size=99 weight=5647.0 (finished), serviceTime 885814 mcs

Statistic test(BoruvkaDequeue) started.
Created: 0 kb 103 mcs          Total: 2174 kb 103 mcs
Done:    0 kb 1276 mcs         Total: 2175 kb 1380 mcs
test(BoruvkaDequeue) size=99 weight=44117.0 (finished), serviceTime 887761 mcs

Statistic test(BoruvkaDequeue) started.
Created: 0 kb 106 mcs          Total: 2174 kb 106 mcs
Done:    0 kb 559 mcs          Total: 2175 kb 666 mcs
test(BoruvkaDequeue) size=99 weight=44117.0 (finished), serviceTime 863154 mcs

GRAPH nodes=1000 edges=10000

Statistic test(BoruvkaDequeue) started.
Created: 0 kb 70 mcs           Total: 3295 kb 70 mcs
Done:    8 kb 39084 mcs        Total: 3304 kb 39155 mcs
test(BoruvkaDequeue) size=999 weight=587449.0 (finished), serviceTime 912100 mcs

Statistic test(BoruvkaDequeue) started.
Created: 0 kb 74 mcs           Total: 3300 kb 74 mcs
Done:    4 kb 14448 mcs        Total: 3305 kb 14523 mcs
test(BoruvkaDequeue) size=999 weight=4249285.0 (finished), serviceTime 923959 mcs

Statistic test(BoruvkaDequeue) started.
Created: 0 kb 30 mcs           Total: 3301 kb 30 mcs
Done:    4 kb 14603 mcs        Total: 3306 kb 14633 mcs
test(BoruvkaDequeue) size=999 weight=4249285.0 (finished), serviceTime 930377 mcs

GRAPH nodes=10000 edges=100000

Statistic test(BoruvkaDequeue) started.
Created: 0 kb 71 mcs           Total: 14727 kb 71 mcs
Done:    94 kb 193668 mcs      Total: 14822 kb 193740 mcs
test(BoruvkaDequeue) size=9999 weight=6.0558222E7 (finished), serviceTime 1098813 mcs

Statistic test(BoruvkaDequeue) started.
Created: 0 kb 74 mcs           Total: 14782 kb 74 mcs
Done:    7 kb 316232 mcs       Total: 14790 kb 316307 mcs
test(BoruvkaDequeue) size=9999 weight=4.38363809E8 (finished), serviceTime 1113518 mcs

Statistic test(BoruvkaDequeue) started.
Created: 0 kb 75 mcs           Total: 14750 kb 75 mcs
Done:    59 kb 362514 mcs      Total: 14810 kb 362589 mcs
test(BoruvkaDequeue) size=9999 weight=4.38363809E8 (finished), serviceTime 1111478 mcs

 */