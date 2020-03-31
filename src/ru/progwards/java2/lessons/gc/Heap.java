package ru.progwards.java2.lessons.gc;

import java.util.Iterator;
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

    public byte[] bytes;           // куча
    public final int maxHeapSize;  // максимальный размер кучи

    public final int averageObjectSize = 64;   // средний размер объекта при на половину заполненной куче
    //public final int freeToObjectsCountToDefrag = 200; // отношение количества свободных к занятым, чтобы провести оптимизацию свободных
    public final int defragCountLimit = 2;     // количество дефрагментаций, проведенных часто, после которых делаем compact

    protected int initObjCount;               // базовый размер служебных таблиц, расчет при инициализации
    protected IntDictionary<Integer> objects; // индекс - позиция, значение - размер объекта
    protected BiHeap2int empties;             // свободные блоки в памяти (сравнение - по размеру блока)
    protected int defragCount;                // количество дефрагментаций, проведенных часто

    public Heap() {
        this.maxHeapSize = 64;
        init();
    }

    public Heap(int maxHeapSize) {
        this.maxHeapSize = maxHeapSize;
        init();
    }

    private void init() {
        bytes = new byte[maxHeapSize];

        initObjCount = maxHeapSize / 2 / averageObjectSize;
        if (initObjCount < 16) initObjCount = 16;
        empties = new BiHeap2int(initObjCount);
        empties.insert(maxHeapSize, 0); // добавим всё пространство как свободное, начиная с адреса 0

        objects = new IntHashTableChained<Integer>(initObjCount / 4);
    }

    // "размещает", т.е. помечает как занятый блок памяти с количеством ячеек массива heap равным size.
    // Соответственно это должен быть непрерывный блок (последовательность ячеек), которые на момент
    // выделения свободны. Возвращает "указатель" - индекс первой ячейки в массиве, размещенного блока.
    public int malloc(int size) throws OutOfMemoryException {
        if(size==0) throw new InvalidPointerException();
        int freeZoneIdx;
        //if(empties.size()>objects.size()*freeToObjectsCountToDefrag/100) defrag();
        try {
            freeZoneIdx = empties.findMinValItemIdx(size);
            if (defragCount > 0) {
                defragCount--;
            }
        } catch (NoSuchElementException e) {
            freeZoneIdx = getFreeZoneIdxWithDefrag(size);
        }
        int eSize = empties.getVal(freeZoneIdx);
        int ePos = empties.getValData(freeZoneIdx);
        if (eSize > size) {
            //System.out.println("freeZoneIdx="+freeZoneIdx+" size "+eSize+"->"+(eSize - size)+" pos "+ePos+"->"+(ePos + size));
            empties.update(freeZoneIdx, eSize - size, ePos + size);
        } else if (eSize == size) {
            //System.out.println("freeZoneIdx="+freeZoneIdx+" size="+eSize+" pos="+ePos+" deleted");
            empties.delete(freeZoneIdx);
        }
        objects.put(ePos, size);
        //System.out.println("Malloced: pos="+ePos+" size="+size);
        return ePos;
    }


    private int getFreeZoneIdxWithDefrag(int size) throws OutOfMemoryException {
        defragCount++;
        if(defragCount>=defragCountLimit) {
            return getFreeZoneIdxWithCompact(size);
        }

        defrag(); // попробуем провести дефраг свободных областей

        try {
            return empties.findMinValItemIdx(size);
        } catch (NoSuchElementException e1) {
            return getFreeZoneIdxWithCompact(size);
        }
    }


    private int getFreeZoneIdxWithCompact(int size) throws OutOfMemoryException {

        compact(); // попробуем провести компакт занятых областей

        if(empties.getValData(0)<size) throw new OutOfMemoryException();

        try {
            return empties.findMinValItemIdx(size);
        } catch (NoSuchElementException e2) {
            throw new OutOfMemoryException();
        }
    }

    // "удаляет", т.е. помечает как свободный блок памяти по "указателю". Проверять валидность указателя - т.е.
    // то, что он соответствует началу ранее выделенного блока, а не его середине, или вообще, уже свободному.
    public void free(int pos, int size) throws InvalidPointerException {
        Integer s = objects.get(pos);
        if(s==null || s!=size) throw new InvalidPointerException();
        objects.remove(pos);
        empties.insert(size, pos);
        //System.out.println("Freed: pos="+pos+" size="+size);
    }
    public void free(int pos) throws InvalidPointerException {
        Integer s = objects.get(pos);
        if(s==null) throw new InvalidPointerException();
        objects.remove(pos);
        empties.insert(s, pos);
        //System.out.println("Freed: pos="+pos+" size="+s);
    }

    // осуществляет дефрагментацию кучи - ищет смежные свободные блоки, границы которых
    // соприкасаются и которые можно слить в один.
    public void defrag() throws OutOfMemoryException {
        //System.out.println("Defragmenting...");
        int eSize = empties.datas.size;
        if(eSize==0) throw new OutOfMemoryException();
        int oSize = objects.size();

        if(eSize>oSize) {
            defragByObjects();
        } else {
            defragByEmpties();
        }
        System.out.println("Defragmented: wasCount="+eSize+" nowCount="+empties.size());
    }

    private void defragByObjects() {
        int oCount = objects.size();
        int[] oPoses = new int[oCount];
        int[] oSizes = new int[oCount];
        BiHeap2int empts = new BiHeap2int(initObjCount);
        // считаем все адреса и размеры объектов
        Iterator<IntDictionary<Integer>.Entry> k = objects.getIterator();
        int i = 0;
        while (k.hasNext()) {
            IntDictionary<Integer>.Entry e = k.next();
            oPoses[i] = e.key;
            oSizes[i++] = e.value;
        }
        sort2arrays(oPoses, oSizes, oCount); // отсортируем по адресам
        // соберем пустые области
        int prevEnd = 0;
        for (i = 0; i < oCount; i++) {
            int start = oPoses[i];
            int size = oSizes[i];
            if (prevEnd < start) {
                empts.insert(size, start); //нашли
            }
            prevEnd = start + size;
        }
        int l = bytes.length;
        if (prevEnd < l) {
            empts.insert(l - prevEnd, prevEnd); //нашли
        }
        this.empties = empts;
    }

    private void defragByEmpties() {
        int eSize = empties.datas.size;
        int[] ePoses = empties.datas.nums;
        int[] eSizes = empties.items.nums;
        sort2arrays(ePoses, eSizes, eSize); // отсортируем по адресам
        BiHeap2int empts = new BiHeap2int(initObjCount);

        // ищем сподряд идущие области и объединяем в новый список свободных областей
        int from = ePoses[0];
        int to = from + eSizes[0];
        for(int i=1; i< eSize; i++) {
            int p = ePoses[i];
            if(p==to) {
                to += eSizes[i];
            } else {
                empts.insert(to-from, from); //нашли
                //System.out.println("found: size="+(to-from)+" from="+from);
                from = p;
                to = from + eSizes[i];
            }
        }
        empts.insert(to-from, from); //обязательно добавляем в конце
        //System.out.println("found! size="+(to-from)+" from="+from);

        this.empties = empts;
    }

    // компактизация кучи - перенос всех занятых блоков в начало хипа, с копированием самих данных - элементов
    // массива. Для более точной имитации производительности копировать просто в цикле по одному элементу, не
    // используя System.arraycopy. Обязательно запускаем compact из malloc если не нашли блок подходящего размера
    public void compact() throws OutOfMemoryException {
        //System.out.println("Compacting...");
        int eSize=empties.datas.size;
        if(eSize==0) throw new OutOfMemoryException();

        int freePosStart = bytes.length; // адрес свободного блока

        // найдем наименьшее начало адреса свободного блока
        int[] ePoses = empties.datas.nums;
        for(int i=eSize-1;i>=0;i--) {
            //System.out.println("freeAll pos="+ePoses[i]);
            if (ePoses[i] < freePosStart) {
                freePosStart = ePoses[i];
            }
        }
        //System.out.println("freePosStart="+freePosStart);

        // переведем таблицу в два массива: позиции и размеры
        int oCount = objects.size();
        int[] oPoses=new int[oCount];
        int[] oSizes=new int[oCount];
        int oToMoveCount=0;
        Iterator<IntDictionary<Integer>.Entry> k = objects.getIterator();
        while(k.hasNext()) {
            IntDictionary<Integer>.Entry e = k.next();
            if(e.key>freePosStart) {
                //System.out.println("oToMove "+oToMoveCount+": pos="+e.key+" size="+e.value);
                oPoses[oToMoveCount] = e.key;
                oSizes[oToMoveCount] = e.value;
                oToMoveCount++;
            } else {
                //System.out.println("oToStay "+oToMoveCount+": pos="+e.key+" size="+e.value);
            }
        }
        // отсортируем пару массивов
        sort2arrays(oPoses, oSizes, oToMoveCount);

        // перемещаем объекты на новые позиции
        for (int i=0; i<oToMoveCount; i++) {
            final int p = oPoses[i];
            final int s = oSizes[i];
            // побайтное(чтобы замедлить алгоритм) копирование объекта в новое место
            for(int b=0;b<s;b++) {
                bytes[freePosStart+b] = bytes[p+b];
            }
            //System.arraycopy(bytes, p, bytes, freePosStart, s);
            objects.change(p,freePosStart);
            //System.out.println("oMoved: pos "+p+"->"+freePosStart+" size="+s+" newFreePosStart="+(freePosStart+s));
            freePosStart+=s;
        }

        // заводим новый объект пустых мест
        empties = new BiHeap2int(initObjCount);
        empties.insert(bytes.length - freePosStart, freePosStart); // добавим всё пространство как свободное, начиная с адреса 0
        System.out.println("Compacted: freeFrom="+freePosStart+" freeBytes="+(bytes.length - freePosStart));
    }

    // провести сортировку массивов по массиву данных от наименьшего к наибольшему
    public void sort2arrays(int[] as, int[] a2, int size) {
        for (int i = 0; i < size; i++) {
            int min = as[i];
            int min_i = i;
            for (int j = i + 1; j < size; j++) {
                if (as[j] < min) {
                    min = as[j];
                    min_i = j;
                }
            }
            if (i != min_i) {
                int tmp = as[i];
                as[i] = as[min_i];
                as[min_i] = tmp;
                tmp = a2[i];
                a2[i] = a2[min_i];
                a2[min_i] = tmp;
            }
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(bytes.length*4);
        for (byte b : bytes) {
            sb.append(b+" ");
        }
        return "Heap{" +
                "bytes=" + sb.toString() +
                ", maxHeapSize=" + maxHeapSize +
                '}';
    }

}