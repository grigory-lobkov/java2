package ru.progwards.java2.lessons.annotation.di.service;

public class Context {
    public static void initialize(String packageName) {
        // я как-то сразу реализовал в статическом методе DI.scanClassesAnnotations(String packageName)
        // скажите, чем моё решение хуже.

        // Я вижу только один минус: если DI не понадобится, то сканировать классы ни к чему.
        // Ответ: тогда не нужно включать данный DI в проект.
        // Зато получаем удобство: весь функционал в одном файле, который легко копировать без зависимостей
        // Решение: вынес вызов инициализатора из статического конструктора
    }
}
