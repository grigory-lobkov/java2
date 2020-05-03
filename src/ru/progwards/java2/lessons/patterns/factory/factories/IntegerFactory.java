package ru.progwards.java2.lessons.patterns.factory.factories;

import ru.progwards.java2.lessons.patterns.factory.Number;
import ru.progwards.java2.lessons.patterns.factory.abs.ByteIntegerNumber;
import ru.progwards.java2.lessons.patterns.factory.abs.IntIntegerNumber;
import ru.progwards.java2.lessons.patterns.factory.abs.ShortIntegerNumber;

public enum IntegerFactory implements NumberFactory {
    INSTANCE;

    @Override
    public Number createNumber(int... nums) {

        if (nums.length == 1) {
            int num = nums[0];

            if (num <= ByteIntegerNumber.MAX_VALUE && num >= ByteIntegerNumber.MIN_VALUE)
                return new ByteIntegerNumber((byte)num);
            else if (num <= ShortIntegerNumber.MAX_VALUE && num >= ShortIntegerNumber.MIN_VALUE)
                return new ShortIntegerNumber((short)num);
            else
                return new IntIntegerNumber(num);
        }

        return null;
    }

}