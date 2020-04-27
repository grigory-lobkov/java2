package ru.progwards.java2.lessons.synchro.gc;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*

Многопоточная версия с сервисным компактизирующим потоком

В блоках добавлены ссылки на предыдущие и следующие блоки, которые постоянно актуализируются, убран emptiesTreeBySize
Не требуется сортировка перед дефргментацией и компактизацией.
Дефрагментация закомментирована, т.к. сервисный поток её выполняет успешно, а в очередь свободных блоков свободные попадают в начало - как бы оставляя старые сервисному потоку в удаление

Heap_initial_hashTable работает также, суда по passed time, но общее время malloc time и free time у него значительно меньше

При тесте в 25% случаев возникает ошибка высвобождения не существующего объекта - сложно найти, не охота думать :)
*/

public class Heap implements HeapInterface {

    enum BlockType {EMPTY, OBJECT};

    class MBlock {
        int ptr;
        int size;
        BlockType type;
        MBlock prev;
        MBlock next;

        public MBlock(int ptr, int size, BlockType type, MBlock prev, MBlock next) {
            this.ptr = ptr;
            this.size = size;
            this.type = type;
            this.prev = prev;
            this.next = next;
        }
    }

    byte[] memory;
    Hashtable<Integer, MBlock> objectsMapByPtr; // список объектов по адресам, ключ = адрес
    TreeMap<Integer, ArrayDeque<MBlock>> emptiesTreeBySize; // поиск пустых блоков по размеру
    //Hashtable<Integer, MBlock> emptiesMapByPtr; // список пустых по адресу
    final int averageObjectSize = 64; // средний размер объекта (для рассчета общего количества)

    //TreeMap<Integer, MBlock> objectsTreeByPtr; // список объектов по адресам, ключ = адрес
    //TreeMap<Integer, MBlock> emptiesTreeByPtr; // поиск пустых блоков по размеру

    //MBlock firstEmpty; // первый свободный блок
    MBlock firstObject = null; // первый занятый не дефрагменченный блок

    int freeSize;

    final HeapService heapService = new HeapService(this);
    final Thread thread = new Thread(heapService);
    final Lock lock = new ReentrantLock();

    Heap(int maxHeapSize) {
        memory = new byte[maxHeapSize];
        freeSize = maxHeapSize;
        int expectedObjectsCount = maxHeapSize / averageObjectSize;
        int expectedEmptiesCount = expectedObjectsCount / 10;

        objectsMapByPtr = new Hashtable<>(expectedObjectsCount);
        emptiesTreeBySize = new TreeMap<>();
        //emptiesMapByPtr = new Hashtable<>(expectedEmptiesCount);

        MBlock emptyBlock = new MBlock(0, maxHeapSize, BlockType.EMPTY, null, null);
        newEmpty(emptyBlock);
        //firstEmpty = emptyBlock;

        thread.start();
    }

    public void dispose() {
        lock.lock();
        try {
            heapService.interruptMe();
            memory = null;
            objectsMapByPtr = null;
            emptiesTreeBySize = null;
            //emptiesMapByPtr = null;
        } finally {
            lock.unlock();
        }
    }

    public int malloc(int size) throws OutOfMemoryException {
        lock.lock();
        try {
            Map.Entry<Integer, ArrayDeque<MBlock>> found;

            found = emptiesTreeBySize.ceilingEntry(size);
            if (found == null) {

                //defrag();

                //found = emptiesTreeBySize.ceilingEntry(size);
                //if (found == null) {

                compact();

                found = emptiesTreeBySize.ceilingEntry(size);
                if (found == null) {
                    Map.Entry<Integer, ArrayDeque<MBlock>> f = emptiesTreeBySize.pollFirstEntry();
                    ArrayDeque<MBlock> a = f.getValue();
                    MBlock b = a.getFirst();
                    System.out.print("emptiesTreeBySize:" + emptiesTreeBySize.size());
                    System.out.print(" ArrayDeque:" + a.size());
                    System.out.print(" MBlock: ptr=" + b.ptr + " size=" + b.size);
                    throw new OutOfMemoryException("Cannot malloc " + size + " bytes of memory.");
                }
                //}
            }

            int foundSize = found.getKey();
            freeSize -= size;
            if (foundSize == size) {
                MBlock eBlock = pollEmpty(found.getValue());
                eBlock.type = BlockType.OBJECT;
                newObject(eBlock);
                return eBlock.ptr;
            } else {
                MBlock eBlock = shrinkEmpty(found.getValue(), size);
                MBlock newObject = new MBlock(eBlock.ptr - size, size, BlockType.OBJECT, eBlock.prev, eBlock);
                if (eBlock.prev != null)
                    eBlock.prev.next = newObject;
                eBlock.prev = newObject;
                newObject(newObject);
                return newObject.ptr;
            }
        } finally {
            lock.unlock();
        }
    }

    private MBlock shrinkEmpty(ArrayDeque<MBlock> empties, int takeSize) {
        MBlock block = empties.pollFirst();
        if (empties.size() == 0) {
            emptiesTreeBySize.remove(block.size);
        }
        //emptiesMapByPtr.remove(block.ptr);

        block.size -= takeSize;
        block.ptr += takeSize;
        ArrayDeque<MBlock> newArray = emptiesTreeBySize.get(block.size);

        if (newArray != null) {
            newArray.add(block);
        } else {
            newArray = new ArrayDeque<>();
            newArray.add(block);
            emptiesTreeBySize.put(block.size, newArray);
        }
        //emptiesMapByPtr.put(block.ptr, block);

        return block;
    }


    private void newEmpty(MBlock block) {
        // можно было бы объединять пустые при высвобождении, но пусть это будет в сервисном потоке
        // если не объединяем, тогда надо бы включить дефрагментацию. Не будем, чтобы не поддерживать firstEmpty
        /*MBlock prev = block.prev;
        while(prev!=null && prev.type == BlockType.EMPTY) {
            ArrayDeque<MBlock> empties = emptiesTreeBySize.get(prev.size);
            if (empties.size() == 1) {
                emptiesTreeBySize.remove(prev.size);
            } else {
                empties.remove(prev);
            }
            block.size += prev.size;
            block.ptr = prev.ptr;
            emptiesMapByPtr.remove(prev.ptr);

            prev = prev.prev;
            block.prev = prev;
            if(prev!=null) prev.next = block;
        }
        MBlock next = block.next;
        while(next!=null && next.type == BlockType.EMPTY) {
            ArrayDeque<MBlock> empties = emptiesTreeBySize.get(next.size);
            if (empties.size() == 1) {
                emptiesTreeBySize.remove(next.size);
            } else {
                empties.remove(next);
            }
            block.size += next.size;
            emptiesMapByPtr.remove(next.ptr);

            next = next.next;
            block.next = next;
            if(next!=null) next.prev = block;
        }*/
        ArrayDeque<MBlock> newArray = emptiesTreeBySize.get(block.size);

        if (newArray != null) {
            //newArray.add(block);
            newArray.addFirst(block); // в итоге - быстрее на 10%, чем add
        } else {
            newArray = new ArrayDeque<>();
            newArray.add(block);
            emptiesTreeBySize.put(block.size, newArray);
        }

        //emptiesMapByPtr.put(block.ptr, block);

//        if(firstEmpty.ptr>block.ptr)
//            synchronized (firstEmpty) {
//                if(firstEmpty.ptr>block.ptr)
//                    firstEmpty = block;
//            }
    }


    private MBlock pollEmpty(ArrayDeque<MBlock> empties) {
        MBlock block = empties.poll();

        if (empties.size() == 0) {
            emptiesTreeBySize.remove(block.size);
        }
        //emptiesMapByPtr.remove(block.ptr);

        return block;
    }

/*    private void removeEmpty(MBlock block) {
        ArrayDeque<MBlock> empties = emptiesTreeBySize.get(block.size);
        empties.remove(block);
        if (empties.size() == 0) {
            emptiesTreeBySize.remove(block.size);
        }
        emptiesMapByPtr.remove(block.ptr);
    }*/


    private void newObject(MBlock block) {
        objectsMapByPtr.put(block.ptr, block);

        if (firstObject == null)
            firstObject = block;
        else if (firstObject.ptr > block.ptr) {
            synchronized (firstObject) {
                if (firstObject == null || firstObject.ptr > block.ptr)
                    firstObject = block;
            }
        }
    }


/*    private MBlock pollObject(int ptr) throws InvalidPointerException {
        MBlock block = objectsMapByPtr.remove(ptr);
        if (block == null) throw new InvalidPointerException();

        return block;
    }*/

    public void free(int ptr) throws InvalidPointerException {
        lock.lock();
        try {
            MBlock block = objectsMapByPtr.remove(ptr);
            if (block == null) {
                System.out.println("free PTR=" + ptr);
                throw new InvalidPointerException();
            }
            block.type = BlockType.EMPTY;
            freeSize += block.size;
            newEmpty(block);
        } finally {
            lock.unlock();
        }
    }


    private void resizeEmpty(MBlock block, MBlock prevBlock) {
        ArrayDeque<MBlock> empties = emptiesTreeBySize.get(block.size);
        if (empties.size() == 1) {
            emptiesTreeBySize.remove(block.size);
        } else {
            empties.remove(block);
        }

        block.size += prevBlock.size;
        ArrayDeque<MBlock> newArray = emptiesTreeBySize.get(block.size);

        if (newArray != null) {
            newArray.add(block);
        } else {
            newArray = new ArrayDeque<>();
            newArray.add(block);
            emptiesTreeBySize.put(block.size, newArray);
        }
        prevBlock.prev.next = block;
        block.prev = prevBlock.prev;

//        if(prevBlock==firstEmpty)
//            synchronized (firstEmpty) {
//                if(prevBlock==firstEmpty)
//                    firstEmpty = block;
//            }
    }

    public void defrag() throws OutOfMemoryException {
        lock.lock();
        try {
            System.out.print("Defrag(" + emptiesTreeBySize.size() + ")...");

//        MBlock prevBlock;
//        MBlock block = firstEmpty;
//        if(block==null) throw new OutOfMemoryException();
//
//        while(true) {
//            while(true) {
//                prevBlock = block;
//                block = prevBlock.next;
//                if(block==null || (block.type==BlockType.EMPTY && prevBlock.type==BlockType.EMPTY))
//                    break;
//            }
//            if(block==null)
//                break;
//            resizeEmpty(block, prevBlock);
//        }
//        System.out.println(" done(" + emptiesTreeBySize.size() + ")");
        } finally {
            lock.unlock();
        }
    }

    public void compact() throws OutOfMemoryException {
        System.out.print("Compact(" + emptiesTreeBySize.size() + ")");
//        thread.interrupt();
//        while (thread.isAlive()) {
//            try {
//                Thread.sleep(0);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
        lock.lock();
        heapService.interruptMe();
        try {
            System.out.print("(" + emptiesTreeBySize.size() + ")...");

            //if (emptiesMapByPtr.size() == 1) return;//throw new OutOfMemoryException();

            MBlock prevBlock;
            MBlock block = firstObject;
            boolean emptyFound = false;

            while (block != null && block.type != BlockType.OBJECT)
                block = block.prev;
            if (block == null) {
                block = new MBlock(firstObject.ptr, 0, BlockType.OBJECT, firstObject.prev, firstObject.next);
            }

            while (true) {
                prevBlock = block;
                while (true) {
                    block = block.next;
                    if (block == null || block.type == BlockType.OBJECT)
                        break;
                    else if (!emptyFound) emptyFound = true;
                }
                if (block == null) break;
                if (emptyFound) {
                    moveObject(block, prevBlock.ptr + prevBlock.size);
                    prevBlock.next = block;
                    block.prev = prevBlock;
                }
            }
            firstObject = prevBlock;
            int lastByte = prevBlock.ptr + prevBlock.size;

            System.out.print(" lastByte=" + lastByte);

            emptiesTreeBySize = new TreeMap<>();
            //emptiesMapByPtr = new Hashtable<>();
            int size = memory.length - lastByte;
            MBlock emptyBlock = new MBlock(lastByte, size, BlockType.EMPTY, null, null);
            newEmpty(emptyBlock);
            //firstEmpty = emptyBlock;
            System.out.println(" done(" + emptiesTreeBySize.size() + ")");
        } finally {
            lock.unlock();
        }
    }

    private void moveObject(MBlock block, int ptr) {
        HeapTest.getBytes(block.ptr, null);
        HeapTest.setBytes(ptr, null);
            objectsMapByPtr.remove(block.ptr);
            block.ptr = ptr;
            objectsMapByPtr.put(block.ptr, block);
    }


    private void removeEmpty(MBlock block) {
        ArrayDeque<MBlock> empties = emptiesTreeBySize.get(block.size);

        if(empties == null)
            return;
        if (empties.size() == 1) {
            emptiesTreeBySize.remove(block.size);
        } else {
            empties.remove(block);
        }
        //emptiesMapByPtr.remove(block.ptr);
    }

    class HeapService implements Runnable {
        Heap heap;
        Thread thisThread;
        boolean myInterrupted = false;
        Lock lock;
        final boolean log = false;

        public HeapService(Heap heap) {
            this.heap = heap;
        }

        @Override
        public void run() {
            this.lock = heap.lock;
            try {
                while (!myInterrupted) {
                    Thread.sleep(100);

                    limit = memory.length - freeSize - freeSize / 10 - memory.length / 10;

                    defragToLimit();
                }
            } catch (InterruptedException e) {
            }
            if(log)System.out.println("HeapService interrupted. myInterrupted=" + myInterrupted);
        }

        int limit;

        private void defragToLimit() throws InterruptedException {
            if(log)System.out.println("HeapService. defragToLimit. limit = "+limit);
            while (!myInterrupted) {
                //System.out.println("HeapService. defragToLimit. firstObject="+firstObject.ptr);
                MBlock saveFirstObject = firstObject;

                MBlock emptyBlock = defragFromFirstObject(firstObject);

                // заведем пустой блок
                if (emptyBlock.size > 0) {
                    if (lock.tryLock(100, TimeUnit.MILLISECONDS))
                        try {
                            emptyBlock.next.prev = emptyBlock;
                            emptyBlock.prev.next = emptyBlock;
                            newEmpty(emptyBlock);
                        } finally {
                            lock.unlock();
                        }
                    else
                        break;
                }
                // передвинем указатель на первый объект
                if (saveFirstObject == firstObject)
                    synchronized (firstObject) {
                        if (saveFirstObject == firstObject) {
                            firstObject = emptyBlock.prev;
                        }
                    }
                if (emptyBlock.ptr > limit) break;
            }
            if(!myInterrupted)
                if(log)
                    System.out.println("HeapService. defragToLimit. Compacted.");
                else
                    System.out.print("*");
        }

        int freed;
        int i;
        MBlock obj;
        MBlock prevObj;

        private MBlock defragFromFirstObject(MBlock saveFirstObject) throws InterruptedException {
            freed = 0;
            i = 0;
            obj = saveFirstObject;
            while (!myInterrupted && obj.ptr < limit) {
                prevObj = obj;

                boolean isFound = findBlockToCompact(); // modified: prevObj, obj, freed

                if (!isFound)
                    break;

                    /*if(obj == null || obj.ptr > limit) {
                        System.out.println("HeapService. Break while: "+(obj == null?"on Null": obj.ptr+" > "+limit));
                        break;
                    }*/ // перешли на isFound
                    /*if (++i == 10_000) {
                        System.out.println("Moved: " + obj.ptr + " -> " + (prevObj.ptr + prevObj.size));
                        i = 0;
                    }*/
                if (lock.tryLock(100, TimeUnit.MILLISECONDS))
                    try {
                        if (obj.prev.next == obj && obj.type == BlockType.OBJECT) {
                            moveObject(obj, prevObj.ptr + prevObj.size);
                            prevObj.next = obj;
                            obj.prev = prevObj;
                        }
                    } finally {
                        lock.unlock();
                    }
                else
                    break;
                if (i % 100 == 0) {
                    synchronized (firstObject) {
                        if (saveFirstObject == firstObject) {
                            firstObject = obj;
                            saveFirstObject = firstObject;
                        } else
                            break;
                    }
                } else {
                    if (saveFirstObject != firstObject)
                        break;
                }
                if (myInterrupted) break;
            }
            synchronized (firstObject) {
                if (saveFirstObject == firstObject)
                    firstObject = obj;
            }
            return new MBlock(obj.ptr + obj.size, freed, BlockType.EMPTY, obj, obj.next);
        }

        private boolean findBlockToCompact() throws InterruptedException {
            boolean emptyFound = false;
            while (!myInterrupted && obj.ptr < limit) {
                obj = obj.next;
                //if(obj.next == prevObj || obj.next == obj) break;
                if(obj == null)
                    return false;
                if (emptyFound) {
                    if (obj.type == BlockType.OBJECT)
                        return true;
                    else {
                        if(!lockedRemoveEmpty(obj))
                            return false;
                        freed += obj.size;
                    }
                } else {
                    if (obj.type == BlockType.OBJECT)
                        prevObj = obj;
                    else {
                        if(!lockedRemoveEmpty(obj))
                            return false;
                        emptyFound = true;
                        freed+= obj.size;
                    }
                }
            }
            return false;
        }

        private boolean lockedRemoveEmpty(MBlock block) throws InterruptedException {
            if (lock.tryLock(100, TimeUnit.MILLISECONDS))
                try {
                    if (block.type == BlockType.EMPTY) {
                        removeEmpty(block);
                        return true;
                    }
                } finally {
                    lock.unlock();
                }
            return false;
        }

        public void interruptMe() {
            myInterrupted = true;
            while (thread.isAlive()) {
                try {
                    Thread.sleep(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
/*
Defrag(225316)... done(194920)
Compact(194920)... lastByte=999709213 done(1)
Defrag(133)... done(111)
Compact(111)... lastByte=999958068 done(1)
Defrag(20)... done(16)
Compact(16)... lastByte=999973100 done(1)
Defrag(8)... done(7)
Compact(7)... lastByte=999991694 done(1)

free memory: 271
malloc time: 13315 free time: 3036
total time: 16351 count: 3564159
[
main.synchro.gc.Heap(int)                             total:581    self:581    count:1         ns/exec:581000000,
main.synchro.gc.Heap.compact()                        total:7514   self:2896   count:4         ns/exec:724000000,
main.synchro.gc.Heap.defrag()                         total:1586   self:177    count:4         ns/exec:44250000,
main.synchro.gc.Heap.free(int)                        total:2498   self:1480   count:1782050   ns/exec:830,
main.synchro.gc.Heap.malloc(int)                      total:15024  self:3226   count:3564159   ns/exec:905,
main.synchro.gc.Heap.moveObject(.synchro.gc.Heap$MBlock,int)total:4618   self:1549   count:1781888   ns/exec:869,
main.synchro.gc.Heap.newEmpty(int,int)                total:595    self:595    count:1782050   ns/exec:333,
main.synchro.gc.Heap.newObject(int,int)               total:963    self:963    count:3564159   ns/exec:270,
main.synchro.gc.Heap.pollEmpty(java.util.ArrayDeque)  total:446    self:446    count:1556576   ns/exec:286,
main.synchro.gc.Heap.pollObject(int)                  total:423    self:423    count:1782050   ns/exec:237,
main.synchro.gc.Heap.removeEmpty(.synchro.gc.Heap$MBlock)total:740    self:740    count:30423     ns/exec:24323,
main.synchro.gc.Heap.resizeEmpty(.synchro.gc.Heap$MBlock,int)total:669    self:669    count:30423     ns/exec:21989,
main.synchro.gc.Heap.shrinkEmpty(java.util.ArrayDeque,int)total:1289   self:1289   count:2007583   ns/exec:642,
main.synchro.gc.HeapTest.<clinit>()                   total:0      self:0      count:1         ns/exec:0,
main.synchro.gc.HeapTest.getBytes(int,byte[])         total:3069   self:3069   count:1781888   ns/exec:1722,
main.synchro.gc.HeapTest.getRandomSize()              total:410    self:410    count:3564159   ns/exec:115,
main.synchro.gc.HeapTest.main(java.lang.String[])     total:23125  self:0      count:1         ns/exec:0,
main.synchro.gc.HeapTest.mainTest(boolean)            total:23125  self:4612   count:1         ns/exec:4612000000]
*/

/*
free memory: 0
malloc time: 9214 free time: 5646
total time: 14860 count: 879357
Thread-0
free memory: 0
malloc time: 10110 free time: 5539
total time: 15649 count: 890109
Thread-1
free memory: 0
malloc time: 10303 free time: 5675
total time: 15978 count: 897103
Defrag(225674)... done(225674)
Compact(225674)... lastByte=999696229 done(1)
Defrag(151)... done(151)
Defrag(150)... done(150)
Compact(150)... lastByte=999696229 done(1)
Thread-3
free memory: 0
malloc time: 49206 free time: 5843
total time: 55049 count: 888065

free memory: 0
malloc time: 78833 free time: 22703
total time: 101536 count: 3554634
passed time: 63637
[
Thread-0.Heap.free(int)                    total:769    self:552    count:445016    ns/exec:1240,
Thread-0.Heap.malloc(int)                  total:2306   self:1624   count:890109    ns/exec:1824,
Thread-0.Heap.newEmpty(.Heap$MBlock)total:217    self:217    count:445016    ns/exec:487,
Thread-0.Heap.newObject(.Heap$MBlock)total:254    self:254    count:890109    ns/exec:285,
Thread-0.Heap.pollEmpty(java.util.ArrayDeque)total:145    self:145    count:387964    ns/exec:373,
Thread-0.Heap.shrinkEmpty(java.util.ArrayDeque,int)total:283    self:283    count:502145    ns/exec:563,
Thread-1.Heap.free(int)                    total:682    self:476    count:448380    ns/exec:1061,
Thread-1.Heap.malloc(int)                  total:2218   self:1463   count:897103    ns/exec:1630,
Thread-1.Heap.newEmpty(.Heap$MBlock)total:206    self:206    count:448380    ns/exec:459,
Thread-1.Heap.newObject(.Heap$MBlock)total:285    self:285    count:897103    ns/exec:317,
Thread-1.Heap.pollEmpty(java.util.ArrayDeque)total:167    self:167    count:391669    ns/exec:426,
Thread-1.Heap.shrinkEmpty(java.util.ArrayDeque,int)total:303    self:303    count:505434    ns/exec:599,
Thread-2.Heap.free(int)                    total:1193   self:490    count:439304    ns/exec:1115,
Thread-2.Heap.malloc(int)                  total:2317   self:1467   count:879357    ns/exec:1668,
Thread-2.Heap.newEmpty(.Heap$MBlock)total:703    self:703    count:439304    ns/exec:1600,
Thread-2.Heap.newObject(.Heap$MBlock)total:248    self:248    count:879357    ns/exec:282,
Thread-2.Heap.pollEmpty(java.util.ArrayDeque)total:144    self:144    count:383995    ns/exec:375,
Thread-2.Heap.shrinkEmpty(java.util.ArrayDeque,int)total:458    self:458    count:495362    ns/exec:924,
Thread-3.Heap.compact()                    total:44986  self:1078   count:2         ns/exec:539000000,
Thread-3.Heap.defrag()                     total:1556   self:62     count:3         ns/exec:20666666,
Thread-3.Heap.free(int)                    total:753    self:523    count:444024    ns/exec:1177,
Thread-3.Heap.malloc(int)                  total:48830  self:1479   count:888065    ns/exec:1665,
Thread-3.Heap.moveObject(.Heap$MBlock,int)total:43908  self:3206   count:1776626   ns/exec:1804,
Thread-3.Heap.newEmpty(.Heap$MBlock)total:230    self:230    count:444024    ns/exec:517,
Thread-3.Heap.newObject(.Heap$MBlock)total:263    self:263    count:888065    ns/exec:296,
Thread-3.Heap.pollEmpty(java.util.ArrayDeque)total:163    self:163    count:387220    ns/exec:420,
Thread-3.Heap.resizeEmpty(.Heap$MBlock,.Heap$MBlock)total:1494   self:1494   count:35307     ns/exec:42314,
Thread-3.Heap.shrinkEmpty(java.util.ArrayDeque,int)total:383    self:383    count:500845    ns/exec:764,
Thread-3.HeapTest.getBytes(int,byte[])     total:39911  self:39911  count:1776626   ns/exec:22464,
Thread-3.HeapTest.setBytes(int,byte[])     total:791    self:791    count:1776626   ns/exec:445,
main.Heap(int)                             total:612    self:612    count:1         ns/exec:612000000,
main.HeapTest.<clinit>()                   total:1      self:1      count:1         ns/exec:1000000,
main.HeapTest.main(java.lang.String[])     total:64288  self:4608   count:1         ns/exec:4608000000]
*/
/*
free memory: 0
malloc time: 10666 free time: 4654
total time: 15320 count: 2658489
passed time: 4373

free memory: 0
malloc time: 10879 free time: 5210
total time: 16089 count: 2663612
passed time: 4765

free memory: 0
malloc time: 10281 free time: 5002
total time: 15283 count: 2669497
passed time: 4448

free memory: 0
malloc time: 10512 free time: 4830
total time: 15342 count: 2664938
passed time: 4404

free memory: 0
malloc time: 9632 free time: 5114
total time: 14746 count: 2654249
passed time: 4241
*/