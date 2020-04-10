package ru.progwards.java2.lessons.annotation;

import java.lang.annotation.*;
import java.lang.reflect.Method;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface AnnotationTest {
    String text() default "Всегда говори привет";
}

public class Greetings {

    //реализуйте метод с сигнатурой void printAnnotation(),
    // который печатает на консоль для класса Greetings название метода,
    // и значение text аннотации AnnotationTest, если таковое определено

    @AnnotationTest
    void test1() {
    }

    @AnnotationTest(text="test")
    void test2() {
    }

    void printAnnotation() {
        Class<Greetings> clazz = Greetings.class;

        Method[] methods = clazz.getDeclaredMethods();
        for (Method method:methods) {
            Annotation[] annotations = method.getDeclaredAnnotations();
            for (Annotation a: annotations)
                if(a.annotationType()==AnnotationTest.class)
                    System.out.println(method.getName() + " "+((AnnotationTest)a).text());
        }
    }

    public static void main(String[] args) {
        new Greetings().printAnnotation();
    }
}
