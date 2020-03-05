package ru.progwards.java2.lessons.ram;

public class Test {
	public static void main(String[] args) {
		Calculator c = new Calculator();
		System.out.println(c.sum(10,15));
	}
}

/*
C:\Users\Grigory\IdeaProjects\java2>"C:\Program Files\Java\jdk1.7.0_80\bin\javac" -sourcepath src -d out src/ru/progwards/java2/lessons/ram/Test.java

C:\Users\Grigory\IdeaProjects\java2>java -classpath .\out ru.progwards.java2.lessons.ram.Test
25
*/