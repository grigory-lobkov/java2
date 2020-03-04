package Lesson02;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

class Test1 {
    class Person {
        private String name;
        private int age;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String toString() {
            return name + " " + age;
        }
    }
    //Создайте метод, используя лямбда, с сигнатурой void sortAndPrint(List<Person> list), который вначале сортирует list по возрасту, а затем выводит его на консоль.

    void sortAndPrint(List<Person> list) {
        //list.sort((a, b)->Integer.compare(a.age, b.age));
        list.sort(Comparator.comparingInt(a -> a.age));
        //list.forEach(a->System.out.println(a));
        list.forEach(System.out::println);
    }

    public static void main(String[] args) {
        new Test1().test();
    }
    void test() {
        List<Person> list = new ArrayList<>(List.of(
                new Person("Петя", 27),
                new Person("Коля", 33),
                new Person("Маша", 18),
                new Person("Сеня", 38),
                new Person("Даша", 35)
        ));
        sortAndPrint(list);
    }
}