package ru.progwards.java2.lessons.reflection;

public class GettersAndSetters {
/*
Реализовать метод public static check(String) который анализирует, для каких private полей класса
нет сеттера или геттера и выводит на консоль сигнатуры отсутствующих методов;

Например, описан класс
class Person {
    private String name;
    private int age;
    private boolean sex;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
     public boolean getSex() {
         return sex;
     }
}

тут у свойства name есть геттер и сеттер.
Геттер это public не static метод getИмяСвойства без параметров и с типом, совпадающим с типом свойства.
Сеттер это public не static void метод setИмяСвойства c одним параметром с типом, совпадающим с типом свойства.

Для приведенного примера на консоль должно быть выдано

public boolean setSex(boolean sex)
public int getAge()
public void setAge(int age)
*/

    public static void check(String clazzName) {

    }

    public static void main(String[] args) throws Exception {
        //inspect("java.lang.String");
        check("ru.progwards.java2.lessons.reflection.Employee");
    }
}
