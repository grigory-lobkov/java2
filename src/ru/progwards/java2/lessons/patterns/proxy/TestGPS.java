package ru.progwards.java2.lessons.patterns.proxy;

import java.util.Random;
import java.util.SplittableRandom;

public class TestGPS {

    final static int RAPID_FIRST_VALUES = 50+1; // надо брать на 1 больше чем объем статистики, т.к. по первой точке скорость не посчитается
    final static int SLEEP_AFTER_EACH_MS = 100;
    final static int EXPERIMENT_DURATION_MS = 3000;

    //final static IGpsStreamer streamer = new GpsStreamer();
    final static IGpsStreamer streamer = new GpsProxy(new GpsStreamer());
    final static GpsProcessor processor = new GpsProcessor(streamer);

    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(new GenerateData());
        thread.start();
        Thread.sleep(EXPERIMENT_DURATION_MS);
        thread.interrupt();
        processor.interrupt();
    }

    private static class GenerateData implements Runnable {

        double lat = 55.751244;
        double lon = 37.618423;
        double maxDelta = 0.000_000_01;
        double speed = maxDelta*4;
        SplittableRandom random = new SplittableRandom();
        long time = System.currentTimeMillis();

        public GenerateData() {
        }

        @Override
        public void run() {
            try {
                for (int i = RAPID_FIRST_VALUES; i >= 0; i--)
                    streamer.add(generateFirstGps(i));

                for (int i = 1; i < RAPID_FIRST_VALUES * 2; i++) {
                    Thread.sleep(SLEEP_AFTER_EACH_MS);
                    streamer.add(generateGps(i));
                }
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
        }

        private GPS generateGps(int i) {
            speed += random.nextDouble(maxDelta * 1.2D) - maxDelta / 2D;
            lat += speed;
            lon += speed;
            double rnd = random.nextInt(3) == 0 ? 100 : 0;

            return new GPS(lat + rnd, lon, time + SLEEP_AFTER_EACH_MS * i);
        }

        private GPS generateFirstGps(int i) {
            speed += random.nextDouble(maxDelta) - maxDelta / 2D;
            lat += speed;
            lon += speed;

            return new GPS(lat, lon, time - SLEEP_AFTER_EACH_MS * i);
        }

    }
}
