package ru.progwards.java2.lessons.recursion;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

class Goods { //товар
    String name; // наименование
    String number; // артикул
    int available; // количество на складе
    double price; // цена
    Instant expired; // срок годности

    @Override
    public String toString() {
        return "\nGoods{" +
                "name='" + name + '\'' +
                ", number='" + number + '\'' +
                ", available=" + available +
                ", price=" + price +
                ", expired=" + expired +
                '}';
    }
}

// Задача на использование лямбда

public class GoodsWithLambda {

    private List<Goods> list; // список товаров

    private final Function<Goods, String> sortNameComparator = a -> a.name;
    private final Function<Goods, String> sortNumberComparator = a -> a.number.toUpperCase();
    private final Function<Goods, String> sortPartNumberComparator = a -> a.number.substring(0, 3);
    private final Function<Goods, Integer> sortAvailableComparator = a -> a.available;

    GoodsWithLambda() {
    }

    GoodsWithLambda(List<Goods> list) {
        this();
        setGoods(list);
    }

    void setGoods(List<Goods> list) {
        this.list = list;
    }

    // вернуть список, отсортированный по наименованию
    List<Goods> sortByName() {
        return sortByStringComparatorFunc(sortNameComparator);
    }

    // вернуть список, отсортированный по артикулу, без учета регистра
    List<Goods> sortByNumber() {
        return sortByStringComparatorFunc(sortNumberComparator);
    }

    // вернуть список, отсортированный по первым 3-м символам артикула, без учета регистра
    List<Goods> sortByPartNumber() {
        return sortByStringComparatorFunc(sortPartNumberComparator);
    }

    // вернуть список, отсортированный по количеству, а для одинакового количества, по артикулу, без учета регистра
    List<Goods> sortByAvailabilityAndNumber() {
        return sortByComparator(Comparator
                .comparing(sortAvailableComparator)
                .thenComparing(sortNumberComparator));
    }

    private List<Goods> sortByStringComparatorFunc(Function<Goods, String> comparator) {
        return sortByComparator(Comparator.comparing(comparator));
    }

    private List<Goods> sortByComparator(Comparator<Goods> comparator) {
        return list.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    // вернуть список, с товаром, который будет просрочен после указанной даты, отсортированный по дате годности
    List<Goods> expiredAfter(Instant date) {
        return list.stream()
                .sorted(Comparator.comparing(a -> a.expired))
                .takeWhile(a -> a.expired.compareTo(date) <= 0)
                .collect(Collectors.toList());
    }

    // вернуть список, с товаром, количество на складе которого меньше указанного
    List<Goods> countLess(int count) {
        return list.stream()
                .sorted(Comparator.comparingInt(a -> a.available))
                .takeWhile(a -> a.available < count)
                .collect(Collectors.toList());
    }

    // вернуть список, с товаром, количество на складе которого больше count1 и меньше count2
    List<Goods> countBetween(int count1, int count2) {
        //return list.stream().sorted(Comparator.comparingInt(a->a.available)).takeWhile(a -> a.available < count2).dropWhile(a -> a.available < count1).collect(Collectors.toList());
        return list.stream()
                .sorted(Comparator.comparingInt(a -> a.available))
                .filter(a -> count1 < a.available && a.available < count2)
                .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        Goods good1 = new Goods();
        good1.available = 31;
        good1.name = "Банан";
        good1.expired = Instant.now().minusMillis(1000);
        good1.price = 56;
        good1.number = "BANANA";
        Goods good2 = new Goods();
        good2.available = 6;
        good2.name = "Ананас";
        good2.expired = Instant.now().plusMillis(1000);
        good2.price = 56;
        good2.number = "PINEAPPLE";

        GoodsWithLambda l = new GoodsWithLambda(List.of(good1, good2));

        System.out.println("sortByAvailabilityAndNumber = "+l.sortByAvailabilityAndNumber());
        System.out.println("sortByName = "+l.sortByName());
        System.out.println("sortByNumber = "+l.sortByNumber());
        System.out.println("sortByPartNumber = "+l.sortByPartNumber());
        System.out.println("expiredAfter = "+l.expiredAfter(Instant.now()));
        System.out.println("countLess = "+l.countLess(10));
        System.out.println("countBetween = "+l.countBetween(10, 100));
    }

}