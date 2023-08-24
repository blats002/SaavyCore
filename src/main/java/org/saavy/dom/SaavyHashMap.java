/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.saavy.dom;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.WeakHashMap;

/**
 *
 * @author rgsaavedra
 */
public class SaavyHashMap<K, V> extends WeakHashMap<K, V> {

    private LinkedList<HelperReference> hardReference = new LinkedList<HelperReference>();

    @Override
    public V remove(Object key) {
        HelperReference href = new HelperReference(this, key);
        if (hardReference.contains(href)) {
            int index = hardReference.indexOf(href);
            hardReference.remove(index).clear();
        }
        return super.remove(key);
    }

    @Override
    public V put(K key, V value) {
        HelperReference href = new HelperReference(this, key);
        if (hardReference.contains(href)) {
            int index = hardReference.indexOf(href);
            href.clear();
            href = hardReference.get(index);
        }else{
            hardReference.add(href);
        }
        return super.put((K) href.get(), value);
    }

    @Override
    public void clear() {
        super.clear();
        for (HelperReference hRef : hardReference) {
            hRef.clear();
        }
        hardReference.clear();
    }

    @Override
    public V get(Object key) {
        HelperReference href = new HelperReference(this, key);
        if (hardReference.contains(href)) {
            int index = hardReference.indexOf(href);
            return super.get(hardReference.get(index).get());
        }
        return super.get(key);
    }

    @Override
    public boolean containsKey(Object key) {
        HelperReference href = new HelperReference(this, key);
//        if (hardReference.contains(href)) {
//            int index = hardReference.indexOf(href);
//            return super.containsKey(hardReference.get(index).get());
//        }
        return hardReference.contains(href);
    }

    @Override
    protected void finalize() throws Throwable {
        if(hardReference != null){
            clear();
            hardReference = null;
        }
        super.finalize();
    }



    public static class HelperReference<T> extends WeakReference<T> {

        protected T strongRef;
        protected SaavyHashMap parent;

        public HelperReference(SaavyHashMap parent, T obj) {
            super(null);
            this.parent = parent;
            strongRef = obj;
        }

        @Override
        public T get() {
            if (parent != null && strongRef != null) {
                return strongRef;
            }
            return null;
        }

        @Override
        public void clear() {
            strongRef = null;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof HelperReference) {
                HelperReference hRef = (HelperReference) obj;
//                System.out.println("HP:"+(hRef.parent.equals(parent)));
//                System.out.println("HR:"+hRef.strongRef);
//                System.out.println("R:"+strongRef);
//                System.out.println("Here:"+(hRef.parent.equals(parent) && hRef.strongRef.equals(strongRef)));
                boolean referantEqual = hRef.strongRef == null && strongRef == null;
                if(!referantEqual){
                    referantEqual = hRef.strongRef.equals(strongRef);
                    if(strongRef instanceof String){
                        referantEqual = strongRef.toString().compareTo(hRef.strongRef.toString()) == 0;
                    }
                }
                return hRef.parent.equals(parent) && referantEqual;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 29 * hash + (this.strongRef != null ? this.strongRef.hashCode() : 0);
            hash = 29 * hash + (this.parent != null ? this.parent.hashCode() : 0);
            return hash;
        }
    }

//    private static double memoryUsed() {
//        long totalMem = Runtime.getRuntime().totalMemory();
//        long freeMem = Runtime.getRuntime().freeMemory();
//        return 100 - ((double) freeMem / (double) totalMem) * 100D;
//    }
//
//    private static void logMemory(final int tries) {
//        new Thread() {
//            @Override
//            public void run() {
//                for (int x = 0; x < tries; x++) {
//                    System.out.println("Memory:" + memoryUsed());
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException ex) {
//                        Logger.getLogger(SaavyHashMap.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
//                System.out.println("Memory:" + memoryUsed());
//            }
//        }.start();
//    }

//    public static void main(String a[]) {
//
//
//        System.gc();
//        double mem1 = memoryUsed();
//        int x = 0;
//
//
////        HashMap map1 = new HashMap();
////        System.out.println("Start Memory:" + memoryUsed());
////        while (map1.size() < 100) {
////            map1.put(x++, String.valueOf(x));
////        }
////
////        System.out.println("BC Memory:" + (memoryUsed()-mem1));
////        mem1 = memoryUsed();
////        map1.clear();
//////        map1 = null;
////
////        System.gc();
////        System.out.println("AC Memory:" + (memoryUsed()-mem1));
////        mem1 = memoryUsed();
////        System.gc();
////        System.out.println("AC Memory:" + (memoryUsed()-mem1));
////        mem1 = memoryUsed();
////        System.gc();
////        System.out.println("AC Memory:" + (memoryUsed()-mem1));
////        mem1 = memoryUsed();
////        System.gc();
////        System.out.println("AC Memory:" + (memoryUsed()-mem1));
////        mem1 = memoryUsed();
////        System.gc();
////        System.out.println("AC Memory:" + (memoryUsed()-mem1));
////
////        System.out.println("End Memory:" + memoryUsed());
//
//        SaavyHashMap map2 = new SaavyHashMap();
//        System.gc();
//        mem1 = memoryUsed();
//        System.out.println("Start Memory:" + memoryUsed());
////        x = 0;
////        while (map2.size() < 1) {
////            map2.put(x++, String.valueOf(x));
////        }
////        x = 0;
////        while (map2.size() < 1) {
////            map2.put(x++, String.valueOf(x));
////        }
//        map2.put(1, 1);
//        map2.put(1,2);
//        System.gc();
////        for(Object obj:map2.values()){
////            System.out.println("Object:"+obj);
////        }
////        System.out.println("Map:"+map2.size());
//
//        System.out.println("BC Memory:" + (memoryUsed()-mem1));
//        mem1 = memoryUsed();
//        map2.clear();
////        map2 = null;
//
//        System.gc();
//        System.out.println("AC Memory:" + (memoryUsed()-mem1));
//        mem1 = memoryUsed();
//        System.gc();
//        System.out.println("AC Memory:" + (memoryUsed()-mem1));
//        mem1 = memoryUsed();
//        System.gc();
//        System.out.println("AC Memory:" + (memoryUsed()-mem1));
//        mem1 = memoryUsed();
//        System.gc();
//        System.out.println("AC Memory:" + (memoryUsed()-mem1));
//        mem1 = memoryUsed();
//        System.gc();
//        System.out.println("AC Memory:" + (memoryUsed()-mem1));
//
//        System.out.println("End Memory:" + memoryUsed());
//    }
}
