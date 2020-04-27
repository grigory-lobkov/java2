package ru.progwards.java2.lessons.synchro;

import java.util.SplittableRandom;

public class Simposium {

    // вилка
    static class Fork {
        private boolean isFree = true;
        public boolean isFree() {
            return isFree;
        }
        public void setFree(boolean free) {
            isFree = free;
        }
    }

    // философ
    static class Philosopher {

        String name;

        Fork right;// вилка справа
        Fork left;// вилка слева

        long reflectTime;// время, которое философ размышляет в мс
        long eatTime;// время, которое философ ест в мс

        long reflectSum;// суммарное время, которое философ размышлял в мс
        long eatSum;// суммарное время, которое философ ел в мс

        final long REPEATTIME = 500;

        //размышлять. Выводит "размышляет "+ name на консоль с периодичностью 0.5 сек
        void reflect() throws InterruptedException {
            long tStart = System.currentTimeMillis();
            long tNow = tStart;
            long passed = 0;
            boolean isInterrupted = false;
            while (passed < reflectTime) {
                System.out.println("размышляет " + name);
                long needReflect = reflectTime - passed;
                try {
                    Thread.sleep(needReflect > REPEATTIME ? REPEATTIME : needReflect);
                } catch (InterruptedException e) {
                    isInterrupted = true;
                    break;
                } finally {
                    tNow = System.currentTimeMillis();
                    passed = tNow - tStart;
                }
            }
            reflectSum += passed;
            if(isInterrupted) throw new InterruptedException();
        }

        //есть. Выводит "ест "+ name на консоль с периодичностью 0.5 сек
        void eat() throws InterruptedException {
            long tStart = System.currentTimeMillis();
            long tNow = tStart;
            long passed = 0;
            boolean isInterrupted = false;
            while (passed < eatTime) {
                System.out.println("ест " + name);
                long needReflect = eatTime - passed;
                try {
                    Thread.sleep(needReflect > REPEATTIME ? REPEATTIME : needReflect);
                } catch (InterruptedException e) {
                    isInterrupted = true;
                    break;
                } finally {
                    tNow = System.currentTimeMillis();
                    passed = tNow - tStart;
                }
            }
            eatSum += passed;
            if(isInterrupted) throw new InterruptedException();
        }

        Philosopher(String name, Fork left, Fork right, long reflectTime, long eatTime) {
            this.reflectTime = reflectTime;
            this.eatTime = eatTime;
            reflectSum = 0;
            eatSum = 0;
            this.left = left;
            this.right = right;
            this.name = name;
        }

        Fork getFork(Side side) {
            return side == Side.LEFT ? left : right;
        }
    }

    enum Side {LEFT, RIGHT};
    final static int PCOUNT = 5; // количество философов
    Thread[] threads = new Thread[PCOUNT]; // поток на каждого философа
    Philosopher[] philosophers = new Philosopher[PCOUNT]; // философы
    SplittableRandom random = new SplittableRandom(); //ГСЧ

    //который инициализирует необходимое количество философов и вилок. Каждый философ выполняется в отдельном потоке.
    // reflectTime задает время в мс, через которое философ проголодается, eatTime задает время в мс,
    // через которое получив 2 вилки философ наестся и положит вилки на место
    Simposium(long reflectTime, long eatTime) {
        Fork[] forks = new Fork[PCOUNT];
        for (int i=0;i<PCOUNT; i++) {
            forks[i] = new Fork();
        }
        for (int i=0;i<PCOUNT; i++) {
            final Philosopher p = new Philosopher("P"+(i+1), forks[i], forks[(i+1)% PCOUNT], reflectTime, eatTime);
            philosophers[i] = p;
            threads[i] = new Thread(new logic(p, random));
        }
    }

    // основная логика действий философа
    static class logic implements Runnable {

        Philosopher p;
        SplittableRandom random;
        Side lookNow = Side.LEFT;

        public logic(Philosopher p, SplittableRandom random) {
            this.p = p;
            this.random = random;
        }

        @Override
        public void run() {
            while (true) {
                lookNow = (lookNow == Side.LEFT ? Side.RIGHT : Side.LEFT);
                try {
                    Thread.sleep(random.nextInt(PCOUNT));
                } catch (InterruptedException e) {
                    break;
                }
                // Ситуация: Нет ни одной вилки
                Fork f1 = p.getFork(lookNow);
                synchronized (f1) {
                    // 1. ПОСМОТРИ НАПРАВО, если нет вилки, то через несколько случайное количество секунд повтори действие поменяв сторону (ПОСМОТРИ НАЛЕВО).
                    if (f1.isFree())
                        f1.setFree(false); // 2. Увидел вилку на столе то ПОПРОБУЙ ее взять. Если попытка неудачна то прекрати попытку и поменяв сторону вернись в п.1
                    else continue;
                }
                // Ситуация: В одной руке есть вилка.
                lookNow = (lookNow == Side.LEFT ? Side.RIGHT : Side.LEFT);
                Fork f2 = p.getFork(lookNow);
                synchronized (f2) {
                    // 3. УДАЧНО - есть одна вилка в руке! ПОСМОТРИ в ДРУГУЮ СТОРОНУ. Если нет вилки , тогда положи свою вилку назад.! И поменяв сторону вернись в п.1
                    if (f2.isFree())
                        f2.setFree(false); // 5. УДАЧНО - есть вторая вилка в руке!
                    else {
                        f1.setFree(true); // 4. ПОПРОБУЙ ВЗЯТЬ ВИЛКУ если неудачно, то ОТПУСТИ (освободи) эту вилку и ПОЛОЖИ ВИЛКУ КОТОРАЯ НАХОДИТСЯ В ДРУГОЙ РУКЕ. Поменяй сторону вернись в п.1
                        continue;
                    }
                }
                // Ситуация: В обеих руках по вилке.
                // 6. Приступай к еде
                try {
                    p.eat();
                } catch (InterruptedException e) {
                    break;
                }
                p.right.setFree(true);
                p.left.setFree(true);
                // 7. Наелся положи обе вилки, сначала правую, затем левую на стол. Думай до тех пока не проголодаешься.
                try {
                    p.reflect();
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    // запускает философскую беседу
    void start() {
        for (int i=0;i<PCOUNT; i++) {
            threads[i].start();
        }
    }

    // завершает философскую беседу
    void stop() throws InterruptedException {
        for (int i=0;i<PCOUNT; i++) {
            threads[i].interrupt();
        }
        for (int i=0;i<PCOUNT; i++) {
            threads[i].join();
        }
    }

    //печатает результаты беседы в формате
    //Философ name, ел ххх, размышлял xxx
    //где ххх время в мс
    void print() {
        for (Philosopher p: philosophers) {
            System.out.println("Философ "+p.name+", ел "+p.eatSum+", размышлял "+p.reflectSum);
        }
    }

    // реализует тест для философской беседы. Проверить варианты, когда ресурсов (вилок) достаточно
    // (философы долго размышляют и мало едят) и вариант когда не хватает (философы много едят и мало размышляют)
    public static void main(String[] args) throws InterruptedException {
        Simposium simposium = new Simposium(1000, 1000);
        simposium.start();
        Thread.sleep(5000);
        simposium.stop();
        simposium.print();
    }


}

