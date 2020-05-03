package ru.progwards.java2.lessons.patterns.factory.factories;

import ru.progwards.java2.lessons.patterns.factory.Number;
import ru.progwards.java2.lessons.patterns.factory.complex.IntComplexNumber;

public enum ComplexFactory implements NumberFactory {
    INSTANCE;


    @Override
    public Number createNumber(int... nums) {

        if (nums.length == 2) {
            return (new IntComplexNumber(nums[0], nums[1]));
        }

        return null;
    }

}