package ru.progwards.java2.lessons.recursion;

import java.util.Arrays;

public class HanoiTower {
    // Решить задачу Ханойской башни методом рекурсии. Ханойская башня - детская головоломка,
    // представленная на картинке ниже. Задача - перенести башню с первого штырька на второй,
    // используя 3-й как промежуточный. При этом запрещено класть большее кольцо на меньшее.

    private boolean isTraceOn = false;
    private int[] pinTop; // верхнее кольцо штыря (0-2)
//    private int srcPin;   // штырь-источник
//    private int destPin;  // штырь-приемник
//    private int freePin;  // штырь-помощник
    private int[] rings;  // местонахождение колец (0..size-1)
    private int size;

    // инициализирует башню с size кольцами (1..size). pos - номер начального штыря (0,1,2)
    public HanoiTower(int size, int pos) {
//        srcPin = pos;
//        destPin = (pos+1)%3;
//        freePin = (pos+2)%3;

        pinTop = new int[]{-1, -1, -1};
        pinTop[pos] = 0;

        rings = new int[size];
        Arrays.fill(rings, pos);

        this.size = size;
    }

    // перенести одно кольцо
    void step(int ringNo, int from, int to) {
        if (pinTop[from] != ringNo) {
            throw new RuntimeException("Не могу перенести кольцо " + pinName(ringNo) + " с " + from + " на " + to + ": кольца нет на " + from);
        }
        if (pinTop[to]>=0 && pinTop[to] <= pinTop[from]) {
            throw new RuntimeException("Не могу перенести кольцо " + pinName(ringNo) + " с " + from + " на " + to + ": на " + to + " лежит " + pinTop[to]);
        }
        rings[ringNo] = to;
        pinTop[to] = ringNo;
        int i = ringNo+1;
        while (i < size && rings[i] != from) i++;
        pinTop[from] = i == size ? -1 : i;
        if (isTraceOn) print();
    }

    // посчитать сколько колец над нужным кольцом
    int calcAbove(int ringNo) {
        int pinNo = rings[ringNo];
        int result = 0;
        int i = 0;
        while (i != ringNo) {
            if(rings[i]==pinNo) result++;
            i++;
        }
        return result;
    }

    // переносит башню со штыря from на штырь to
    public void move(int from, int to) {
        if (size == 0) {
            throw new RuntimeException("Башни то нет! (size=0)");
        }
        if (rings[0] != from) {
            throw new RuntimeException("Верхнее кольцо не на " + from + ", а на " + rings[0]);
        }
        if (from == to || from<0 || from >2 || to<0||to>2) {
            throw new RuntimeException("Входные параметры заданы не верно, from=" + from+", to="+to+", from!=to");
        }
//        srcPin = from;
//        destPin = to;
//        freePin = (from + 1) % 3;
//        if (freePin == destPin) freePin = (destPin + 1) % 3;
        resolve(size-1,from,to);
    }

//    public void resolve1() {
//        if (pinTop[srcPin] == -1) return;
//        int ringNo = pinTop[srcPin] + 2;
//        if (ringNo >= size) ringNo = size - 1;
//
//        int above = calcAbove(ringNo);
//        switch (above) {
//            case 2:
//                step(ringNo - 2, srcPin, destPin);
//                step(ringNo - 1, srcPin, freePin);
//                step(ringNo - 2, destPin, freePin);
//                step(ringNo, srcPin, destPin);
//                step(ringNo - 2, freePin, srcPin);
//                step(ringNo - 1, freePin, destPin);
//                step(ringNo - 2, srcPin, destPin);
//                break;
//            case 1:
//                step(ringNo - 1, srcPin, freePin);
//                step(ringNo, srcPin, destPin);
//                step(ringNo - 1, freePin, destPin);
//                break;
//            case 0:
//                step(ringNo, srcPin, destPin);
//                break;
//        }
//        resolve1();
//    }

    public void resolve(int ringNo, int from, int to) {
        if (ringNo == 0) {
            step(ringNo, from, to);
            return;
        }
        int free = (from + 1) % 3;
        if (free == to) free = (to + 1) % 3;
        resolve(ringNo - 1, from, free);
        step(ringNo, from, to);
        resolve(ringNo - 1, free, to);
    }

    String pinName(int ringNo) {
        return String.format("<%03d>", ringNo+1);
    }
    // выводит текущее состояние башни на консоль в формате
    void print() {
        String[] strings = new String[size];
        for (int p = 0; p < 3; p++) {
            int cnt = 0;
            for (int i = size-1; i >=0; i--)
                if(rings[i] == p) {
                    strings[cnt] = (strings[cnt] == null ? "" : strings[cnt] + " ") + (rings[i] == p ? pinName(i) : "  I  ");
                    cnt++;
                }
            for(; cnt<size; cnt++)
                strings[cnt] = (strings[cnt] == null ? "" : strings[cnt] + " ") + "  I  ";
        }
        for (int i = size-1; i >= 0; i--) {
            System.out.println(strings[i]);
        }
        System.out.println("=".repeat(17));
    }

    // включает отладочную печать состояния игрового поля после каждого шага алгоритма (метода move)
    void setTrace(boolean on) {
        isTraceOn = on;
    }

    public static void main(String[] args) {
        HanoiTower t = new HanoiTower(3, 0);
        //t.setTrace(true);
        t.print();
        t.move(0,2);
        t.print();
    }
}
