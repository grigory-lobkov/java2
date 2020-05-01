package ru.progwards.java2.lessons.patterns.proxy;

import java.util.Random;
import java.util.SplittableRandom;

public class TestGPS {

    final static int RAPID_FIRST_VALUES = 50+1; // надо брать на 1 больше чем объем статистики, т.к. по первой точке скорость не посчитается
    final static int SLEEP_AFTER_EACH_MS = 100;
    final static int EXPERIMENT_DURATION_MS = 1000;

    //final static IGpsStreamer streamer = new GpsStreamer();
    final static IGpsStreamer streamer = new GpsProxy();
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
        double speed = 0.01;
        SplittableRandom random = new SplittableRandom();

        public GenerateData() {
        }

        @Override
        public void run() {
            try {
                for (int i = RAPID_FIRST_VALUES; i > 0; i--)
                    streamer.add(generateFirstGps(i));

                for (int i = 0; i < RAPID_FIRST_VALUES * 2; i++) {
                    Thread.sleep(SLEEP_AFTER_EACH_MS);
                    streamer.add(generateGps(i));
                }
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
        }

        private GPS generateGps(int i) {
            //speed += random.nextDouble(0.02) - 0.005;
            speed += random.nextDouble(0.01) - 0.01 / 2;
            lat += speed;
            lon += speed;
            long time = System.currentTimeMillis();

            return new GPS(lat + random.nextInt(2)*100, lon, time);
        }

        private GPS generateFirstGps(int i) {
            speed += random.nextDouble(0.02) - 0.01 / 2;
            lat += speed;
            lon += speed;
            long time = System.currentTimeMillis() - SLEEP_AFTER_EACH_MS * i;

            return new GPS(lat, lon, time);
        }

    }
}
