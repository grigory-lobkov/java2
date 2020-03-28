package ru.progwards.java2.lessons.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Lesson7 {

    void setName(Person person, String name) {
        Class clazz = person.getClass();
        try {
            Field field = clazz.getDeclaredField("name");
            field.setAccessible(true);
            field.set(person, name);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    void callSetName(Person person, String name) {
        Class<?> clazz = person.getClass();
        try {
            Method method = clazz.getDeclaredMethod("setName", String.class);
            method.setAccessible(true);
            method.invoke(person, (Object)name);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    Person callConstructor(String name) {
        Class<?> clazz = Person.class;
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor(String.class);
            constructor.setAccessible(true);
            return (Person)constructor.newInstance(name);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        Person p = new Person();
        System.out.println(p.getName());

        new Lesson7().setName(p, "Alex1");
        System.out.println(p.getName());

        new Lesson7().callSetName(p, "Alex2");
        System.out.println(p.getName());

        Person p3 = new Lesson7().callConstructor("Alex3");
                System.out.println(p3.getName());
    }

}
