package ru.progwards.java2.lessons.sort.external;


import ru.progwards.java2.lessons.sort.simple.QuickSort;

import java.io.*;
import java.util.function.Consumer;
import java.util.function.Function;

/*
Задача 1. Класс ExternalSort

Класс должен реализовывать алгоритм внешней сортировки.


Отсортировать 200 млн целых чисел используя 10 тыс ячеек памяти
Файл с числами находится в дополнительных материалах к уроку
Записать результаты в файл sorted.txt

Промежуточные файлы удалить


Сигнатура метода static void sort(String inFileName, String outFileName)
 */
public class ExternalSort<T extends Comparable> {

    final int MAX_BLOCK_SIZE = 100_000; // количество элементов, сортируемое за один раз
    final int MAX_FILES_COUNT = 5; // количество файлов, открываемых одновременно для слияния

    final String SORT_FILES_PREFIX = "sort"; // имена временных файлов начинаются с
    final String SORT_FILES_POSTFIX = ".txt";  // имена временных файлов заканчиваются на

    File inFile;
    String outFileName;
    Function<String, T> lineToValue;
    Function<T, String> valueToLine;
    Consumer<Comparable[]> oneBlockSorter;
    int sortFilesCount;
    int mergesCount;
    String fileAddPrefix;

    public ExternalSort(String inFileName, String outFileName,
                        Function<String, T> lineToValue, Function<T, String> valueToLine, Consumer<Comparable[]> oneBlockSorter) {
        this.inFile = new File(inFileName);
        this.outFileName = outFileName;
        this.lineToValue = lineToValue;
        this.valueToLine = valueToLine;
        this.oneBlockSorter = oneBlockSorter;
    }

    private void splitAndSort() throws FileNotFoundException {
        System.out.println("splitAndSort");
        Comparable[] data = new Comparable[MAX_BLOCK_SIZE];
        sortFilesCount = 0;
        fileAddPrefix = "";
        mergesCount = 0;
        try(
            FileReader fr = new FileReader(inFile);
            BufferedReader br = new BufferedReader(fr);
        ) {
            String line;
            int i = 0;
            while ((line = br.readLine()) != null) {
                data[i++] = lineToValue.apply(line);
                if(i==MAX_BLOCK_SIZE) {
                    sortAndSave(data);
                    i = 0;
                }
            }
            if(i>0) {
                sortAndSave(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sortAndSave(Comparable[] data) {
        oneBlockSorter.accept(data);
        String fileName = SORT_FILES_PREFIX+fileAddPrefix+sortFilesCount+SORT_FILES_POSTFIX;
        try(
                FileWriter fw = new FileWriter(fileName);
                BufferedWriter bw = new BufferedWriter(fw)
        ) {
            for(Object e:data)
                bw.write(valueToLine.apply((T)e)+"\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        sortFilesCount++;
    }

    private void checkMerge() {
        System.out.println("checkMerge(), sortFilesCount=" + sortFilesCount);
        while (sortFilesCount > MAX_FILES_COUNT) {
            int i = 0;
            int newFilesCount = 0;
            String newAddPrefix = mergesCount + "-";
            int step = Math.min(MAX_FILES_COUNT, sortFilesCount / MAX_FILES_COUNT);
            while (i < sortFilesCount) {
                String resultName = SORT_FILES_PREFIX + newAddPrefix + newFilesCount + SORT_FILES_POSTFIX;
                int cnt = Math.min(sortFilesCount - i, step);
                String[] sourceFiles = new String[cnt];
                for (int k = 0; k < cnt; k++)
                    sourceFiles[k] = SORT_FILES_PREFIX + fileAddPrefix + (i + k) + SORT_FILES_POSTFIX;
                mergeFiles(sourceFiles, resultName);
                newFilesCount++;
                i += step;
            }
            fileAddPrefix = newAddPrefix;
            mergesCount++;
            sortFilesCount = newFilesCount;
        }
    }

    private void mergeFiles(String[] sourceFiles, String resultName) {
        System.out.println("mergeFiles(");
        for (int k = 0; k < sourceFiles.length; k++)
            System.out.println(sourceFiles[k]);
        System.out.println(" -> "+resultName+" )");
    }

    private void merge() {
        System.out.println("merge(), sortFilesCount="+sortFilesCount);
        String[] sourceFiles = new String[sortFilesCount];
        for(int k = 0; k<sortFilesCount; k++)
            sourceFiles[k] = SORT_FILES_PREFIX+fileAddPrefix+k+SORT_FILES_POSTFIX;
        mergeFiles(sourceFiles, outFileName);
    }

    static void sort(String inFileName, String outFileName) throws FileNotFoundException {
        Function<String, Integer> lineToValue = str -> Integer.valueOf(str);
        Function<Integer, String> valueToLine = val -> val.toString();
        Consumer<Comparable[]> oneBlockSorter = a -> QuickSort.sortHoare(a, 0, a.length - 1);

        ExternalSort<Integer> s = new ExternalSort(inFileName, outFileName, lineToValue, valueToLine, oneBlockSorter);
        s.sortFilesCount = 10;//s.splitAndSort();
        s.checkMerge();
        s.merge();
    }

    public static void main(String[] args) throws FileNotFoundException {
        long start = System.currentTimeMillis();
        sort("data.txt", "sorted.txt");
        System.out.println("Execution time: "+(System.currentTimeMillis()-start)/1000+" s");
    }

}
