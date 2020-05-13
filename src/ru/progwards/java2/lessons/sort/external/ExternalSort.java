package ru.progwards.java2.lessons.sort.external;


import ru.progwards.java2.lessons.sort.simple.InsertionSort;
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

Можно: Легко добавить многопоточность при объединении групп файлов, т.к. данные различны
*/
public class ExternalSort<T extends Comparable> {

    final int MAX_BLOCK_SIZE = 10_000; // количество элементов, сортируемое за один раз
    final int MAX_FILES_COUNT = 200; // количество файлов, открываемых одновременно для слияния

    // VALUES_COUNT = 20_000_000
    //oneBlockSorter = a -> QuickSort.sortHoare(a, 0, a.length - 1);
    //mergeSorter = a -> Arrays.sort(a);
    // BlockSize, FileCount -> sec
    // 10_000, 2000 -> 310 s
    // 10_000, 200 -> 45 s
    // 10_000, 150 -> 38 s
    // 10_000, 100 -> 36 s
    // 10_000, 50 -> 35 s
    // 10_000, 30 -> 32 s
    // 10_000, 20 -> 30 s
    // 10_000, 10 -> 26 s
    // 10_000, 5 -> 28 s
    // 10_000, 2 -> 43 s
    //mergeSorter = a -> QuickSort.sortHoare(a, 0, a.length - 1);
    // 10_000, 200 -> 154 s
    //mergeSorter = a -> ShellSort.sort(a);
    // 10_000, 200 -> 212 s
    //mergeSorter = a -> InsertionSort.sort(a);
    // 10_000, 2000 -> 488 s
    // 10_000, 200 -> 49 s
    //mergeSorter = a -> InsertionSort.sortZeroQuick(a);
    // 10_000, 2000 -> 18 s
    // 10_000, 200 -> 16 s

    // VALUES_COUNT = 200_000_000
    //oneBlockSorter = a -> QuickSort.sortHoare(a, 0, a.length - 1);
    //mergeSorter = a -> InsertionSort.sortZeroQuick(a);
    // BlockSize, FileCount -> sec
    // 10_000,20000 -> 633 s
    // 10_000, 2000 -> 219 s
    // 10_000,  500 -> 184 s
    // 10_000,  200 -> 178 s
    // 10_000,  150 -> 199 s
    // 10_000,  100 -> 252 s
    // 10_000,   30 -> 240 s
    // при этом одно ядро из четырех загружено полностью, а диск лишь на 30МБ/с из 1ТБ/с - перспективно для многопоточности.
    // К сожалению, не знаю, как ещё снизить нагрузку на процессор
    // Файлов много лучше не открывать, особенно для носителей с последовательным доступом, чтобы жесткий диск не молотил головками. Число файлов надо подбирать экспериментально.

    final String SORT_FILES_PREFIX = "C:\\TEMP\\sort"; // имена временных файлов начинаются с
    final String SORT_FILES_POSTFIX = ".txt";  // имена временных файлов заканчиваются на

    File inFile;
    String outFileName;
    Function<String, T> lineToValue;
    Function<T, String> valueToLine;
    Consumer<Comparable[]> oneBlockSorter;
    Consumer<Comparable[]> mergeSorter;
    int sortFilesCount = 0;
    int mergesCount = 0;
    String fileAddPrefix = "";

    public ExternalSort(String inFileName, String outFileName,
                        Function<String, T> lineToValue, Function<T, String> valueToLine,
                        Consumer<Comparable[]> oneBlockSorter, Consumer<Comparable[]> mergeSorter) {
        this.inFile = new File(inFileName);
        this.outFileName = outFileName;
        this.lineToValue = lineToValue;
        this.valueToLine = valueToLine;
        this.oneBlockSorter = oneBlockSorter;
        this.mergeSorter = mergeSorter;
    }


    /**
     * Разбиение файла на много отсортированных файлов
     */
    private void splitAndSort() {
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

    /**
     * Отсортировать даные и сохранить в файл
     *
     * @param data
     */
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

    /**
     * Проверка, можем ли мы делать окончательное слияние. Если нет - будем объединять пока не сможем
     *
     * @throws IOException
     */
    private void checkMerge() throws IOException {
        while (sortFilesCount > MAX_FILES_COUNT) {
            int step = Math.min(MAX_FILES_COUNT, (sortFilesCount+MAX_FILES_COUNT-1) / MAX_FILES_COUNT);
            System.out.println("checkMerge(), sortFilesCount=" + sortFilesCount+", step="+step);
            int i = 0;
            int newFilesCount = 0;
            String newAddPrefix = mergesCount + "-";
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

    /**
     * Вспомогательный класс для многопутевого слияния файлов
     * Класс обеспечивает поток объектов {@code <T>}
     */
    class mergeSource implements Comparable {
        String fileName;
        File file;
        FileReader fr;
        BufferedReader br;
        T nextValue;
        public boolean hasNext;
        int cnt = 0;

        mergeSource(String fileName) throws IOException {
            this.fileName = fileName;
            file = new File(fileName);
            fr = new FileReader(file);
            br = new BufferedReader(fr);
            String line;
            hasNext = (line = br.readLine()) != null;
            nextValue = hasNext ? lineToValue.apply(line) : null;
        }

        public T get() throws IOException {
            T result = nextValue;
            if(hasNext) {
                cnt++;
                String line;
                hasNext = (line = br.readLine()) != null;
                nextValue = hasNext ? lineToValue.apply(line) : null;
            }
            return result;
        }

        public void close() throws IOException {
            br.close();
            fr.close();
            file.delete();
        }

        @Override
        public int compareTo(Object o) {
            return nextValue.compareTo(((mergeSource)o).nextValue);
        }
    }

    /**
     * Сбалансированное многопутевое слияение файлов
     * Открываем каждый файл и через буфер считываем построчно
     *
     * @param sourceFiles
     * @param resultName
     */
    private void mergeFiles(String[] sourceFiles, String resultName) throws IOException {

        // Сообщим в консоль, что делаем

//        System.out.print("mergeFiles( "+sourceFiles[0]);
//        for (int k = 1; k < sourceFiles.length; k++)
//            System.out.print(", "+sourceFiles[k]);
//        System.out.println(" -> "+resultName+" )");

        // Открываем потоки входящих файлов

        Comparable[] sources = new Comparable[sourceFiles.length];
        for (int k = 0; k < sourceFiles.length; k++)
            sources[k] = new mergeSource(sourceFiles[k]);

        // Открываем файл для записи

        File resultFile = new File(resultName);
        FileWriter resultWriter = new FileWriter(resultFile);
        BufferedWriter resultBWriter = new BufferedWriter(resultWriter);

        // Основной цикл

        mergeSorter.accept(sources);
        while (sources.length>0) {
            mergeSource topSource = (mergeSource)sources[0];
            T minValue = topSource.get();
            resultBWriter.write(valueToLine.apply(minValue)+"\n");
            if(topSource.hasNext) {
                mergeSorter.accept(sources);
            } else {
                topSource.close();
                int newLen = sources.length - 1;
                Comparable[] newSource = new Comparable[newLen];
                System.arraycopy(sources, 1, newSource, 0, newLen);
                sources = newSource;
            }
        }

        // Закрываем файл
        resultBWriter.close();
        resultWriter.close();
    }

    /**
     * Выполнить слияние всех оставшихся файлов
     *
     * @throws IOException
     */
    private void merge() throws IOException {
        System.out.println("merge(), sortFilesCount="+sortFilesCount);
        String[] sourceFiles = new String[sortFilesCount];
        for(int k = 0; k<sortFilesCount; k++)
            sourceFiles[k] = SORT_FILES_PREFIX+fileAddPrefix+k+SORT_FILES_POSTFIX;
        mergeFiles(sourceFiles, outFileName);
    }

    /**
     * Отсортировать файл {@code inFileName} и сохранить как {@code outFileName}
     *
     * @param inFileName
     * @param outFileName
     * @throws IOException
     */
    static void sort(String inFileName, String outFileName) throws IOException {
        Function<String, Integer> lineToValue = str -> Integer.valueOf(str);
        Function<Integer, String> valueToLine = val -> val.toString();
        Consumer<Comparable[]> oneBlockSorter = a -> QuickSort.sortHoare(a, 0, a.length - 1);
        //Consumer<Comparable[]> mergeSorter = a -> Arrays.sort(a);
        //Consumer<Comparable[]> mergeSorter = a -> QuickSort.sortHoare(a, 0, a.length - 1);
        //Consumer<Comparable[]> mergeSorter = a -> ShellSort.sort(a);
        //Consumer<Comparable[]> mergeSorter = a -> InsertionSort.sort(a);
        Consumer<Comparable[]> mergeSorter = a -> InsertionSort.sortZeroQuick(a);

        ExternalSort<Integer> s = new ExternalSort(inFileName, outFileName, lineToValue, valueToLine, oneBlockSorter, mergeSorter);
        s.splitAndSort();
        s.checkMerge();
        s.merge();
    }

    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();
        sort("C:\\TEMP\\data.txt", "C:\\TEMP\\sorted.txt");
        System.out.println("Execution time: "+(System.currentTimeMillis()-start)/1000+" s");
    }

}