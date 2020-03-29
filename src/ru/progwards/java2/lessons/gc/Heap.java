package ru.progwards.java2.lessons.gc;

import java.util.NoSuchElementException;

/*
Имеется массив байт, который будет представлять из себя кучу - heap.
Нужно будет написать алгоритм, который выделяет и освобождает память
(ячейки в массиве) и делает дефрагментацию.

Для реализации этих методов надо будет завести структуру данных - список
(или другая структура данных) свободных блоков. При выделении памяти искать
блок подходящего размера в этом списке, при освобождении - добавлять его туда.
Для проверки валидности освобождения указателей - список (или другая структура данных)
занятых блоков. При компактизации саму процедуру замены старый указателей на новые
опускаем, поэтому и делаем не очень эффективное копирование самих данных, что бы была
близкая производительность.
*/
public class Heap {

    public byte[] bytes;          // куча
    public final int maxHeapSize; // максимальный размер кучи
    public final int initObjectPercent = 50; // сотые доли процента для расчета примерного количества объектов от размера кучи

    private int initObjCount;                  // базовый размер служебных таблиц, расчет при инициализации
    private IntDictionary<Integer> sizeInPos;  // индекс - позиция, значение - размер объекта
    private BiHeap2int freeSpace;              // свободные блоки в памяти (сравнение - по размеру блока)

    Heap() {
        this.maxHeapSize = 64;
        init();
    }

    Heap(int maxHeapSize) {
        this.maxHeapSize = maxHeapSize;
        init();
    }

    private void init() {
        bytes = new byte[maxHeapSize];
        int initObjCount = maxHeapSize > 10_000_000 ? maxHeapSize / 10_000 * initObjectPercent : maxHeapSize * initObjectPercent / 10_000;
        if (initObjCount < 10) initObjCount = 10;
        freeSpace = new BiHeap2int(initObjCount);
        freeSpace.insert(maxHeapSize, 0); // добавим всё пространство как свободное, начиная с адреса 0
        sizeInPos = new IntHashTableChained<Integer>(initObjCount / 4);
    }

    // "размещает", т.е. помечает как занятый блок памяти с количеством ячеек массива heap равным size.
    // Соответственно это должен быть непрерывный блок (последовательность ячеек), которые на момент
    // выделения свободны. Возвращает "указатель" - индекс первой ячейки в массиве, размещенного блока.
    public int malloc(int size) throws OutOfMemoryException {
        int freeZoneIdx;
        try {
            freeZoneIdx = freeSpace.findMinValItemIdx(size);
        } catch (NoSuchElementException e1) {
            defrag(); // попробуем провести дефраг свободных областей
            try {
                freeZoneIdx = freeSpace.findMinValItemIdx(size);
            } catch (NoSuchElementException e2) {
                compact(); // попробуем провести компакт занятых областей
                try {
                    freeZoneIdx = freeSpace.findMinValItemIdx(size);
                } catch (NoSuchElementException e3) {
                    throw new OutOfMemoryException();
                }
            }
        }
        int s = freeSpace.getVal(freeZoneIdx);
        int pos = freeSpace.getValData(freeZoneIdx);
        if(s>size) {
            freeSpace.update(freeZoneIdx, s-size, pos+size);
        } else if(s==pos) {
            freeSpace.delete(freeZoneIdx);
        }
        sizeInPos.put(pos, size);
        return pos;
    }

    // "удаляет", т.е. помечает как свободный блок памяти по "указателю". Проверять валидность указателя - т.е.
    // то, что он соответствует началу ранее выделенного блока, а не его середине, или вообще, уже свободному.
    public void free(int index, int size) throws InvalidPointerException {
        Integer s = sizeInPos.get(index);
        if(s==null || s!=size) throw new InvalidPointerException();
        sizeInPos.remove(index);
        freeSpace.insert(size, index);
    }

    // осуществляет дефрагментацию кучи - ищет смежные свободные блоки, границы которых
    // соприкасаются и которые можно слить в один.
    public void defrag() throws OutOfMemoryException {
        int size = freeSpace.datas.size;
        if(size==0) throw new OutOfMemoryException();
        int[] poses = freeSpace.datas.nums;
        int[] sizes = freeSpace.items.nums;
        BiHeap2int fs = new BiHeap2int(initObjCount);

        int from = poses[0];
        int to = from + sizes[0] + 1;

        for(int i=1; i< size; i++) {
            int p = poses[i];
            if(p==to) {
                to += sizes[i];
            } else {
                fs.insert(from, to-from - 1);
                from = p;
                to = from + sizes[i] + 1;
            }
        }

        fs.insert(from, to-from);
        this.freeSpace = fs;
    }

    // компактизация кучи - перенос всех занятых блоков в начало хипа, с копированием самих данных - элементов
    // массива. Для более точной имитации производительности копировать просто в цикле по одному элементу, не
    // используя System.arraycopy. Обязательно запускаем compact из malloc если не нашли блок подходящего размера
    public void compact() {

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(bytes.length);
        for (byte b : bytes) {
            sb.append(b);
        }
        return "Heap{" +
                "bytes=" + sb.toString() +
                ", maxHeapSize=" + maxHeapSize +
                '}';
    }
}