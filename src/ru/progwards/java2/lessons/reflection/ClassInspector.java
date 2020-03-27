package ru.progwards.java2.lessons.reflection;

import java.lang.reflect.*;

public class ClassInspector {
/*
Реализовать метод public static void inspect(String clazz), который выводит в файл с именем
равным имени класса и расширением java содержимое класса в формате:

class <имя> {
  <конструктор 1>
  <конструктор 2>
  ...
  <свойство 1>
  <свойство 2>
  ...
  <метод 1>
  <метод 2>
  ...
}

каждый конструктор имеет формат:
<модификаторы> <имя>(<параметр 1>, <параметр 2>, ...) {}

где каждое свойство имеет формат:
<модификаторы> <тип> <имя>;

каждый метод имеет формат:
<модификаторы> <тип> <имя>(<параметр 1>, <параметр 2>, ...) {}

и каждый параметр имеет формат:
<тип> <имя>

Пример

class Person {
    private String name;
    public Person() {}
    private Person(String name) {}
    public String getName() {}
    public void setName(String name) {}
}
Параметр clazz задает полное имя класса с пакетом
*/

    public static void inspect(String clazzName) throws ClassNotFoundException {
        Class clazz = Class.forName(clazzName);
        printName(clazz);
        System.out.println("{");
        printFields(clazz, "    ");
        System.out.println();
        printConstructors(clazz, "    ");
        System.out.println();
        printMethods(clazz, "    ");
        System.out.println("}");
    }

    // вывести заголовок класса
    private static void printName(Class clazz) {
        System.out.println(getModifiers(clazz.getModifiers(), true) + clazz.getSimpleName());
    }

    // вернуть строку с модификаторами объекта
    private static String getModifiers(int mods, boolean isClass) {
        return (Modifier.isPublic(mods) ? "public " : "") +
                (Modifier.isPrivate(mods) ? "private " : "") +
                (Modifier.isAbstract(mods) ? "abstract " : "") +
                (Modifier.isFinal(mods) ? "final " : "") +
                (Modifier.isStatic(mods) ? "static " : "") +
                (isClass ? Modifier.isInterface(mods) ? "interface " : "class " : "");
    }

    // вернуть строку с аргументами и их типами через запятую
    private static String getArguments(Parameter[] parameters) {
        boolean isFirst = true;
        StringBuilder sb = new StringBuilder();
        for (Parameter p : parameters) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(", ");
            }
            sb.append(p.getType().getSimpleName() + " " + p.getName());
        }
        return sb.toString();
    }

    // вывести все конструкторы класса
    public static void printConstructors(Class clazz, String prefix) {
        Constructor[] constructors = clazz.getConstructors();
        for (Constructor constructor : constructors) {
            System.out.println(prefix + clazz.getSimpleName() + "("
                    + getArguments(constructor.getParameters()) + ") {}");
        }
    }

    // вывести все свойства класса
    public static void printFields(Class clazz, String prefix) {
        Field[] fields = clazz.getFields();
        for (Field field : fields) {
            System.out.println(prefix + getModifiers(field.getModifiers(), false)
                    + field.getType().getSimpleName() + " " + field.getName() + ";");
        }
    }

    // вывести все методы класса
    public static void printMethods(Class clazz, String prefix) {
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            System.out.println(prefix + getModifiers(method.getModifiers(), false)
                    + method.getReturnType().getSimpleName() +" "+ method.getName() + "("
                    + getArguments(method.getParameters()) + ") {}");
        }
    }

    // тест
    public static void main(String[] args) throws Exception {
        //inspect("java.lang.String");
        //inspect("ru.progwards.java2.lessons.reflection.Employee");
        inspect("ru.progwards.java2.lessons.reflection.Person");
    }

}