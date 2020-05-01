package ru.progwards.java2.lessons.patterns.proxy;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class GpsStreamer implements IGpsStreamer {

    public BlockingQueue<GPS> queue = new LinkedBlockingQueue<GPS>();

    public void add(GPS gps) throws InterruptedException {
        //try {
            queue.put(gps);
        //} catch (InterruptedException e) {
        //    System.out.println("Can't put to queue " + gps + ":");
        //    e.printStackTrace();
        //}
    }

    public GPS get() throws InterruptedException {
        return queue.take();  // use put(gps), take() - blocking methods
    }

}
