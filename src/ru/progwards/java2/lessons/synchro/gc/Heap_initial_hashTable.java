package ru.progwards.java2.lessons.synchro.gc;

import java.util.*;

/*

Достаточно оптимальная версия для многопоточного использования, без доп.потоков

emptiesMapByPtr можно было бы убрать, если взять авто-дефрагментацию из Heap методе newEmpty - не буду тратить время, но вероятно, эффективность Кучи повысится
*/

public class Heap_initial_hashTable implements HeapInterface {

    class MBlock {
        int ptr;
        int size;

        public MBlock(int ptr, int size) {
            this.ptr = ptr;
            this.size = size;
        }
    }

    byte[] memory;
    Hashtable<Integer, MBlock> objectsMapByPtr; // список объектов по адресам, ключ = адрес
    TreeMap<Integer, ArrayDeque<MBlock>> emptiesTreeBySize; // поиск пустых блоков по размеру
    Hashtable<Integer, MBlock> emptiesMapByPtr; // список пустых по адресу
    final int averageObjectSize = 64; // средний размер объекта (для рассчета общего количества)

    Heap_initial_hashTable(int maxHeapSize) {
        memory = new byte[maxHeapSize];
        int expectedObjectsCount = maxHeapSize / averageObjectSize;
        int expectedEmptiesCount = expectedObjectsCount / 10;

        objectsMapByPtr = new Hashtable<>(expectedObjectsCount);
        emptiesTreeBySize = new TreeMap<>();
        emptiesMapByPtr = new Hashtable<>(expectedEmptiesCount);

        MBlock emptyBlock = new MBlock(0, maxHeapSize);
        ArrayDeque<MBlock> emptyBlockArray = new ArrayDeque<>();
        emptyBlockArray.add(emptyBlock);
        emptiesTreeBySize.put(maxHeapSize, emptyBlockArray);
        emptiesMapByPtr.put(0, emptyBlock);
    }

    @Override
    public void dispose() {
    }

    public synchronized int malloc(int size) throws OutOfMemoryException {
        Map.Entry<Integer, ArrayDeque<MBlock>> found;

        found = emptiesTreeBySize.ceilingEntry(size);
        if (found == null) {

            defrag();

            found = emptiesTreeBySize.ceilingEntry(size);
            if (found == null) {

                compact();

                found = emptiesTreeBySize.ceilingEntry(size);
                if (found == null) throw new OutOfMemoryException("Cannot malloc " + size + " bytes of memory.");
            }
        }

        int foundSize = found.getKey();
        if (foundSize == size) {
            MBlock foundEmpty = pollEmpty(found.getValue());
            MBlock newObject = newObject(foundEmpty.ptr, size);
            return newObject.ptr;
        } else {
            int ptr = shrinkEmpty(found.getValue(), size);
            MBlock newObject = newObject(ptr, size);
            return ptr;
        }
    }

    private int shrinkEmpty(ArrayDeque<MBlock> empties, int takeSize) {
        MBlock block = empties.pollFirst();
        int result = block.ptr;
        if (empties.size() == 0) {
            emptiesTreeBySize.remove(block.size);
        }
        emptiesMapByPtr.remove(block.ptr);

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
        emptiesMapByPtr.put(block.ptr, block);

        return result;
    }

    private void resizeEmpty(MBlock block, int addSize) {
        ArrayDeque<MBlock> empties = emptiesTreeBySize.get(block.size);
        if (empties.size() == 1) {
            emptiesTreeBySize.remove(block.size);
        } else {
            empties.remove(block);
        }

        block.size += addSize;
        ArrayDeque<MBlock> newArray = emptiesTreeBySize.get(block.size);

        if (newArray != null) {
            newArray.add(block);
        } else {
            newArray = new ArrayDeque<>();
            newArray.add(block);
            emptiesTreeBySize.put(block.size, newArray);
        }
    }

    private MBlock newEmpty(int ptr, int size) {
        MBlock block = new MBlock(ptr, size);
        ArrayDeque<MBlock> newArray = emptiesTreeBySize.get(size);

        if (newArray != null) {
            newArray.add(block);
        } else {
            newArray = new ArrayDeque<>();
            newArray.add(block);
            emptiesTreeBySize.put(size, newArray);
        }

        emptiesMapByPtr.put(ptr, block);

        return block;
    }


    private MBlock pollEmpty(ArrayDeque<MBlock> empties) {
        MBlock block = empties.poll();

        if (empties.size() == 0) {
            emptiesTreeBySize.remove(block.size);
        }
        emptiesMapByPtr.remove(block.ptr);

        return block;
    }

    private void removeEmpty(MBlock block) {
        ArrayDeque<MBlock> empties = emptiesTreeBySize.get(block.size);
        empties.remove(block);
        if (empties.size() == 0) {
            emptiesTreeBySize.remove(block.size);
        }
        emptiesMapByPtr.remove(block.ptr);
    }


    private MBlock newObject(int ptr, int size) {
        MBlock newObject = new MBlock(ptr, size);

        objectsMapByPtr.put(ptr, newObject);

        return newObject;
    }


    private MBlock pollObject(int ptr) throws InvalidPointerException {
        MBlock block = objectsMapByPtr.remove(ptr);
        if (block == null) throw new InvalidPointerException();

        return block;
    }


    public synchronized void free(int ptr) throws InvalidPointerException {
        MBlock block = pollObject(ptr);
        MBlock newEmpty = newEmpty(block.ptr, block.size);
    }

    public synchronized void defrag() throws OutOfMemoryException {
        System.out.print("Defrag(" + emptiesMapByPtr.size() + ")...");

        Object[] sorted = emptiesMapByPtr.values().toArray();
        Arrays.sort(sorted, Comparator.comparingInt(b -> ((MBlock) b).ptr));
        if (sorted.length <= 1) throw new OutOfMemoryException();

        MBlock prevBlock = (MBlock) sorted[0];
        int len = sorted.length;

        for (int i = 1; i < len; i++) {
            MBlock block = (MBlock) sorted[i];
            if (block.size > 0) {
                if (block.ptr == prevBlock.ptr + prevBlock.size) {
                    removeEmpty(prevBlock);
                    resizeEmpty(block, prevBlock.size);
                }
                prevBlock = block;
            }
        }
        System.out.println(" done(" + emptiesMapByPtr.size() + ")");
    }

    public synchronized void compact() throws OutOfMemoryException {
        System.out.print("Compact(" + emptiesMapByPtr.size() + ")...");

        Object[] sorted = objectsMapByPtr.values().toArray();
        Arrays.sort(sorted, Comparator.comparingInt(b -> ((MBlock) b).ptr));
        if (sorted.length == 0) throw new OutOfMemoryException();

        MBlock prevBlock = (MBlock) sorted[0];
        int len = sorted.length;

        for (int i = 1; i < len; i++) {
            MBlock block = (MBlock) sorted[i];
            int freePtr = prevBlock.ptr+prevBlock.size;
            if (freePtr < block.ptr) {
                moveObject(block, freePtr);
            }
            prevBlock = block;
        }
        int lastByte = prevBlock.ptr+prevBlock.size;

        System.out.print(" lastByte=" + lastByte);

        emptiesTreeBySize = new TreeMap<>();
        emptiesMapByPtr = new Hashtable<>();
        int size = memory.length - lastByte;
        MBlock block = new MBlock(lastByte, size);
        ArrayDeque<MBlock> newArray = new ArrayDeque<>();
        newArray.add(block);
        emptiesTreeBySize.put(size, newArray);
        emptiesMapByPtr.put(lastByte, block);
        System.out.println(" done(" + emptiesMapByPtr.size() + ")");
    }

    private void moveObject(MBlock block, int ptr) {
        HeapTest.getBytes(block.ptr, null);
        HeapTest.setBytes(ptr, null);
        objectsMapByPtr.remove(block.ptr);
        block.ptr = ptr;
        objectsMapByPtr.put(block.ptr, block);
    }
}
/*
Defrag(226221)... done(195890)
Compact(195890)... lastByte=999686802 done(1)
Defrag(91)... done(81)
Compact(81)... lastByte=999959011 done(1)
Defrag(22)... done(18)
Compact(18)... lastByte=999993275 done(1)

free memory: 568
malloc time: 12621 free time: 2973
total time: 15594 execsCount: 3554168
[
main.synchro.gc.Heap(int)                             total:572    self:572    execsCount:1         ns/exec:572000000,
main.synchro.gc.Heap.compact()                        total:4518   self:2156   execsCount:3         ns/exec:718666666,
main.synchro.gc.Heap.defrag()                         total:1611   self:181    execsCount:3         ns/exec:60333333,
main.synchro.gc.Heap.free(int)                        total:2450   self:1510   execsCount:1778305   ns/exec:849,
main.synchro.gc.Heap.malloc(int)                      total:11543  self:3408   execsCount:3554168   ns/exec:958,
main.synchro.gc.Heap.moveObject(.synchro.gc.Heap$MBlock,int)total:2362   self:1752   execsCount:1769642   ns/exec:990,
main.synchro.gc.Heap.newEmpty(int,int)                total:698    self:698    execsCount:1778305   ns/exec:392,
main.synchro.gc.Heap.newObject(int,int)               total:645    self:645    execsCount:3554168   ns/exec:181,
main.synchro.gc.Heap.pollEmpty(java.util.ArrayDeque)  total:415    self:415    execsCount:1551965   ns/exec:267,
main.synchro.gc.Heap.pollObject(int)                  total:242    self:242    execsCount:1778305   ns/exec:136,
main.synchro.gc.Heap.removeEmpty(.synchro.gc.Heap$MBlock)total:746    self:746    execsCount:30345     ns/exec:24583,
main.synchro.gc.Heap.resizeEmpty(.synchro.gc.Heap$MBlock,int)total:684    self:684    execsCount:30345     ns/exec:22540,
main.synchro.gc.Heap.shrinkEmpty(java.util.ArrayDeque,int)total:946    self:946    execsCount:2002203   ns/exec:472,
main.synchro.gc.HeapTest.<clinit>()                   total:0      self:0      execsCount:1         ns/exec:0,
main.synchro.gc.HeapTest.getBytes(int,byte[])         total:610    self:610    execsCount:1769642   ns/exec:344,
main.synchro.gc.HeapTest.getRandomSize()              total:312    self:312    execsCount:3554168   ns/exec:87,
main.synchro.gc.HeapTest.main(java.lang.String[])     total:18331  self:0      execsCount:1         ns/exec:0,
main.synchro.gc.HeapTest.mainTest(boolean)            total:18331  self:3454   execsCount:1         ns/exec:3454000000]
*/

/*
Thread-3
free memory: 0
malloc time: 10370 free time: 6888
total time: 17258 execsCount: 891884
Thread-1
free memory: 0
malloc time: 10308 free time: 6934
total time: 17242 execsCount: 887701
Thread-2
free memory: 0
malloc time: 11037 free time: 6537
total time: 17574 execsCount: 887607
Defrag(224409)... done(194591)
Compact(194591)... lastByte=999696791 done(1)
Defrag(124)... done(101)
Compact(101)... lastByte=999968019 done(1)
Defrag(23)... done(19)
Compact(19)... lastByte=999985357 done(1)
Defrag(25)... done(18)
Compact(18)... lastByte=999989277 done(1)
Defrag(34)... done(27)
Compact(27)... lastByte=999996313 done(1)
Defrag(11)... done(8)
Defrag(11)... done(9)
Compact(9)... lastByte=999999556 done(1)
Defrag(3)... done(2)
Compact(2)... lastByte=999999608 done(1)
Defrag(6)... done(5)
Compact(5)... lastByte=999999988 done(1)
Defrag(3)... done(2)
Compact(2)... lastByte=999999990 done(1)
Thread-0
free memory: 0
malloc time: 56366 free time: 6564
total time: 62930 execsCount: 894066

free memory: 0
malloc time: 57930 free time: 26923
total time: 84853 execsCount: 3561258
passed time: 76408
[
Thread-0.Heap.compact()                    total:55651  self:8527   execsCount:9         ns/exec:947444444,
Thread-0.Heap.defrag()                     total:2116   self:449    execsCount:10        ns/exec:44900000,
Thread-0.Heap.free(int)                    total:1171   self:650    execsCount:447853    ns/exec:1451,
Thread-0.Heap.malloc(int)                  total:59955  self:1386   execsCount:894066    ns/exec:1550,
Thread-0.Heap.moveObject(.Heap$MBlock,int)total:47124  self:2931   execsCount:1780546   ns/exec:1646,
Thread-0.Heap.newEmpty(int,int)            total:264    self:264    execsCount:447853    ns/exec:589,
Thread-0.Heap.newObject(int,int)           total:271    self:271    execsCount:894066    ns/exec:303,
Thread-0.Heap.pollEmpty(java.util.ArrayDeque)total:136    self:136    execsCount:390600    ns/exec:348,
Thread-0.Heap.pollObject(int)              total:257    self:257    execsCount:447853    ns/exec:573,
Thread-0.Heap.removeEmpty(.Heap$MBlock)total:882    self:882    execsCount:29867     ns/exec:29530,
Thread-0.Heap.resizeEmpty(.Heap$MBlock,int)total:785    self:785    execsCount:29867     ns/exec:26283,
Thread-0.Heap.shrinkEmpty(java.util.ArrayDeque,int)total:395    self:395    execsCount:503466    ns/exec:784,
Thread-0.HeapTest.getBytes(int,byte[])     total:43290  self:43290  execsCount:1780546   ns/exec:24312,
Thread-0.HeapTest.setBytes(int,byte[])     total:903    self:903    execsCount:1780546   ns/exec:507,
Thread-1.Heap.free(int)                    total:1057   self:652    execsCount:443875    ns/exec:1468,
Thread-1.Heap.malloc(int)                  total:2265   self:1281   execsCount:887701    ns/exec:1443,
Thread-1.Heap.newEmpty(int,int)            total:233    self:233    execsCount:443875    ns/exec:524,
Thread-1.Heap.newObject(int,int)           total:448    self:448    execsCount:887701    ns/exec:504,
Thread-1.Heap.pollEmpty(java.util.ArrayDeque)total:144    self:144    execsCount:388525    ns/exec:370,
Thread-1.Heap.pollObject(int)              total:172    self:172    execsCount:443875    ns/exec:387,
Thread-1.Heap.shrinkEmpty(java.util.ArrayDeque,int)total:392    self:392    execsCount:499176    ns/exec:785,
Thread-2.Heap.free(int)                    total:1075   self:664    execsCount:443731    ns/exec:1496,
Thread-2.Heap.malloc(int)                  total:2879   self:1796   execsCount:887607    ns/exec:2023,
Thread-2.Heap.newEmpty(int,int)            total:245    self:245    execsCount:443731    ns/exec:552,
Thread-2.Heap.newObject(int,int)           total:212    self:212    execsCount:887607    ns/exec:238,
Thread-2.Heap.pollEmpty(java.util.ArrayDeque)total:151    self:151    execsCount:387603    ns/exec:389,
Thread-2.Heap.pollObject(int)              total:166    self:166    execsCount:443731    ns/exec:374,
Thread-2.Heap.shrinkEmpty(java.util.ArrayDeque,int)total:720    self:720    execsCount:500004    ns/exec:1439,
Thread-3.Heap.free(int)                    total:1197   self:710    execsCount:445304    ns/exec:1594,
Thread-3.Heap.malloc(int)                  total:2129   self:1471   execsCount:891884    ns/exec:1649,
Thread-3.Heap.newEmpty(int,int)            total:281    self:281    execsCount:445304    ns/exec:631,
Thread-3.Heap.newObject(int,int)           total:237    self:237    execsCount:891884    ns/exec:265,
Thread-3.Heap.pollEmpty(java.util.ArrayDeque)total:141    self:141    execsCount:389404    ns/exec:362,
Thread-3.Heap.pollObject(int)              total:206    self:206    execsCount:445304    ns/exec:462,
Thread-3.Heap.shrinkEmpty(java.util.ArrayDeque,int)total:280    self:280    execsCount:502480    ns/exec:557]
*/
/*
adjustClass(ru.progwards.java2.lessons.synchro.gc.HeapTest)
adjustClass(ru.progwards.java2.lessons.synchro.gc.Heap_initial_hashTable)
179015679
177846611
178048496
177927919
107439523
105138603
106350922
105204309
33663650
35209685
34809693
33703681
Thread-3
free memory: 0
malloc time: 10173 free time: 6499
total time: 16672 execsCount: 888269
Thread-2
free memory: 0
malloc time: 10523 free time: 6797
total time: 17320 execsCount: 895603
Thread-0
free memory: 0
malloc time: 10498 free time: 6882
total time: 17380 execsCount: 891977
Defrag(223926)... done(194290)
Compact(194290)... lastByte=999685970 done(1)
Defrag(159)... done(145)
Compact(145)... lastByte=999971327 done(1)
Defrag(12)... done(9)
Compact(9)... lastByte=999988715 done(1)
Defrag(6)... done(4)
Compact(4)... lastByte=999988253 done(1)
Defrag(18)... done(13)
Compact(13)... lastByte=999994327 done(1)
Defrag(24)... done(18)
Compact(18)... lastByte=999999214 done(1)
Defrag(9)... done(6)
Compact(6)... lastByte=999999430 done(1)
Defrag(8)... done(7)
Compact(7)... lastByte=999999919 done(1)
Defrag(6)... done(5)
Compact(5)... lastByte=999999941 done(1)
Defrag(6)... done(6)
Compact(6)... lastByte=999999984 done(1)
Defrag(2)... done(2)
Compact(2)... lastByte=999999994 done(1)
Thread-1
free memory: 0
malloc time: 59149 free time: 7028
total time: 66177 execsCount: 888880

free memory: 0
malloc time: 67104 free time: 27206
total time: 94310 execsCount: 3564729
passed time: 73781
[
Thread-0.Heap_initial_hashTable.free(int)  total:1124   self:772    execsCount:446239    ns/exec:1730,
Thread-0.Heap_initial_hashTable.malloc(int)total:2097   self:1485   execsCount:891977    ns/exec:1664,
Thread-0.Heap_initial_hashTable.newEmpty(int,int)total:203    self:203    execsCount:446239    ns/exec:454,
Thread-0.Heap_initial_hashTable.newObject(int,int)total:234    self:234    execsCount:891977    ns/exec:262,
Thread-0.Heap_initial_hashTable.pollEmpty(java.util.ArrayDeque)total:131    self:131    execsCount:389939    ns/exec:335,
Thread-0.Heap_initial_hashTable.pollObject(int)total:149    self:149    execsCount:446239    ns/exec:333,
Thread-0.Heap_initial_hashTable.shrinkEmpty(java.util.ArrayDeque,int)total:247    self:247    execsCount:502038    ns/exec:491,
Thread-1.HeapTest.getBytes(int,byte[])     total:42106  self:42106  execsCount:1782721   ns/exec:23618,
Thread-1.HeapTest.setBytes(int,byte[])     total:175    self:175    execsCount:1782721   ns/exec:98,
Thread-1.Heap_initial_hashTable.compact()  total:53355  self:7734   execsCount:11        ns/exec:703090909,
Thread-1.Heap_initial_hashTable.defrag()   total:1912   self:425    execsCount:11        ns/exec:38636363,
Thread-1.Heap_initial_hashTable.free(int)  total:1237   self:688    execsCount:444731    ns/exec:1547,
Thread-1.Heap_initial_hashTable.malloc(int)total:57553  self:1624   execsCount:888880    ns/exec:1827,
Thread-1.Heap_initial_hashTable.moveObject(.Heap_initial_hashTable$MBlock,int)total:45621  self:3340   execsCount:1782721   ns/exec:1873,
Thread-1.Heap_initial_hashTable.newEmpty(int,int)total:274    self:274    execsCount:444731    ns/exec:616,
Thread-1.Heap_initial_hashTable.newObject(int,int)total:244    self:244    execsCount:888880    ns/exec:274,
Thread-1.Heap_initial_hashTable.pollEmpty(java.util.ArrayDeque)total:137    self:137    execsCount:389019    ns/exec:352,
Thread-1.Heap_initial_hashTable.pollObject(int)total:275    self:275    execsCount:444731    ns/exec:618,
Thread-1.Heap_initial_hashTable.removeEmpty(.Heap_initial_hashTable$MBlock)total:787    self:787    execsCount:29671     ns/exec:26524,
Thread-1.Heap_initial_hashTable.resizeEmpty(.Heap_initial_hashTable$MBlock,int)total:700    self:700    execsCount:29671     ns/exec:23592,
Thread-1.Heap_initial_hashTable.shrinkEmpty(java.util.ArrayDeque,int)total:281    self:281    execsCount:499861    ns/exec:562,
Thread-2.Heap_initial_hashTable.free(int)  total:1227   self:763    execsCount:448068    ns/exec:1702,
Thread-2.Heap_initial_hashTable.malloc(int)total:2272   self:1582   execsCount:895603    ns/exec:1766,
Thread-2.Heap_initial_hashTable.newEmpty(int,int)total:224    self:224    execsCount:448068    ns/exec:499,
Thread-2.Heap_initial_hashTable.newObject(int,int)total:258    self:258    execsCount:895603    ns/exec:288,
Thread-2.Heap_initial_hashTable.pollEmpty(java.util.ArrayDeque)total:155    self:155    execsCount:391409    ns/exec:396,
Thread-2.Heap_initial_hashTable.pollObject(int)total:240    self:240    execsCount:448068    ns/exec:535,
Thread-2.Heap_initial_hashTable.shrinkEmpty(java.util.ArrayDeque,int)total:277    self:277    execsCount:504194    ns/exec:549,
Thread-3.Heap_initial_hashTable.free(int)  total:1172   self:688    execsCount:443639    ns/exec:1550,
Thread-3.Heap_initial_hashTable.malloc(int)total:2396   self:1585   execsCount:888269    ns/exec:1784,
Thread-3.Heap_initial_hashTable.newEmpty(int,int)total:323    self:323    execsCount:443639    ns/exec:728,
Thread-3.Heap_initial_hashTable.newObject(int,int)total:308    self:308    execsCount:888269    ns/exec:346,
Thread-3.Heap_initial_hashTable.pollEmpty(java.util.ArrayDeque)total:134    self:134    execsCount:388146    ns/exec:345,
Thread-3.Heap_initial_hashTable.pollObject(int)total:161    self:161    execsCount:443639    ns/exec:362,
Thread-3.Heap_initial_hashTable.shrinkEmpty(java.util.ArrayDeque,int)total:369    self:369    execsCount:500123    ns/exec:737,
main.HeapTest.<clinit>()                   total:1      self:1      execsCount:1         ns/exec:1000000,
main.HeapTest.main(java.lang.String[])     total:74617  self:4768   execsCount:1         ns/exec:4768000000,
main.Heap_initial_hashTable(int)           total:771    self:771    execsCount:1         ns/exec:771000000]
*/

/*
free memory: 0
malloc time: 9386 free time: 2568
total time: 11954 execsCount: 2664365
passed time: 4418
*/
/*
free memory: 0
malloc time: 10549 free time: 2736
total time: 13285 execsCount: 2654641
passed time: 5548

free memory: 0
malloc time: 9820 free time: 3259
total time: 13079 execsCount: 2658458
passed time: 5011

free memory: 0
malloc time: 9703 free time: 2764
total time: 12467 execsCount: 2656161
passed time: 4821

free memory: 0
malloc time: 9372 free time: 3097
total time: 12469 execsCount: 2648633
passed time: 4445

free memory: 0
malloc time: 9900 free time: 2390
total time: 12290 execsCount: 2666973
passed time: 4814
*/