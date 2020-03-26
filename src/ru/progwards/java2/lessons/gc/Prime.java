package ru.progwards.java2.lessons.gc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Prime {
    static List<Integer> list;
    private static int lastChecked;

    static {
        list = new ArrayList<Integer>();
        list.addAll(Arrays.asList(2, 3, 5, 7, 11));
        lastChecked = 12;
    }

    static private void fillArray(int fillTo) {
        for (int num = lastChecked + 1; num <= fillTo; num++) {
            for (int i : list) {
                if (num % i == 0) break;
                if (i * i > num) {
                    list.add(num);
                    break;
                }
            }
        }
        lastChecked = fillTo;
    }

    static int getHigher(int num) {
        int fillTo = (int) Math.sqrt(num + Math.sqrt(num)); // до куда проверяем, интервал между простыми примерно равен корню из простого
        fillArray(fillTo);
        boolean found = false;
        while (!found) {
            num++;
            found = true;
            for (int i : list) {
                if (num % i == 0) {
                    found = false;
                    break;
                }
            }
        }
        return num;
    }
}
