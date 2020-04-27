package ru.progwards.java2.lessons.synchro;

import javassist.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/*
java-агент - профилировщик, замеряющий время работы всех методов
*/

/*
Артифакт билдится при правках сам, если он добавлен в проекте:
Ctrl+Alt+Shift+S
Artifacts
Создаем джава-агент по трем файлам: SystemProfiler, Interceptor, Profiler

Запускаем что-нибудь и прописываем путь в параметрах Run - Edit Configurations - VM Options:
-javaagent:"C:\Users\Grigory\IdeaProjects\java2\out\artifacts\Profiler\Profiler.jar"=Heap;HeapTest

где SimpleReadData - это класс, который надо проинспектировать.
*/

enum TimeMeasureUnits {MILLISECONDS, NANOSECONDS};

public class Profiler implements ClassFileTransformer {

    public static void premain(String agentArgument, Instrumentation instrumentation) {
        instrumentation.addTransformer(new Profiler(agentArgument));
    }

    final static String ROOT_PKG = "ru.progwards.java2.lessons.synchro.gc";
    final static String ROOT_PATH = ROOT_PKG.replace(".", "/");

    private static final ClassPool classPool = ClassPool.getDefault();
    private static final HashSet<String> inspectedClasses = new HashSet<String>();
    private static final String currentPkg = Profiler.class.getPackageName();

    public Profiler(String agentArgument) {
        if(agentArgument!=null) {
            String[] strParts = agentArgument.split(";");
            for (String s : strParts)
                inspectedClasses.add(s);
        }
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (className.startsWith(ROOT_PATH)) {
            String shortName = className.substring(className.lastIndexOf("/") + 1);
            if (inspectedClasses.contains(shortName))
                try {
                    String dottedClassName = className.replace('/', '.');
                    return adjustClass(dottedClassName, classBeingRedefined,
                            classfileBuffer);
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
        return null;
    }

    private byte[] adjustClass(final String className,
                               final Class<?> classBeingRedefined, final byte[] classfileBuffer)
            throws IOException, RuntimeException, CannotCompileException {

        System.out.println("adjustClass(" + className+')');

        CtClass clazz = null;

        try {
            clazz = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
            if (!clazz.isInterface()) {
                CtBehavior[] methods = clazz.getDeclaredBehaviors();
                for (CtBehavior method : methods) {
                    if (!method.isEmpty() && !Modifier.isNative(method.getModifiers())) {
                        try {
                            String secName = method.getLongName();
                            if(secName.contains(".lambda$")) continue;

                            //System.out.println(secName);

                            method.insertBefore(String.format(currentPkg+".Profiler.enterSection(\"%s\");",secName));
                            method.insertAfter(String.format(currentPkg+".Profiler.exitSection(\"%s\");",secName));
                            if(method.getName().compareTo("main")==0) {
                                method.insertAfter(String.format(currentPkg+".Profiler.printStatisticInfo(\"%s.stat\");",clazz.getSimpleName()));
                            }
                        } catch (Throwable t) {
                            System.out.println("Error instrumenting " + className + "."
                                    + method.getName());
                            t.printStackTrace();
                        }
                    }
                }
                return clazz.toBytecode();
            }
        } finally {
            if (clazz != null) {
                clazz.detach();
            }
        }
        return classfileBuffer;
    }

    public static void printStatisticInfo(String fileName) {
        System.out.println(Profiler.getSectionsInfo().replace(ROOT_PKG, ""));
        /*//System.out.println("File: "+fileName);
        try (FileWriter fileWriter = new FileWriter(fileName, true)) {
            fileWriter.write(new Date().toString()+ ru.progwards.java2.lessons.classloader.Profiler.getSectionsInfo()+"\n\n");
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    static TreeMap<String, StatisticInfo> sections = new TreeMap<String, StatisticInfo>();

    static ReadWriteLock sectionsLock = new ReentrantReadWriteLock();

    // войти в профилировочную секцию
    public static void enterSection(String name) {
        StatisticInfo section;
        String secName = Thread.currentThread().getName() + name;
        sectionsLock.readLock().lock();
        boolean exists = sections.containsKey(secName);
            if (exists) {
                section = sections.get(secName);
                sectionsLock.readLock().unlock();
            } else {
                sectionsLock.readLock().unlock();
                section = new StatisticInfo(secName);
                sectionsLock.writeLock().lock();
                sections.put(secName, section);
                sectionsLock.writeLock().unlock();
            }
            if (!section.isRun) {
                sectionsLock.readLock().lock();
                for (StatisticInfo i : sections.values()) i.addInsider(section);
                sectionsLock.readLock().unlock();
            }
        section.enter();
    }

    // выйти из профилировочной секции
    public static void exitSection(String name) {
        String secName = Thread.currentThread().getName() + name;
        sectionsLock.readLock().lock();
        StatisticInfo section = sections.get(secName);
        sectionsLock.readLock().unlock();
        section.exit();
        if (!section.isRun) {
            sectionsLock.writeLock().lock();
            for (StatisticInfo i : sections.values()) i.removeInsider(section);
            sectionsLock.writeLock().unlock();
        }
    }

    // обнулить статистику
    public static synchronized void clear() {
        sectionsLock.writeLock().lock();
        sections.clear();
        sectionsLock.writeLock().unlock();
    }

    // получить профилировочную статистику, отсортировать по наименованию секции
    public static synchronized List<StatisticInfo> getStatisticInfo() {
        sectionsLock.readLock().lock();
        try {
            return new ArrayList<StatisticInfo>(sections.values());
        } finally {
            sectionsLock.readLock().unlock();
        }
    }

    public static synchronized String getSectionsInfo() {
        sectionsLock.readLock().lock();
        try {

            for (StatisticInfo s: sections.values()) {

            }
            return sections.values().toString();
        } finally {
            sectionsLock.readLock().unlock();
        }
    }

    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
        }
    }
}

class StatisticInfo implements Comparable {
    public String sectionName; // имя секции
    public int fullTime = 0; // полное время выполнения секции в миллисекундах
    public int selfTime = 0; // чистое время выполнения секции в миллисекундах
    public int count = 0; // количество вызовов

    boolean isRun; // выполняется ли секция
    private long runStartTime; // время начала
    private List<Insider> runInside; // кто запущен внутри

    static ReadWriteLock runInsideLock = new ReentrantReadWriteLock();

    StatisticInfo(String sectionName) {
        this.sectionName = sectionName;
        runInside = new ArrayList<Insider>();
    }

    // вход в секцию
    void enter() {
        count++;
        if (!isRun) {
            isRun = true;
            runStartTime = System.currentTimeMillis();
        }
    }

    // выход из секции
    void exit() {
        if (!isRun) return;
        long timeNow = System.currentTimeMillis();
        int newFullTime = fullTime + (int) (timeNow - runStartTime);
        int newSelfTime = actualSelfTime(timeNow);
        runInsideLock.writeLock().lock();
        runInside.clear();
        runInsideLock.writeLock().unlock();
        isRun = false;
        fullTime = newFullTime;
        selfTime = newSelfTime;
    }

    // собственное время, без подсекций
    int actualSelfTime(long timeNow) {
        if (!isRun) return selfTime;
        int result = selfTime + (int) (timeNow - runStartTime);
        runInsideLock.readLock().lock();
        for (Insider i : runInside) {
            result -= i.getInsideTime(timeNow);
        }
        runInsideLock.readLock().unlock();
        return result;
    }

    // добавить внутреннюю секцию
    void addInsider(StatisticInfo info) {
        if (isRun) {
            runInsideLock.writeLock().lock();
            runInside.add(new Insider(info, System.currentTimeMillis()));
            runInsideLock.writeLock().unlock();
        }
    }

    // удалить внутреннюю секцию
    void removeInsider(StatisticInfo info) {
        if (isRun) {
            long timeNow = System.currentTimeMillis();
            runInsideLock.readLock().lock();
            ListIterator i = runInside.listIterator();
            while (i.hasNext()) {
                Insider insider = (Insider) i.next();
                if (insider.info == info) {
                    selfTime -= insider.getInsideTime(timeNow);
                    i.remove();
                }
            }
            runInsideLock.readLock().unlock();
        }
    }
    public String rpad(String inputString, int length) {
        int len = inputString.length();
        if (len >= length) {
            return inputString;
        }
        return inputString+" ".repeat(length-len);
    }
    public String rpad(long inputNum, int length) {
        return rpad(String.valueOf(inputNum), length);
    }

    @Override
    public String toString() {
        return "\n" + rpad(sectionName,80) + "total:" + rpad(fullTime, 7) + "self:" + rpad(selfTime, 7) + "count:" + rpad(count,9) + " ns/exec:"+(1_000_000L*(long)selfTime/count);
    }

    // для сортировки в TreeMap
    @Override
    public int compareTo(Object o) {
        return this.sectionName.compareTo(((StatisticInfo) o).sectionName);
    }
}

class Insider {
    public int selfTime; // чистое время связянной секции на момент создания связи
    StatisticInfo info; // секция, с которой связь

    Insider(StatisticInfo info, long timeNow) {
        this.info = info;
        selfTime = info.actualSelfTime(timeNow);
    }

    int getInsideTime(long timeNow) {
        return info.actualSelfTime(timeNow) - selfTime;
    }
}

