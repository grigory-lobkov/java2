package ru.progwards.java2.lessons.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;

public class GettersAndSetters {
/*
Реализовать метод public static check(String) который анализирует, для каких private полей класса
нет сеттера или геттера и выводит на консоль сигнатуры отсутствующих методов;

Например, описан класс
class Person {
    private String name;
    private int age;
    private boolean sex;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
     public boolean getSex() {
         return sex;
     }
}

тут у свойства name есть геттер и сеттер.
Геттер это public не static метод getИмяСвойства без параметров и с типом, совпадающим с типом свойства.
Сеттер это public не static void метод setИмяСвойства c одним параметром с типом, совпадающим с типом свойства.

Для приведенного примера на консоль должно быть выдано

public boolean setSex(boolean sex)
public int getAge()
public void setAge(int age)
*/

    public class Property {
        Field field;
        String name;
        Class type;
        String getterName;
        String setterName;
        boolean getterFound;
        boolean setterFound;

        @Override
        public String toString() {
            return "Property{" +
                    "name='" + name + '\'' +
                    ", type=" + type.getSimpleName() +
                    ", getterName='" + getterName + '\'' +
                    ", setterName='" + setterName + '\'' +
                    ", getterFound=" + getterFound +
                    ", setterFound=" + setterFound +
                    "}\n";
        }
    }

    public class Properties {
        List<Property> list = new ArrayList<Property>();
        Map<String, Property> table = new Hashtable<String, Property>();

        public void add(Field f) {
            Property p = new Property();
            list.add(p);
            p.field = f;
            p.name = f.getName();
            p.type = f.getType();
            p.getterName = GettersAndSetters.this.getGetterName(f);
            p.setterName = GettersAndSetters.this.getSetterName(f);
            table.put(p.getterName, p);
            table.put(p.setterName, p);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (Property p : list) {
                sb.append(p.toString());
            }
            return sb.toString();
        }

        public void check(Method m) {
            String name = m.getName();
            Property p = table.get(name);
            if (p != null) {
                if (!p.getterFound && p.getterName.compareTo(name) == 0) checkGetter(p, m);
                if (!p.setterFound && p.setterName.compareTo(name) == 0) checkSetter(p, m);
            }
            /*System.out.println(method.getReturnType().getSimpleName() +" "+ method.getName() + "("
                    + getArguments(method.getParameters()) + ") {}");*/
        }

        // проверить, что метод - подходящий геттер (кроме имени) для свойства
        private void checkGetter(Property p, Method m) {
            Parameter[] parameters = m.getParameters();
            if (m.getReturnType().equals(p.type) // возвращаемый класс должен совпадать
                    && parameters.length == 0 // параметров быть не должно
            ) {
                p.getterFound = true;
            }
        }

        // проверить, что метод - подходящий сеттер (кроме имени) для свойства
        private void checkSetter(Property p, Method m) {
            Parameter[] parameters = m.getParameters();
            if (parameters.length != 1) return; // должен быть один параметр
            Parameter parameter = parameters[0];
            if (parameter.getType().equals(p.type) // класс параметра должен совпадать с классом свойства
                    && m.getReturnType().equals(Void.TYPE) // метод должен возвращать void
            ) {
                p.setterFound = true;
            }
        }

        public void printWithoutGAS() {
            for (Property p : list) {
                if(!p.getterFound) {
                    System.out.println(GettersAndSetters.this.getGetterFunc(p));
                }
                if(!p.setterFound) {
                    System.out.println(GettersAndSetters.this.getSetterFunc(p));
                }
            }
        }
    }

    Properties properties;

    GettersAndSetters(Class clazz) {
        properties = new Properties();
        fillProperties(clazz);
        //System.out.println(properties);
        checkMethods(clazz);
        //System.out.println(properties);
    }

    // наполняем класс properties нужными свойствами
    private void fillProperties(Class clazz) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if(checkNeedGAS(field)) properties.add(field);
        }
    }
    // проверить все методы класса
    public void checkMethods(Class clazz) {
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if(checkMethodCanGAS(method)) properties.check(method);
            /*System.out.println(getModifiers(method.getModifiers(), false)
                    + method.getReturnType().getSimpleName() +" "+ method.getName() + "("
                    + getArguments(method.getParameters()) + ") {}");*/
        }
    }

    // проверить, что метод удовлетворяет базовым ограничениям
    private static boolean checkMethodCanGAS(Method m) {
        int mods = m.getModifiers();
        return Modifier.isPublic(mods) && !Modifier.isStatic(mods);
    }

    // проверка свойства, нужны ли ему методы установки
    private boolean checkNeedGAS(Field f) {
        return Modifier.isPrivate(f.getModifiers());
    }

    // генерация имени геттера
    private String getGetterName(Field f) {
        String name = f.getName();
        return "get"+name.substring(0,1).toUpperCase()+name.substring(1);
    }

    // генерация имени сеттера
    private String getSetterName(Field f) {
        String name = f.getName();
        return "set"+name.substring(0,1).toUpperCase()+name.substring(1);
    }

    // генерация сигнатуры геттера
    private String getGetterFunc(Property p) {
        return "public "+p.type.getSimpleName()+" "+p.getterName+"()";
    }

    // генерация сигнатуры сеттера
    private String getSetterFunc(Property p) {
        return "public void "+p.setterName+"("+p.type.getSimpleName()+" "+p.name+")";
    }

    private void printPropertiesWithoutGAS() {
        properties.printWithoutGAS();
    }

    public static void check(String clazzName) throws ClassNotFoundException {
        GettersAndSetters gas = new GettersAndSetters(Class.forName(clazzName));
        gas.printPropertiesWithoutGAS();
    }

    public static void main(String[] args) throws Exception {
        check("ru.progwards.java2.lessons.reflection.Person");
    }
}
