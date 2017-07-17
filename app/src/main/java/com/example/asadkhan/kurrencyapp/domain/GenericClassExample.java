package com.example.asadkhan.kurrencyapp.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Kreated by asadkhan on 03 | June |  2017 | at 8:13 AM.
 */
public class GenericClassExample {

    public void betterForCasting() {

        List<String> list = new ArrayList<>();
        list.add("Hahahah");
        list.add("lolololool");

        String s = list.get(0);
    }

    public void BoxTest(){
        List<Boxx<String>> boxes = new ArrayList<>();
        Boxx<String> a = new Boxx<>();
        a.setObj("1");
        Boxx<String> b = new Boxx<>();
        b.setObj("2");

        boxes.add(a);
        boxes.add(b);

        boxes.get(0).inspectoo(123);
    }

    public void PairTest(){
        OrderPair<String, String> pairwa = new OrderPair<>("Kakakak", "jsdnjsdhs");
        Pair<String, String> pairwa2 = new OrderPair<>("Kakakak", "jsdnjsdhs");
    }

    public void ReactTest(){
        List<Boxx<String>> vars = Collections.synchronizedList(new ArrayList<Boxx<String>>());
        Boxx<String> one = new Boxx("akalala");
        Boxx<String> teo = new Boxx("sdfgfgfg");
        Boxx<String> trre = new Boxx("54345678");
        vars.add(one);
        vars.add(teo);
        vars.add(trre);

        vars.stream().map(x -> x.getObj()+" is the word").forEach(System.out::println);
        vars.stream().filter(stringBoxx -> stringBoxx.getObj().equals("akalala")).forEach(stringBoxx -> System.out.print(stringBoxx.getObj()));
    }

}

class Boxx<T> {

    private T obj;

    public Boxx() { }

    public Boxx(T obj) {
        this.obj = obj;
    }

    public T getObj() {
        return obj;
    }

    public void setObj(T obj) {
        this.obj = obj;
    }

    public <U extends Number> void inspectoo(U u){
        System.out.println("T " + obj.getClass().getName() + " >> " + obj);
        System.out.println("U " + u.getClass().getName() + " >> " + u);
    }
}

interface Pair<K, V> {
    public V getValue();
    public K getKey();
}

class OrderPair<K, V> implements Pair{

    private K key;
    private V valoo;

    public OrderPair(K k, V v){
        this.key = k;
        this.valoo = v;
    }

    @Override
    public Object getValue() {
        return valoo;
    }

    @Override
    public Object getKey() {
        return key;
    }
}
//Task-list
/*
*
* */

///usr/local/bin:/usr/bin:/bin:/usr/local/games:/usr/games:/usr/lib/jvm/java-8-oracle/bin:/usr/lib/jvm/java-8-oracle/db/bin:/usr/lib/jvm/java-8-oracle/jre/bin:/usr/share/rvm/bin/usr/local/bin:/usr/bin:/bin:/usr/local/games:/usr/games:/usr/lib/jvm/java-8-oracle/bin:/usr/lib/jvm/java-8-oracle/db/bin:/usr/lib/jvm/java-8-oracle/jre/bin:/usr/share/rvm/bin
///home/asadkhan/.rbenv/shims:/home/asadkhan/.rbenv/bin:/home/asadkhan/.linuxbrew/sbin:/home/asadkhan/.rbenv/shims:/home/asadkhan/.rbenv/bin:/home/asadkhan/.linuxbrew/bin:/home/asadkhan/bin:/usr/local/bin:/home/asadkhan/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/games:/usr/local/games:/usr/lib/jvm/java-8-oracle/bin:/usr/lib/jvm/java-8-oracle/db/bin:/usr/lib/jvm/java-8-oracle/jre/bin:/usr/share/rvm/bin:/usr/lib/jvm/java-8-oracle/bin:/home/asadkhan/Android/Sdk/tools:/home/asadkhan/Android/Sdk/platform-tools:/home/asadkhan/android-studio/bin/