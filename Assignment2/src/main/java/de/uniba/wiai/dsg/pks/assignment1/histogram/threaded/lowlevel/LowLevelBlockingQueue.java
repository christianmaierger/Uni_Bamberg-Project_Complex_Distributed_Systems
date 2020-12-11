package de.uniba.wiai.dsg.pks.assignment1.histogram.threaded.lowlevel;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.NotThreadSafe;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;


/**
 * Low level implementation of a BlockingQueue with only the methods needed for Assignment 1.
 * It is not functional for any methods other than put(E entry) and take().
 * @param <E> Class to be held in the BlockingQueue
 */
@NotThreadSafe
public class LowLevelBlockingQueue<E> implements BlockingQueue<E> {
    @GuardedBy(value = "lock")
    private final List<E> entries;

    private final Object lock = new Object();

    public LowLevelBlockingQueue(){
        this.entries = new ArrayList<>();
    }

    @Override
    public void put(E entry) {
        if(!Objects.nonNull(entry)){
            throw new NullPointerException("Entry is null and cannot be added to LowLevelBlockingQueue.");
        }
        synchronized (lock){
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
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean offer(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E poll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E element() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E peek() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E poll(long timeout, TimeUnit unit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int remainingCapacity() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<E> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int drainTo(Collection<? super E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int drainTo(Collection<? super E> c, int maxElements) {
        throw new UnsupportedOperationException();
    }
}
