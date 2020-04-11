package ru.progwards.java2.lessons.classloader;

import javassist.*;

import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Date;
import java.util.HashSet;

/*
java-агент - профилировщик, замеряющий время работы всех методов
*/

/*
Артифакт билдится при правках сам, если он добавлен в проекте:
Ctrl+Alt+Shift+S
Artifacts
Создаем джава-агент по трем файлам: SystemProfiler, Interceptor, Profiler

Запускаем что-нибудь и прописываем путь в параметрах Run - Edit Configurations - VM Options:
-javaagent:"C:\Users\Grigory\IdeaProjects\java2\out\artifacts\Artifact1\Artifact1.jar"=SimpleReadData

где SimpleReadData - это класс, который надо проинспектировать.
*/

/*
Download Javassist jar: https://github.com/jboss-javassist/javassist/releases
Install as External library
*/

/*
Создать java-агент - профилировщик, основанный на классе Profiler из базового курса.
При решении задачи использовать библиотеку javassist (начиная с JDK 9 требуется версия 3.21.0-GA или старше).
Задача агента - пропатчить программу:

1) добавить из агента необходимые классы профайлера в программу, над которой работает агент;

2) пропатчить каждый метод, вставляя перед ним вызов enterSection(<полное имя метода, включая класс и пакет>),
и после него exitSection(<полное имя метода, включая класс и пакет>);

3) реализовать метод, printStatisticInfo(String fileName) который выводит результат профилировки в файл с именем fileName;

4) добавить в конец метода main программы вызов метода printStatisticInfo(<Имя главного класса программы> + ".stat");

5) имена классов для профилировки className взять из параметра агента (-javaagent:<имя агента.jar>=<параметр>),
при этом 0-ой класс является главным классом программы (содержит метод main).

Имена классов, когда их более одного разделяются ";"



Подсказка по работе Javassist (агент добавляет в метод main программы вычисление времени выполнения метода):

ClassPool classPool = ClassPool.getDefault();

CtClass ctClass = classPool.get(className.replace('/', '.'));

CtMethod ctMethod = ctClass.getDeclaredMethod("main"); // имя метода

ctMethod.addLocalVariable("start", CtClass.longType);

ctMethod.insertBefore("start = System.currentTimeMillis();");

ctMethod.insertAfter("System.out.println(\"время выполнения\" + (System.currentTimeMillis() - start));");
*/

public class SystemProfiler implements ClassFileTransformer {
    final static String ROOT_PKG = "ru.progwards.java2.lessons.classloader".replace(".", "/");

    private static final ClassPool classPool = ClassPool.getDefault();
    private static final HashSet<String> inspectedClasses = new HashSet<String>();
    private static final String currentPkg = SystemProfiler.class.getPackageName();

    public SystemProfiler(String agentArgument) {
        if(agentArgument!=null) {
            String[] strParts = agentArgument.split(";");
            for (String s : strParts)
                inspectedClasses.add(s);
        }
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (className.startsWith(ROOT_PKG)) {
            String shortName = className.substring(className.lastIndexOf("/") + 1);
            if (inspectedClasses.contains(shortName))
                try {
                    String dottedClassName = className.replace('/', '.');
                    return adjustClass(dottedClassName, classBeingRedefined,
                            classfileBuffer);
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
        return null;
    }

    private byte[] adjustClass(final String className,
                               final Class<?> classBeingRedefined, final byte[] classfileBuffer)
            throws IOException, RuntimeException, CannotCompileException {

        //System.out.println("SystemProfiler.adjustClass(" + className+')');

        CtClass clazz = null;

        try {
            clazz = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
            if (!clazz.isInterface()) {
                CtBehavior[] methods = clazz.getDeclaredBehaviors();
                for (CtBehavior method : methods) {
                    if (!method.isEmpty() && !Modifier.isNative(method.getModifiers())) {
                        try {
                            String secName = method.getLongName();
                            method.insertBefore(String.format(currentPkg+".Profiler.enterSection(\"%s\");",secName));
                            method.insertAfter(String.format(currentPkg+".Profiler.exitSection(\"%s\");",secName));
                            if(method.getName().compareTo("main")==0) {
                                method.insertAfter(String.format(currentPkg+".SystemProfiler.printStatisticInfo(\"%s.stat\");",clazz.getSimpleName()));
                            }
                        } catch (Throwable t) {
                            System.out.println("Error instrumenting " + className + "."
                                    + method.getName());
                            t.printStackTrace();
                        }
                    }
                }
                return clazz.toBytecode();
            }
        } finally {
            if (clazz != null) {
                clazz.detach();
            }
        }
        return classfileBuffer;
    }

    static void printStatisticInfo(String fileName) {
        //System.out.println("File: "+fileName+"\n"+Profiler.getSectionsInfo());
        try (FileWriter fileWriter = new FileWriter(fileName, true)) {
            fileWriter.write(new Date().toString()+Profiler.getSectionsInfo()+"\n\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
