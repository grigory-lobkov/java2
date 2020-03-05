package ru.progwards.java2.lessons.recursion;

public class Test3 {

//    Напишите метод с сигнатурой String reverseChars(String str),
//    который возвращает символы строки str в обратном порядке.
//    Т.е. если на входе "12345" на выходе должно быть "54321"
//
//    Задачу надо решить методом рекурсии, циклы использовать нельзя!!!

    String reverseChars(String str) {
        int l = str.length();
        if (l <= 1) return str;
        int h = l / 2;
        return reverseChars(str.substring(h)) + reverseChars(str.substring(0, h));
    }

    public static void main(String[] args) {
        System.out.println(new Test3().reverseChars("123456789"));
    }

}