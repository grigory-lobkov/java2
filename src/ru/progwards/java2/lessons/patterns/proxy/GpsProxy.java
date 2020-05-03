package ru.progwards.java2.lessons.patterns.proxy;


public class GpsProxy implements IGpsStreamer {

    IGpsStreamer streamer;
    final int STAT_COUNT = GpsFilter.INSTANCE.STAT_COUNT;
    int count = 0;

    public GpsProxy(IGpsStreamer streamer) {
        this.streamer = streamer;
    }

    @Override
    public void add(GPS gps) throws InterruptedException {
        if (count <= STAT_COUNT || GpsFilter.INSTANCE.allowNext(gps)) { // count <= , because we need one more point for speed statistic
            count++;
            GpsFilter.INSTANCE.add(gps);

            streamer.add(gps);

        } else
            System.out.println("Skipped " + gps);
    }

    @Override
    public GPS get() throws InterruptedException {

        return streamer.get();

    }

}