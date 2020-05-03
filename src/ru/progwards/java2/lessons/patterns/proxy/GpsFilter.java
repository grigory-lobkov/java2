package ru.progwards.java2.lessons.patterns.proxy;

public enum GpsFilter {
    INSTANCE;

    final int STAT_COUNT = 50;
    GPS[] hist = new GPS[STAT_COUNT]; // statistic
    GPS prevGps = null;
    double[] histV = new double[STAT_COUNT]; // speeds
    int histNextPos = 0;
    double speed;
    GPS gps;
    double sumV; // Сумма скоростей в статистике
    double sumV2; // Сумма квадратов в статистике

    final double LAST_WEIGHT = 0.5D; // вероятность последнего элемента
    final double DECR_MULTIPLIER = Math.pow(LAST_WEIGHT, 1D / STAT_COUNT); // уменьшитель вероятности до LAST_WEIGHT
    final double ALL_WEIGHT = (STAT_COUNT + STAT_COUNT * LAST_WEIGHT) / 2D; // делить сумму скоростей надо на сумму всех вкладов вероятностей (всех весов)

    public void add(GPS gps) {
        if (this.gps != gps)
            speed = prevGps == null ? 0 : calcSpeed(prevGps, gps);

        hist[histNextPos] = gps;
        double oldV = histV[histNextPos];
        histV[histNextPos] = speed;

        // одинаковая вероятность выпадения всех
//        sumV = sumV - oldV + speed;
//        sumV2 = sumV2 - oldV * oldV + speed * speed;

        // вероятность уменьшается по мере отдаления от свежих данных
        sumV = sumV * DECR_MULTIPLIER - oldV * LAST_WEIGHT + speed;
        sumV2 = sumV2 * DECR_MULTIPLIER - oldV * oldV * LAST_WEIGHT + speed * speed;

        gps.v = speed;

        prevGps = gps;
        histNextPos = (histNextPos + 1) % STAT_COUNT;
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

    boolean allowNext(GPS gps) {
        this.gps = gps;
        speed = prevGps == null ? 0 : calcSpeed(prevGps, gps);

        double expectedValue = sumV / ALL_WEIGHT; // Математическое ожидание
        double dispersion = sumV2 / ALL_WEIGHT - expectedValue * expectedValue; // Дисперсия
        double sigma3 = Math.sqrt(dispersion) * 3; // 3 sigma

        // вернем false, если переданная скорость не удовлетворяет правилу "3 sigma"
        boolean result = speed > expectedValue - sigma3 && speed < expectedValue + sigma3;
        if (!result)
            System.out.println(speed * 1_000_000_000 + " [" + (expectedValue - sigma3) * 1_000_000_000 + "," + (expectedValue + sigma3) * 1_000_000_000 + "] = " + result);
        gps.v = speed;
        return result;
    }

}