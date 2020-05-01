package ru.progwards.java2.lessons.patterns.proxy;


public class GpsProxy extends GpsStreamer {

    final int STAT_COUNT = 50;
    GPS[] hist = new GPS[STAT_COUNT]; // statistic
    GPS prevGps = null;
    double[] histV = new double[STAT_COUNT]; // speeds
    int histNextPos = 0;
    int count = 0;

    @Override
    public void add(GPS gps) throws InterruptedException {
        double speedNow = prevGps == null ? 0 : calcSpeed(prevGps, gps);
        //System.out.println("tryAdd: "+gps+" v="+speedNow);
        if (count <= STAT_COUNT || allowNextSpeed(speedNow)) { // count <= , because we need one more point for speed statistic
            count++;
            hist[histNextPos] = gps;
            if (prevGps != null) {
                histV[histNextPos] = speedNow;
            }
            prevGps = gps;
            histNextPos = (histNextPos + 1) % STAT_COUNT;
            queue.put(gps);
        } else
            System.out.println("Skipped " + gps);
    }

    final double _eQuatorialEarthRadius = 6378.1370D;
    final double _d2r = (Math.PI / 180D);

    private double HaversineInKm(double lat1, double long1, double lat2, double long2) {
        double slat = Math.sin((lat2 - lat1) * _d2r / 2D);
        double slong = Math.sin((long2 - long1) * _d2r / 2D);
        double a = slat * slat + Math.cos(lat1 * _d2r) * Math.cos(lat2 * _d2r) * slong * slong;
        double c = 2D * Math.atan2(Math.sqrt(a), Math.sqrt(1D - a));

        return _eQuatorialEarthRadius * c;
    }

    private double calcSpeed(GPS g1, GPS g2) {
        double distance = HaversineInKm(g1.lat, g1.lon, g2.lat, g2.lon);
        long time = g2.time - g1.time;
        return distance / time;
    }

    private boolean allowNextSpeed(double speed) {
        // для ускорения мат.ожидание и дисперсию надо считать вычитанием удаленного значения и прибавлением добавленного, постоянно храня актуальное состояние - не буду заморачиваться, чтобы было легче проверять
        double expectedValue = calcExpected(); // Математическое ожидание
        double dispersion = calcDispersion(expectedValue); // Дисперсия

        // вернем false, если переданная скорость не удовлетворяет правилу "3 sigma"
        boolean result = speed > expectedValue - dispersion * 3 && speed < expectedValue + dispersion * 3;
        //System.out.println(speed + " ["+(expectedValue - dispersion * 3)+","+(expectedValue + dispersion * 3)+"] = "+result);
        return result;
    }

    //Математическое ожидание
    private double calcExpected() {
        double sum = 0;
        for (double v : histV)
            sum += v;
        return sum / histV.length;
    }

    // Дисперсия
    private double calcDispersion(double exp) {
        double sum = 0;
        for (double v : histV)
            sum += v * v;
        return sum - exp * exp;
    }

}