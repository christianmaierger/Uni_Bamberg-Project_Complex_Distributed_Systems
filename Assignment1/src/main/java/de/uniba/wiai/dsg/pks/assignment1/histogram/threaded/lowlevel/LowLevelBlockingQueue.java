package de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.lowlevel;

import net.jcip.annotations.NotThreadSafe;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

//FIXME: methoden implementierung?

/**
 * Low level implementation of a BlockingQueue with only the methods needed for Assignment 1.
 * It is threadsafe if only put() and take() are used, and not functional or threadsafe at all for other methods.
 * @param <E> Class to be held in the BlockingQueue
 */
@NotThreadSafe
public class LowLevelBlockingQueue<E> implements BlockingQueue<E> {
    private final int capacity;
    private final List<E> entries;
    private final Object lock = new Object();

    public LowLevelBlockingQueue(int capacity){
        this.capacity = capacity;
        this.entries = new ArrayList<>();
    }

    @Override
    public void put(E entry) throws InterruptedException {
        if(!Objects.nonNull(entry)){
            throw new NullPointerException("Entry is null and cannot be added to LowLevelBlockingQueue.");
        }
        synchronized (lock){
            while(entries.size() >= capacity){
                lock.wait();
            }
            entries.add(entry);
            lock.notifyAll();
        }
    }

    @Override
    public E take() throws InterruptedException {
        E entryToReturn;
        synchronized (lock){
            while(entries.size() == 0){
                lock.wait();
            }
            entryToReturn = entries.remove(0);
        }
        return entryToReturn;
    }

    @Override
    public boolean add(E e) {
        return false;
    }

    @Override
    public boolean offer(E e) {
        return false;
    }

    @Override
    public E remove() {
        return null;
    }

    @Override
    public E poll() {
        return null;
    }

    @Override
    public E element() {
        return null;
    }

    @Override
    public E peek() {
        return null;
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }



    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        return null;
    }

    @Override
    public int remainingCapacity() {
        return 0;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {

    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public Iterator<E> iterator() {
        return null;
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return null;
    }

    @Override
    public int drainTo(Collection<? super E> c) {
        return 0;
    }

    @Override
    public int drainTo(Collection<? super E> c, int maxElements) {
        return 0;
    }
}
