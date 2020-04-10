package ru.progwards.java2.lessons.threads;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PrintScan {

    final static int PRINT_SPEED_MS = 50;
    final static int SCAN_SPEED_MS = 70;
    final static int PRINT_THREAD_COUNT = 5;
    final static int SCAN_THREAD_COUNT = 5;


    // Создать класс PrintScan - МФУ - на котором возможно одновременно выполнять печать и
    // сканирование документов, но нельзя одновременно печатать или сканировать два документа.

    static Lock lockPrint = new ReentrantLock();
    static Lock lockScan = new ReentrantLock();

    // "печатает" страницы документа с именем name - выводит на консоль
    static void print(String name, int pages) {
        System.out.println("Printing "+name+"...");
        lockPrint.lock();
        try {
            for (int p = 1; p <= pages; p++)
                try {
                    Thread.sleep(PRINT_SPEED_MS);
                    System.out.println("print " + name + " page " + p + "   " + Thread.currentThread().getName());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        } finally {
            lockPrint.unlock();
        }
    }
//    print <name> page 1
//    print <name> page 2
//            ...
//    с интервалом в 50 мс. Вместо <name> выводится содержимое name.
//    Пока один документ "печатается", второй не может быть напечатан


    // "сканирует" страницы документа с именем name - выводит на консоль
    static void scan(String name, int pages) {
        System.out.println("Scanning "+name+"...");
        lockScan.lock();
        try {
        for(int p = 1; p<=pages; p++)
            try {
                System.out.println("scan "+name+" page "+p+"   "+Thread.currentThread().getName());
                Thread.sleep(SCAN_SPEED_MS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } finally {
            lockScan.unlock();
        }
    }
    //    scan <name> page 1
    //    scan <name> page 2
    //            ...
    //    с интервалом в 70 мс. Вместо <name> выводится содержимое name. Пока один документ "сканируется", второй не может быть отсканирован

    // Написать тест, который запускает на печатать параллельно 10 документов и запускает
    // на сканирование еще 10 документов параллельно. Потоки создает только main, print и scan
    // содержат только синхронизацию.
    public static void main(String[] args) throws InterruptedException {
        //print("test",5);
        //scan("test",3);
        Thread[] prnt = new Thread[PRINT_THREAD_COUNT];
        Thread[] scan = new Thread[SCAN_THREAD_COUNT];

        for(int i = 0; i < PRINT_THREAD_COUNT; i++) {
            int finalI = i;
            prnt[i] = new Thread(() -> {
                System.out.println("Print "+finalI);
                print("doc"+finalI, 2+(int)(5*Math.random()));
            });
        }
        for(int i = 0; i < SCAN_THREAD_COUNT; i++) {
            int finalI = i;
            scan[i] = new Thread(() -> {
                System.out.println("Scan "+finalI);
                scan("doc" + finalI, 2+(int) (5 * Math.random()));
            });
        }
        System.out.println("Main thread creation done.");

        for(int i = 0; i < PRINT_THREAD_COUNT; i++)
            prnt[i].start();
        for(int i = 0; i < SCAN_THREAD_COUNT; i++)
            scan[i].start();
        System.out.println("Main start done.");

        for(int i = 0; i < PRINT_THREAD_COUNT; i++)
            prnt[i].join();
        for(int i = 0; i < SCAN_THREAD_COUNT; i++)
            scan[i].join();
        System.out.println("Main join done.");
    }
}
/*

Main thread creation done.
Main start done.
Print 0
Scan 2
Scan 3
Print 3
Print 2
Scan 1
Scan 0
Scan 4
Print 1
Printing doc1...
Scanning doc4...
Scanning doc2...
Printing doc3...
Print 4
Printing doc4...
Printing doc0...
Printing doc2...
Scanning doc1...
Scanning doc0...
Scanning doc3...
scan doc4 page 1   Thread-9
print doc1 page 1   Thread-1
scan doc4 page 2   Thread-9
print doc1 page 2   Thread-1
print doc1 page 3   Thread-1
scan doc4 page 3   Thread-9
print doc3 page 1   Thread-3
scan doc4 page 4   Thread-9
print doc3 page 2   Thread-3
scan doc4 page 5   Thread-9
print doc3 page 3   Thread-3
print doc3 page 4   Thread-3
scan doc2 page 1   Thread-7
print doc3 page 5   Thread-3
scan doc2 page 2   Thread-7
print doc4 page 1   Thread-4
print doc4 page 2   Thread-4
scan doc2 page 3   Thread-7
print doc4 page 3   Thread-4
scan doc2 page 4   Thread-7
print doc4 page 4   Thread-4
print doc4 page 5   Thread-4
scan doc1 page 1   Thread-6
print doc0 page 1   Thread-0
scan doc1 page 2   Thread-6
print doc0 page 2   Thread-0
scan doc0 page 1   Thread-5
print doc0 page 3   Thread-0
print doc0 page 4   Thread-0
scan doc0 page 2   Thread-5
print doc0 page 5   Thread-0
scan doc0 page 3   Thread-5
print doc2 page 1   Thread-2
print doc2 page 2   Thread-2
scan doc0 page 4   Thread-5
print doc2 page 3   Thread-2
scan doc3 page 1   Thread-8
scan doc3 page 2   Thread-8
Main join done.

*/