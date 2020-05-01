package ru.progwards.java2.lessons.patterns.proxy;

public class GpsProcessor {

    IGpsStreamer stream;
    Thread thread;

    public GpsProcessor(IGpsStreamer stream) {
        this.stream = stream;
        thread = new Thread(new PrintStreamData(stream));
        thread.start();
    }

    public void interrupt() {
        thread.interrupt();
    }


    private class PrintStreamData implements Runnable {

        IGpsStreamer stream;

        public PrintStreamData(IGpsStreamer stream) {
            this.stream = stream;
        }

        @Override
        public void run() {
            Thread thread = Thread.currentThread();
            while (!thread.isInterrupted()) {
                try {
                    System.out.println("Read: "+stream.get());
                } catch (InterruptedException e) {
                    thread.interrupt();
                }
            }
        }
    }
}