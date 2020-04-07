package ru.progwards.java2.lessons.annotation.di;

import ru.progwards.java2.lessons.annotation.di.model.Account;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Simple Dependency Injection class
 *
 * Usage:
 *  - mark classes with annotation {@code @Dependency(name="DepName")}, ex. DepClass.class
 *  - use it as {@code DepClass dep = DI.of("DepName");}
 *    or {@code DepClass dep = DI.of("DepName", DI.args(String.class), DI.vals("Ivan"));}
 *
 * @author Gregory Lobkov
 * @version 1.0
 */

public class DI {

    /**
     * Adding of class to class factory
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Dependency {
        /**
         * Alias of class, used to create instance of the class. Mandatory.
         */
        String name();

        /**
         * If singleton flag set, the only one instance will be generated. Default is {@code false}
         */
        boolean isSingleton() default false;
    }

    /**
     * Table of all annotated classes
     * @see Dependency#name
     * @see Entry
     *
     * @param key Alias of class
     * @param value Entry of class, storing all parameters
     */
    private static Map<String, Entry> storage = new Hashtable<>();

    /**
     * Scan classes for annotation
     *
     * @param packageName full start name of package to scan for annotations
     */
    private static void initialize(String packageName) {
        List<Path> paths = scanPackageFolder(packageName);
        Path root = paths.get(0);

        for (Path p : paths) {
            String fileName = root.relativize(p).toString();
            int dotPos = fileName.indexOf('.');
            if (dotPos > 0 && fileName.substring(dotPos).compareTo(".class") == 0) {
                String clazzName = packageName + '.' + fileName.substring(0, dotPos).replace(File.separator, ".");
                try {
                    Class<?> clazz = Class.forName(clazzName);
                    addClassToStorage(clazz);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Scan class for annotations and store it in the {@code storage}
     * @see DI#storage
     *
     * @param clazz full start name of package to scan for annotations
     */
    private static void addClassToStorage(Class<?> clazz) {
        Annotation[] annotations = clazz.getAnnotations();
        for (Annotation annotation : annotations)
            if (annotation.annotationType() == Dependency.class) {
                Dependency d = (Dependency) annotation;
                String dName = d.name();
                Entry exists = storage.get(dName);
                if (exists == null)
                    storage.put(dName, new Entry(clazz, d));
                else throw new RuntimeException("\u001B[31mDependency name '" + dName + "' repeated!" +
                        "\n   exist class=" + exists.clazz.getName() +
                        "\nconflict class=" + clazz.getName() + "\u001B[0m");
            }
    }

    /**
     * Scan classes files in {@code packageName} folder
     *
     * @param packageName full start name of package to scan for annotations
     * @return list of all {@code Path} to the classes
     */
    private static List<Path> scanPackageFolder(String packageName) {
        List<Path> result = new ArrayList<>();
        ClassLoader classLoader = new DI().getClass().getClassLoader();
        String packagePath = packageName.replace(".", "/");
        Path root = Path.of(new File(classLoader.getResource(packagePath).getPath()).toURI());
        result.add(root);

        try (Stream<Path> s = Files.walk(Paths.get(root.toUri()))) {
            s.filter(Files::isRegularFile).forEach(result::add);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Entry for storing {@code Class} parameters
     */
    static class Entry {
        Class<?> clazz;
        Dependency dep;
        Object instance;
        boolean isSingleton;

        public Entry(Class<?> clazz, Dependency dependency) {
            this.clazz = clazz;
            this.dep = dependency;
            instance = null;
            isSingleton = dep.isSingleton();
        }
    }

    /**
     * Return instance of custom class
     * @see Dependency
     *
     * @param name name, given in {@code @Dependency} annotation
     * @param args arguments to desired constructor - list of classes (primitives is not supported)
     * @param vals values for constructor - list of Objects
     * @param <T> result in desired type
     * @return instance of class with given {@code name}
     */
    static <T> T of(String name, Class<?>[] args, Object[] vals) {
        Entry entry = storage.get(name);
        if (entry.isSingleton && entry.instance != null)
            return (T) entry.instance;
        try {
            Constructor<?> constructor = entry.clazz.getDeclaredConstructor(args);
            try {
                Object instance = constructor.newInstance(vals);
                entry.instance = instance;
                return (T) instance;
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Return instance of custom class, short version for default constructor
     * @see DI#of(String, Class[], Object[])
     *
     * @param name name, given in {@code @Dependency} annotation
     * @param <T> result in desired type
     * @return instance of class with given {@code name}
     */
    static <T> T of(String name) {
        return of(name, null, null);
    }

    /**
     * Arguments for constructor shortener
     * Ex: {@code DI.of("Account", DI.args(String.class, String.class, Integer.class), DI.vals("id23", "Ivan", 23))}
     *
     * @param args list of arguments for
     * @return array for instance creating method
     */
    static Class<?>[] args(Class<?>... args) {
        return args;
    }

    /**
     * Values for constructor shortener
     * Ex: {@code DI.of("Account", DI.args(String.class, String.class, Integer.class), DI.vals("id23", "Ivan", 23))}
     *
     * @param vals list of objects
     * @return array for instance creating method
     */
    static Object[] vals(Object... vals) {
        return vals;
    }

    /**
     * Return instance of custom class, alias for {@code of}
     * @see DI#of(String)
     */
    static <T> T getBean(String name) {
        return of(name, null, null);
    }

    /**
     * Return instance of custom class, alias for {@code of}
     * @see DI#of(String, Class[], Object[])
     */
    static <T> T getBean(String name, Class<?>[] args, Object[] vals) {
        return of(name, args, vals);
    }

    /**
     * Example code (should be deleted or commented)
     *
     * @param a not used
     */
    public static void main(String[] a) {
        //Account account = DI.of("Account");
        DI.initialize("ru.progwards.java2.lessons.annotation"); // can be moved to static{} block
        Account account = DI.of("Account", DI.args(String.class, String.class, Integer.class), DI.vals("id23", "Ivan", 23));
        account.setPin(3);
        System.out.println(account);
    }
}