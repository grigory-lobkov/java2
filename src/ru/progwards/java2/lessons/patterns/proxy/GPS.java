package ru.progwards.java2.lessons.patterns.proxy;


public class GPS {

    static int nextId = 0;
    int id;

    public double lat; // широта

    public double lon; // долгота

    public long time; // время в мс

    public double v; // рассчитанная по точке скорость


    public GPS(double lat, double lon, long time) {
        this.lat = lat;
        this.lon = lon;
        this.time = time;
        synchronized (GPS.class) {
            int id = nextId++;
        }
    }

    @Override
    public String toString() {
        return "GPS{" +
                "lat=" + String.format("%.5f", lat) +
                ", lon=" + String.format("%.5f", lon) +
                //", time=" + time +
                ", v=" + String.format("%.3f", v*1_000_000_000) +
                "} id="+id;
    }

}