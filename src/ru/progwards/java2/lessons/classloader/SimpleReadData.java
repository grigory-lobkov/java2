package ru.progwards.java2.lessons.classloader;

public class SimpleReadData {

    final String name;

    public SimpleReadData() {
        //try { Thread.sleep(5); } catch (InterruptedException e) { e.printStackTrace(); }
        name = "SRD"+String.valueOf(System.currentTimeMillis());
    }

    public String toString() {
        //try { Thread.sleep(2); } catch (InterruptedException e) { e.printStackTrace(); }
        return name;
    }

    public static void main(String[] args) {
        //try { Thread.sleep(27); } catch (InterruptedException e) { e.printStackTrace(); }
        System.out.println(new SimpleReadData());
    }

}
