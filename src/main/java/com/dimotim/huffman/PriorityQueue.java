package com.dimotim.huffman;

public final class PriorityQueue<T extends Comparable<T>>{
    private final T[] storage;
    private int size=0;
    public interface Constructor<T>{
        T[] create(int n);
    }

    public PriorityQueue(int capacity, Constructor<T> constructor){
        storage=constructor.create(capacity);
    }

    private void swap(int i, int j){
        //System.out.println("replace: "+i+" "+j);
        T tmp=storage[i];
        storage[i]=storage[j];
        storage[j]=tmp;
    }

    private void siftDown(int i){
        if(left(i)>=size)return;
        if(right(i)>=size){
            if(storage[i].compareTo(storage[left(i)])>=0) {
                swap(i,left(i));
                siftDown(left(i));
            }
            return;
        }
        if(storage[i].compareTo(storage[left(i)])<0){
            if(storage[i].compareTo(storage[right(i)])>=0) {
                swap(i,right(i));
                siftDown(right(i));
            }
        }
        else {
            if(storage[i].compareTo(storage[right(i)])>=0 && storage[right(i)].compareTo(storage[left(i)])<=0) {
                swap(i, right(i));
                siftDown(right(i));
            }
            else {
                swap(i,left(i));
                siftDown(left(i));
            }
        }
    }

    private void siftUp(int i){
        if(i==0)return;
        if(storage[i].compareTo(storage[parent(i)])<0){
            swap(i,parent(i));
            siftUp(parent(i));
        }
    }

    public void push(T elem){
        storage[size]=elem;
        siftUp(size);
        //debugPrint();
        size++;
    }

    private void debugPrint(){
        for (int i=0;i<size+1;i++)System.out.println(storage[i].toString());
        System.out.println();
    }

    private static int parent(int i){
        return (i+1)/2-1;
    }

    private static int left(int i){
        return (i+1)*2-1;
    }

    private static int right(int i){
        return (i+1)*2;
    }

    public T pop(){
        T ret=storage[0];
        size--;
        storage[0]=null;
        swap(0,size);
        siftDown(0);
        return ret;
    }

    public int size(){
        return size;
    }
}
