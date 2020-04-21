package ru.progwards.java2.lessons.synchro.gc;

// Исключение: нет свободного блока подходящего размера
class OutOfMemoryException extends Exception {
    public OutOfMemoryException() {}
    public OutOfMemoryException(String message) {
        super(message);
    }
}
