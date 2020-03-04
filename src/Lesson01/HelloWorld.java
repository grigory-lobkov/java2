package Lesson01;

public class HelloWorld {
	public static void main(String[] args) {
		System.out.println("Hello, Java geek! This is console JAVA HelloWrorld.");
	}
}

/*
C:\Users\Grigory\IdeaProjects\java2>"C:\Program Files\Java\jdk1.7.0_80\bin\javac" -d out src/Lesson01/HelloWorld.java

C:\Users\Grigory\IdeaProjects\java2>java -classpath .\out Lesson01.HelloWorld
*/

/*
C:\Users\Grigory\IdeaProjects\java2>"C:\Program Files\Java\jdk1.7.0_80\bin\javac" -d out src/Lesson01/HelloWorld.java

C:\Users\Grigory\IdeaProjects\java2>"C:\Program Files\Java\jdk1.7.0_80\bin\jar" cfe hello.jar HelloWorld out/Lesson01/HelloWorld.class

C:\Users\Grigory\IdeaProjects\java2>"C:\Program Files\Java\jdk1.7.0_80\bin\java" -jar hello.jar
Error: Could not find or load main class HelloWorld              -- ??
*/