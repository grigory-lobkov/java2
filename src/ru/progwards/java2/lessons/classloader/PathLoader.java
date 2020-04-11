package ru.progwards.java2.lessons.classloader;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

/*

Требуется реализовать систему, которая позволяет подгружать исправленный программный код (патчи)
работающих модулей "на лету". Для этого мы размещаем систему папок <root>/<date>/<package>/<class>

root - некая корневая папка
date - дата выпуска патча в формате ггггммдд - 20200425
package - структура папок для пакета, возможно с подпакетами
class - скомпилированный class-файл. Если в пакете более одного class-файла,
то надо использовать их все.

Каждая папка date содержит разное количество классов. Один и тот же класс может присутствовать в
нескольких папках (был исправлен несколько раз), некоторые не разу. В одной папке date может быть
несколько исправленных файлов, в другой только один, и такой, каких нет в предыдущей.

Необходимо реализовать свой ClassLoader, который просканирует каталог root,
найдет там самую свежую реализацию нужного класса, и загрузит ее.

Вести лог загрузки - файл patchloader.log, куда писать информацию в формате

дд.мм.гггг чч.мм.сс <полное имя класса с пакетом> загружен из <полный путь до папки с пакетом> успешно
или
дд.мм.гггг чч.мм.сс <полное имя класса с пакетом> ошибка загрузки <описание ошибки>
*/

public class PathLoader extends ClassLoader {

    final static String ROOT_PATH = "C:\\Users\\Grigory\\IdeaProjects\\java2\\classloader";
    final static String ROOT_PKG = "ru.progwards.java2.lessons.";
    final static String DOT_CLASS = ".class";
    final static String LOG_FILE = ROOT_PATH + File.separator + "patchloader.log";

    private final String basePath;
    static SimpleDateFormat logDateFormatter;

    static {
        logDateFormatter = new SimpleDateFormat("dd.MM.yyyy HH.mm.ss", Locale.getDefault());
        logDateFormatter.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
    }

    public PathLoader(String basePath, ClassLoader parent) {
        super(parent);
        this.basePath = basePath;
    }

    public PathLoader(String basePath) {
        this(basePath, ClassLoader.getSystemClassLoader());
    }

    @Override
    protected Class<?> findClass(String className) throws ClassNotFoundException {
        if (className.startsWith(ROOT_PKG)) {
            String classPath = File.separator + className.replace(".", File.separator);
            File[] folders = getFoldersArray(basePath);
            for (File f : folders)
                try {
                    String strName = f.getPath() + classPath + DOT_CLASS;
                    Path classPathName = Paths.get(strName);
                    if (Files.exists(classPathName)) {
                        System.out.println(strName + " found!");
                        byte b[] = Files.readAllBytes(classPathName);
                        log(className + " загружен из " + strName + " успешно");
                        return defineClass(className, b, 0, b.length);
                    } else {
                        System.out.println(strName + " not found!");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    log(className + " ошибка загрузки " + e.getMessage());
                }
        }
        return findSystemClass(className);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> c = findClass(name);
        if (resolve)
            resolveClass(c);
        return c;
    }

    public static void log(String string) {
        String time = logDateFormatter.format(new Date());
        try (FileWriter writer = new FileWriter(LOG_FILE, true)) {
            writer.write(time + ' ' + string + '\n');
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public static class Entry {
        File folder;
        Object instance;
        String fPath;

        public Entry(Object instance, String oName) {
            this.instance = instance;
            //fPath = instance.getClass().getName().replace(".", File.separator) + DOT_CLASS;
            fPath = oName.replace(".", File.separator) + DOT_CLASS;
        }
    }


    public static void main(String[] args) throws InterruptedException, IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Map<String, Entry> objects = new LinkedHashMap<>();
        String oName = "ru.progwards.java2.lessons.classloader.SimpleReadData";
        objects.put(oName, new Entry(null, oName));

        while (true) {

            updateObjects(objects);

            for (Entry o : objects.values())
                System.out.println(o.instance);

            Thread.sleep(5000);
        }
    }

    private static void updateObjects(Map<String, Entry> objects) throws IOException {
        File[] folders = getFoldersArray(ROOT_PATH);

        // попробуем найти в этих папках бновленные классы

        objects.forEach((className, entry) -> {
            for (File f : folders) {
                try {
                    if (entry.folder != null && entry.folder.compareTo(f) == 0)
                        break; // если дошли до папки последнего обновления - не перебираем дальше

                    File classFile = new File(f.getPath() + File.separator + entry.fPath);

                    if (classFile.exists()) {
                        // файл с классом найден, он новее!
                        entry.folder = f;
                        PathLoader loader = new PathLoader(ROOT_PATH);
                        Class objectClass = loader.loadClass(className, true);
                        entry.instance = objectClass.getDeclaredConstructor().newInstance();
                    }
                } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static File[] getFoldersArray(String rootPath) {
        File rootFolder = new File(ROOT_PATH);

        // список папок, только состоящие из 8 цифр

        File[] folders = rootFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches("\\d{8}+");
            }
        });

        // отсортируем в обратном порядке по наименованию

        Arrays.sort(folders, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o2.getName().compareTo(o1.getName());
            }
        });
        //for (File f: folders) System.out.println(f.getName());

        return folders;
    }

}