package ru.progwards.java2.lessons.threads;

import java.math.BigInteger;

public class Summator {
    //Реализовать класс Summator который суммирует все числа от 1 до number в несколько потоков.
    //Например для числа 5 должно быть просуммировано 1+2+3+4+5

    int count; // количество потоков

    // инициализирует класс, с указанием в какое количество потоков надо будет
    // проводить суммирование, count - количество потоков.
    Summator(int count) {

        this.count=count;

    }

    public class MakeSum implements Runnable {

        public BigInteger result = BigInteger.ZERO;
        BigInteger from;
        BigInteger to;

        public MakeSum(BigInteger from, BigInteger to) {
            this.from = from;
            this.to = to;
            //System.out.println("Runnable: "+from+" ++ "+to);
        }

        @Override
        public void run() {
            result = Summator.this.sum(from, to);
            //System.out.println("Runnable: "+from+" ++ "+to+" = "+result);
        }
    }

    // запускает потоки выполняющие суммирование, number - число, до которого надо просуммировать числа.
    // Для этого нужно будет разбить весь диапазон суммируемых чисел на блоки равного размера, по
    // количеству потоков. Каждому потоку выдать блок для суммирования от n...m. Например, если мы
    // суммируем 1000 в 3 потока, то первому достанется от 1 до 333 второму от 334 до 666, третьему
    // от 667 до 1000. После чего результат суммирования каждого блока нужно будет инкрементировать
    // в общую сумму и вернуть как результат метода.

     public BigInteger sum(BigInteger number) {

         BigInteger countBig = BigInteger.valueOf((long) count);
         if (number.compareTo(countBig) <= 0 || count <= 0) // если число потоков меньше числа, делаем в один поток
             return sum(BigInteger.ONE, number);

         MakeSum[] runnable = new MakeSum[count];
         Thread[] threads = new Thread[count];
         BigInteger step = number.divide(countBig);
         int cycleTo = count - 1;
         BigInteger from = BigInteger.ZERO;
         for (int i = 0; i < cycleTo; i++) {
             BigInteger nextFrom = from.add(step);
             BigInteger to = nextFrom.subtract(BigInteger.ONE);
             runnable[i] = new MakeSum(from, to);
             threads[i] = new Thread(runnable[i]);
             from = nextFrom;
         }
         runnable[cycleTo] = new MakeSum(from, number);
         threads[cycleTo] = new Thread(runnable[cycleTo]);
         System.out.println("Preparation done. Threads count = "+count);
         for (Thread thread : threads)
             thread.start();
         System.out.println("Threads started.");
         BigInteger result = BigInteger.ZERO;
         for (int i = 0; i < count; i++)
             try {
                 threads[i].join();
                 result = result.add(runnable[i].result);
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
         System.out.println("Threads finished.");
         return result;
     }

     public BigInteger sum(BigInteger from, BigInteger to) {
        BigInteger result = BigInteger.ZERO;
        BigInteger i = from;
        while(i.compareTo(to)<=0) {
            result = result.add(i);
            i = i.add(BigInteger.ONE);
        }
        return result;
     }

     public BigInteger sum1(BigInteger number) {
         return number.compareTo(BigInteger.ONE) >= 0
                 ? number.multiply(number.add(BigInteger.ONE)).divide(BigInteger.TWO)
                 : BigInteger.ZERO;
     }

    public BigInteger sum1(BigInteger from, BigInteger to) {
        return from.compareTo(to) >= 0
                ? sum1(to).subtract(sum1(from))
                : BigInteger.ZERO;
    }

    public static void main(String[] args) {
        Summator s = new Summator(4);
        BigInteger number = BigInteger.valueOf(1234567890);
        Long time = System.currentTimeMillis();
        System.out.println("Got sum = "+s.sum(number));
        System.out.println("Right sum = "+s.sum1(number));
        System.out.println("Spent millis: "+(System.currentTimeMillis()-time));
    }
}

/*
Timings for number = 1234567890 (4 cores in fact):
1: 31306
2: 18386
3: 14961
4: 14840
5: 14830
6: 15316
7: 14824
8: 14936
9: 15660
10: 14794
20: 14882
50: 14585

Preparation done. Threads count = 50
Threads started.
Threads finished.
Got sum = 762078938126809995
Right sum = 762078938126809995
Spent millis: 14585
*/