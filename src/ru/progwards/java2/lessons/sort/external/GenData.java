package ru.progwards.java2.lessons.sort.external;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.concurrent.ThreadLocalRandom;



public class GenData {

    final static String FILE_NAME = "C:\\TEMP\\data.txt";

    final static long VALUES_COUNT = 200_000_000;

    static void generate() {
        PrintWriter file = null;
        try {
            file = new PrintWriter(new FileOutputStream(new File(FILE_NAME)));
            for(long i=0; i<VALUES_COUNT; i++)
                file.println(ThreadLocalRandom.current().nextInt());
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (file != null)
                file.close();
        }
    }

    public static void main(String[] args) {
        System.out.println("Generating "+FILE_NAME+" ...");
        long start = System.currentTimeMillis();
        generate();
        System.out.println("Done. Execution time: "+(System.currentTimeMillis()-start)+" ms");
        // VALUES_COUNT = 2_000_000 -> 419 ms
        // VALUES_COUNT = 20_000_000 -> 2022 ms
        // VALUES_COUNT = 200_000_000 -> 15881 ms
    }

}
