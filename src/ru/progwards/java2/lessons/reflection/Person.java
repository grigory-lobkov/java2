package ru.progwards.java2.lessons.reflection;

public class Person {
    private String name;
    private int age;
    private boolean sex;

    public Person() {
        name = "no name";
    }

    private Person(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    public boolean getSex() {
        return sex;
    }

}
