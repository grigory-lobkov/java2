package ru.progwards.java2.lessons.sort.threads;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

public class ThreadPool implements Executor {

    class Worker extends Thread {
        boolean run;
        Boolean lock;
        Runnable command;

        public Worker() {
            lock = false;
            run = true;
        }

        @Override
        public void run() {
            counter.countDown();
            lock();

            while (run) {
                command.run();
                releaseWorker(this);
            }
        }

        void lock() {
            if (!run)
                return;

            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        void unlock() {
            synchronized (lock) {
                lock.notify();
            }
        }

        void exit() {
            run = false;
            unlock();
        }

        void setRunnable(Runnable command) {
            this.command = command;
        }
    }


    ConcurrentLinkedQueue<Runnable> queue;
    public ConcurrentLinkedQueue<Worker> pool;
    public CountDownLatch counter;
    Worker[] all;

    public ThreadPool(int size) {
        counter = new CountDownLatch(size);
        pool = new ConcurrentLinkedQueue<>();
        queue = new ConcurrentLinkedQueue<>();
        all = new Worker[size];
        for (int i = 0; i < size; i++) {
            Worker worker = new Worker();
            pool.offer(worker);
            worker.start();
            all[i] = worker;
        }
        try {
            counter.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void shutDown(boolean wait) throws InterruptedException {
        if (wait)
            while (queue.size() > 0) {
                Thread.sleep(10);
            }

        for (int i = 0; i < all.length; i++) {
            all[i].exit();
        }
        for (int i = 0; i < all.length; i++) {
            all[i].join();
        }
    }


    @Override
    public void execute(Runnable command) {
        Worker worker = getWorker();
        if (worker != null) {
            worker.setRunnable(command);
            worker.unlock();
        } else {
            queue.offer(command);
        }
    }

    Worker getWorker() {
        return pool.poll();
    }

    void releaseWorker(Worker worker) {
        Runnable command = queue.poll();
        if (command != null) {
            worker.setRunnable(command);
        } else {
            pool.offer(worker);
            worker.lock();
        }
    }
}