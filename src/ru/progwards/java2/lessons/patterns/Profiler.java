package ru.progwards.java2.lessons.patterns;

import javassist.*;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * java-agent
 *
 * profiler, calculated time of method execution in the inspected classes
 *
 *
 * Compilation:
 * - Create artifact in you project: Ctrl+Alt+Shift+S -> Artifacts -> Add (Alt+Insert)
 * - Name: "Profiler"
 * - Manifest file: choose any folder
 * - Open manifest file in editor, add there line with Profiler class full path:
 * Premain-Class: ru.progwards.java2.lessons.patterns.Profiler
 * - Edit Profiler.rootPkg variable to mark, where to search for inspected classes
 * - Build -> Build Artifactis -> Build
 *
 * Compile dependency:
 * - javassist https://mvnrepository.com/artifact/org.javassist/javassist
 *
 *
 * Usage:
 * - Run any class once
 *
 * - Go to Run -> Edit configurations
 *
 * - VM options: -javaagent:"PATH_TO_JAR\Profiler.jar"[=[INSPECTED_PACKAGE][;INSPECTED_CLASS1[;INSPECTED_CLASS2[;...]]]]
 * -javaagent:"C:\Users\Grigory\IdeaProjects\java2\out\artifacts\Profiler\Profiler.jar"=;Heap;HeapTest
 * -javaagent:"C:\Users\Grigory\IdeaProjects\java2\out\artifacts\Profiler\Profiler.jar"=ru.progwards.java2.lessons.synchro.gc;Heap;HeapTest
 * -javaagent:"C:\Users\Grigory\IdeaProjects\java2\out\artifacts\Profiler\Profiler.jar"=ru.progwards.java2.lessons.synchro.gc
 * -javaagent:"C:\Users\Grigory\IdeaProjects\java2\out\artifacts\Profiler\Profiler.jar"
 *
 * - Run again to watch execution statistics
 *
 *
 * @author Gregory Lobkov
 * @link <a href="https://github.com/grigory-lobkov/profiler-1class">Project page</a>
 * @version 0.1
 * @since 1.5
 */

/*
 * java-агент - профилировщик, замеряющий время работы всех методов
 *
 * Артифакт билдится при правках сам, если он добавлен в проекте:
 * Ctrl+Alt+Shift+S
 * Artifacts
 * Создаем джава-агент Profiler, добавляем манифест, добавляем в нём строку
 * Premain-Class: ru.progwards.java2.lessons.patterns.Profiler
 *
 * Запускаем что-нибудь и прописываем путь в параметрах Run - Edit Configurations - VM Options:
 * -javaagent:"C:\Users\Grigory\IdeaProjects\java2\out\artifacts\Profiler\Profiler.jar"=;Heap;HeapTest
 * -javaagent:"C:\Users\Grigory\IdeaProjects\java2\out\artifacts\Profiler\Profiler.jar"=ru.progwards.java2.lessons.synchro.gc;Heap;HeapTest
 * -javaagent:"C:\Users\Grigory\IdeaProjects\java2\out\artifacts\Profiler\Profiler.jar"=Heap;HeapTest
 *
 * где Heap;HeapTest - это класс, который надо проинспектировать.
 */

public enum Profiler {
    INSTANCE;

    final boolean joinThreadsInReport = true;

    public void printStatisticInfo() {
        System.out.println(getSectionsInfo());
    }

    Hashtable<String, Section> sections = new Hashtable<String, Section>();
    Lock sectionsLock = new ReentrantLock();

    /**
     * Enter profiling section
     *
     * @param name name of section
     */
    public void enterSection(String name) {
        Section section;
        String secName = Thread.currentThread().getName() + '@' + name;
        sectionsLock.lock();
        try {
            section = sections.get(secName);
            if (section == null) {
                section = new Section(secName);
                sections.put(secName, section);
            }
            if (!section.isRun) {
                for (Section i : sections.values())
                    i.addInsider(section);
            }
        } finally {
            sectionsLock.unlock();
        }
        section.enter();
    }

    /**
     * Exit profiling section
     *
     * @param name name of section
     */
    public void exitSection(String name) {
        String secName = Thread.currentThread().getName() + '@' + name;
        sectionsLock.lock();
        try {
            Section section = sections.get(secName);
            section.exit();
            if (!section.isRun) {
                for (Section i : sections.values())
                    i.removeInsider(section);
            }
        } finally {
            sectionsLock.unlock();
        }
    }

    /**
     * Clean statistic
     */
    public void clear() {
        sections.clear();
    }

    /**
     * Determine class name by given line
     *
     * @param name full method name string
     * @return string before open bracket or before last dot
     */
    public String getSectionsClassName(String name) {
        int pos = name.indexOf('(');
        if (pos > 0) name = name.substring(0, pos);
        pos = name.lastIndexOf('.');
        if (pos <= 0) return name;

        return name.substring(0, pos);
    }

    /**
     * Find equal beginning of two lines
     *
     * @param str1 one line to compare
     * @param str2 another line to compare
     * @return equal beginning of two lines
     */
    public String getEqualStartsWith(String str1, String str2) {
        int i;
        int len = str1.length();
        if (len > str2.length())
            len = str2.length();
        for (i = 0; i < len; i++)
            if (str1.charAt(i) != str2.charAt(i))
                break;

        return str1.substring(0, i);
    }

    /**
     * Get string explanation of Sections list
     *
     * @return each method on self line
     */
    public String getSectionsInfo() {
        sectionsLock.lock();
        try {
            // if no need to join, then no need to work hard :)
            // если не нужно объединять потоки, не будем мудрствовать
            if (!joinThreadsInReport) return sections.values().toString();

            // throw out thread names from section names, join same methods from different threads
            // отбросим имена потоков из имен секций, сохраним объединяя в Hashtable

            Hashtable<String, Section> table = new Hashtable<>();
            for (Section s : sections.values()) {
                int pos = s.name.indexOf('@');
                String secName = s.name.substring(pos + 1);
                Section ns = table.get(secName);
                if (ns == null) {
                    ns = new Section(secName);
                    table.put(secName, ns);
                }
                ns.totalTime += s.totalTime;
                ns.selfTime += s.selfTime;
                ns.execsCount += s.execsCount;
            }

            // cutting of the same package name from the list
            // посмотрим, можно ли обрезать одинаковые имена пакетов, и обрежем если можно

            List<Section> list = new ArrayList<>(table.values());
            String pkgToTrunc = null;
            for (Section s : list) {
                if (pkgToTrunc == null)
                    pkgToTrunc = s.name;
                else
                    pkgToTrunc = getEqualStartsWith(pkgToTrunc, s.name);
            }
            if (pkgToTrunc != null && !pkgToTrunc.isEmpty()) {
                if (pkgToTrunc.charAt(pkgToTrunc.length() - 1) != '.')
                    pkgToTrunc = pkgToTrunc.substring(0, pkgToTrunc.lastIndexOf('.') + 1);
                for (Section s : list)
                    s.name = s.name.replace(pkgToTrunc, "");
            }

            // sort by class name. For one class by self time
            // отсортируем по имени класса, затем по затратности

            Collections.sort(list, (e1, e2) -> {
                Section i1 = e1;
                Section i2 = e2;
                String name1 = getSectionsClassName(i1.name);
                String name2 = getSectionsClassName(i2.name);
                int cmpNames = name1.compareTo(name2);
                if (cmpNames != 0)
                    return cmpNames;
                // если имя класса одинаково, отсортируем по времени
                return -Integer.compare(i1.selfTime, i2.selfTime);
            });

            // maximum lengths of data in columns
            // посчитаем максимальные длины данных

            final int calcEachOnCount = 1_000;
            int maxNameLen = 10;
            int maxTotalLen = 8;
            int maxSelfLen = 7;
            int maxCountLen = 5;
            int maxMsEach = -1;
            int maxMsLen = 7;
            for (Section s : list) {
                int c = s.name.length();
                if (c > maxNameLen) maxNameLen = c;
                c = String.valueOf(s.totalTime).length();
                if (c > maxTotalLen) maxTotalLen = c;
                c = String.valueOf(s.selfTime).length();
                if (c > maxSelfLen) maxSelfLen = c;
                c = String.valueOf(s.execsCount).length();
                if (c > maxCountLen) maxCountLen = c;
                if (s.execsCount >= calcEachOnCount) {
                    c = (int) (1_000L * (long) s.selfTime / s.execsCount);
                    if (c > maxMsEach) maxMsEach = c;
                }
            }
            maxSelfLen++;
            maxCountLen++;
            maxMsLen++;

            // prepare header line
            // подготовим заголовок

            StringBuilder sb = new StringBuilder();
            Section t = new Section("");
            sb.append("\n" + t.rpad("ClassName", maxNameLen)
                    + t.lpad("Total,ms", maxTotalLen)
                    + t.lpad("Self,ms", maxSelfLen)
                    + t.lpad("Count", maxCountLen));
            if (maxMsEach > 100000) sb.append(t.lpad("ms/exec", maxMsLen));
            else if (maxMsEach > 100) sb.append(t.lpad("mcs/exec", maxMsLen));
            else if (maxMsEach >= 0) sb.append(t.lpad("ns/exec", maxMsLen));

            // prepare and return of joined methods statistic in beautiful way
            // сформируем и выведем объединенную статистику

            for (Section s : list) {
                sb.append("\n" + s.rpad(s.name, maxNameLen)
                        + s.lpad(s.totalTime, maxTotalLen)
                        + s.lpad(s.selfTime, maxSelfLen)
                        + s.lpad(s.execsCount, maxCountLen));
                if (s.execsCount >= calcEachOnCount)
                    if (maxMsEach > 80000)
                        sb.append(t.lpad(s.selfTime / s.execsCount, maxMsLen));
                    else if (maxMsEach > 80)
                        sb.append(t.lpad(1_000L * (long) s.selfTime / s.execsCount, maxMsLen));
                    else
                        sb.append(t.lpad(1_000_000L * (long) s.selfTime / s.execsCount, maxMsLen));
            }
            return sb.toString();
        } finally {
            sectionsLock.unlock();
        }
    }

    /**
     * Sample code to understand profiler work
     *
     * @param args not used
     * @throws InterruptedException when {@code sleep} will be interrupted
     */
    public static void main(String[] args) throws InterruptedException {
        Profiler.INSTANCE.enterSection("s1");
        Thread.sleep(100);
        Profiler.INSTANCE.enterSection("s2");
        Thread.sleep(200);
        Profiler.INSTANCE.exitSection("s2");
        Profiler.INSTANCE.enterSection("s2");
        Thread.sleep(200);
        Profiler.INSTANCE.exitSection("s2");
        Profiler.INSTANCE.enterSection("s2");
        Thread.sleep(200);
        Profiler.INSTANCE.exitSection("s2");
        Thread.sleep(100);
        Profiler.INSTANCE.exitSection("s1");
        System.out.println(Profiler.INSTANCE.getSectionsInfo());
    }
}

/**
 * Section of time calculation
 * Hierarchy supported
 */
class Section {
    /**
     * unique identifier iterator
     */
    public static int nextId = 0;
    /**
     * unique identifier of the section
     */
    public int id;

    /**
     * thread, section is run in
     */
    public long threadId;
    /**
     * name of section
     */
    public String name;
    /**
     * full execution time in milliseconds
     */
    public int totalTime = 0;
    /**
     * self execution time in milliseconds
     */
    public int selfTime = 0;
    /**
     * executions count
     */
    public int execsCount = 0;
    /**
     * is section run now?
     */
    boolean isRun;
    /**
     * last section run time
     */
    private long runStartTime;
    /**
     * who is running inside
     */
    private Hashtable<Integer, SectionLink> runInside;

    /**
     * Default constructor
     *
     * @param name name of section
     */
    Section(String name) {
        this.name = name;
        this.threadId = Thread.currentThread().getId();
        runInside = new Hashtable<Integer, SectionLink>();
        synchronized (Section.class) {
            id = nextId++;
        }
    }

    /**
     * Enter to section. Start execution timer
     */
    void enter() {
        execsCount++;
        if (!isRun) {
            isRun = true;
            runStartTime = System.currentTimeMillis();
        }
    }

    /**
     * Exit from section. Stop execution timer
     */
    void exit() {
        if (!isRun) return;
        long timeNow = System.currentTimeMillis();
        int newFullTime = totalTime + (int) (timeNow - runStartTime);
        int newSelfTime = actualSelfTime(timeNow);
        runInside.clear();
        isRun = false;
        totalTime = newFullTime;
        selfTime = newSelfTime;
    }

    /**
     * Self time execution calulation
     *
     * @param timeNow current system time
     * @return in-house costs
     */
    int actualSelfTime(long timeNow) {
        if (!isRun) return selfTime;
        int result = selfTime + (int) (timeNow - runStartTime);
        for (SectionLink i : runInside.values()) {
            result -= i.getInsideTime(timeNow);
        }
        return result;
    }

    /**
     * Add section inside this one
     *
     * @param section link this subsection
     */
    void addInsider(Section section) {
        if (isRun && section.threadId == threadId) {
            runInside.put(section.id, new SectionLink(section, System.currentTimeMillis()));
        }
    }

    /**
     * Remove section from inside
     *
     * @param section unlink this subsection
     */
    void removeInsider(Section section) {
        if (isRun && section.threadId == threadId) {
            long timeNow = System.currentTimeMillis();
            SectionLink insider = runInside.remove(section.id);
            if (insider != null)
                selfTime -= insider.getInsideTime(timeNow);
        }
    }

    /**
     * Fill {@code string} with spaces from the left, until it's length become {@code length}
     * If {@code string} length is greater than {@code length}, it won't be truncated
     *
     * @param string string to fill with spaces
     * @param length desirable length of result
     * @return
     */
    public String lpad(String string, int length) {
        int len = string.length();
        if (len >= length) {
            return string;
        }
        return " ".repeat(length - len) + string;
    }

    /**
     * Implementation of {@code lpad} for numbers
     *
     * @see Section#lpad(String, int)
     */
    public String lpad(long number, int length) {
        return lpad(String.valueOf(number), length);
    }

    /**
     * Fill {@code string} with spaces from the right, until it's length become {@code length}
     * If {@code string} length is greater than {@code length}, it won't be truncated
     *
     * @param string string to fill with spaces
     * @param length desirable length of the result
     * @return
     */
    public String rpad(String string, int length) {
        int len = string.length();
        if (len >= length) {
            return string;
        }
        return string + " ".repeat(length - len);
    }

    /**
     * Implementation of {@code rpad} for numbers
     *
     * @see Section#rpad(String, int)
     */
    public String rpad(long number, int length) {
        return rpad(String.valueOf(number), length);
    }

    /**
     * This string will be displayed, when {@code joinThreadsInReport} is set to {@code false}
     *
     * @return cumulative section info in one line
     */
    @Override
    public String toString() {
        return "\n" + rpad(name, 80) + "total:" + rpad(totalTime, 7) + "self:" + rpad(selfTime, 7) + "execsCount:" + rpad(execsCount, 9) + " ns/exec:" + (1_000_000L * (long) selfTime / execsCount);
    }
}

/**
 * Link to subsection class
 */
class SectionLink {

    /**
     * Self time of linked section
     */
    int selfTime;

    /**
     * Linked section
     */
    Section info;

    /**
     * Create link
     *
     * @param info    linked section
     * @param timeNow system time
     */
    SectionLink(Section info, long timeNow) {
        this.info = info;
        selfTime = info.actualSelfTime(timeNow);
    }

    /**
     * Get time, section is inside master section
     *
     * @param timeNow system time
     * @return self time of linked, from link creation
     */
    int getInsideTime(long timeNow) {
        return info.actualSelfTime(timeNow) - selfTime;
    }
}