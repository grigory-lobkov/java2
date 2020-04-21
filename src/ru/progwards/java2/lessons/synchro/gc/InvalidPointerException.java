package ru.progwards.java2.lessons.synchro.gc;

// Исключение: не верный указатель.
// Возникает при освобождении блока, если переданный указатель не является началом блока
class InvalidPointerException extends Exception {
}
