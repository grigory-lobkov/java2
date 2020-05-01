package ru.progwards.java2.lessons.patterns.proxy;

public interface IGpsStreamer {

    void add(GPS gps) throws InterruptedException;

    GPS get() throws InterruptedException;

}
