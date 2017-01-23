package com.example.ymiyauchi.mylibrary.listener;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

/**
 * Created by  ymiyauchi on 2016/11/11.
 * <p>
 * 特に記載のない場合には、@NonNullアノテーションのついた引数にnullを渡すとNullPointerExceptionが投げられます
 */

public class ObservableArrayList<E> implements ObservableList<E> {
    private final List<OnListChangedListener<E>> listeners;
    private final List<E> list = new ArrayList<>();

    public ObservableArrayList() {
        listeners = new ArrayList<>();
    }

    @Override
    public void addEventListener(@NonNull OnListChangedListener<E> listener) {
        Objects.requireNonNull(listener);
        listeners.add(listener);
    }

    @Override
    public void removeEventListener(@NonNull OnListChangedListener<E> listener) {
        Objects.requireNonNull(listener);
        listeners.remove(listener);
    }

    @Override
    public boolean addAllWithLastEvent(@NonNull Collection<E> e) {
        boolean ret = list.addAll(e);
        for (OnListChangedListener<E> listener : listeners) {
            listener.onAdded(this, list.size() - 1);
        }
        return ret;
    }

    @Override
    public boolean addWithNonEvent(E e) {
        return list.add(e);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(@Nullable Object o) {
        return list.contains(o);
    }

    @NonNull
    @Override
    public Iterator<E> iterator() {
        return list.iterator();
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @NonNull
    @Override
    public <T> T[] toArray(@NonNull T[] ts) {
        return list.toArray(ts);
    }

    @Override
    public boolean add(E e) {
        for (OnListChangedListener<E> listener : listeners) {
            listener.onAdd(this, list.size(), e);
        }

        boolean result = list.add(e);

        for (OnListChangedListener<E> listener : listeners) {
            listener.onAdded(this, size() - 1);
        }
        return result;
    }

    @Override
    public boolean remove(@NonNull Object o) {
        Objects.requireNonNull(o);
        int index = list.indexOf(o);
        if (index < 0)
            return false;
        for (OnListChangedListener<E> listener : listeners) {
            listener.onRemove(this, index);
        }
        return list.remove(o);
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> collection) {
        return list.containsAll(collection);
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends E> collection) {
        Objects.requireNonNull(collection);
        int index = list.size();
        for (OnListChangedListener<E> listener : listeners) {
            listener.onAddAll(this, index, collection);
        }
        boolean ret = list.addAll(collection);
        for (OnListChangedListener<E> listener : listeners) {
            listener.onAddedAll(this, index, collection);
        }
        return ret;
    }

    @Override
    public boolean addAll(int i, @NonNull Collection<? extends E> collection) {
        Objects.requireNonNull(collection);
        for (OnListChangedListener<E> listener : listeners) {
            listener.onAddAll(this, i, collection);
        }
        boolean ret = list.addAll(i, collection);
        for (OnListChangedListener<E> listener : listeners) {
            listener.onAddedAll(this, i, collection);
        }
        return ret;
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> collection) {
        Objects.requireNonNull(collection);
        for (OnListChangedListener<E> listener : listeners) {
            listener.onRemoveAll(this, collection);
        }
        return list.removeAll(collection);
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> collection) {
        Objects.requireNonNull(collection);
        for (OnListChangedListener<E> listener : listeners) {
            listener.onRetainAll(this, collection);
        }
        return list.retainAll(collection);
    }

    @Override
    public void clear() {
        for (OnListChangedListener<E> listener : listeners) {
            listener.onClear(this);
        }
        list.clear();
    }

    @Override
    public E get(int i) {
        return list.get(i);
    }

    @Override
    public E set(int i, E e) {
        for (OnListChangedListener<E> listener : listeners) {
            listener.onSet(this, i, e);
        }
        return null;
    }

    @Override
    public void add(int i, E e) {
        for (OnListChangedListener<E> listener : listeners) {
            listener.onAdd(this, i, e);
        }

        list.add(i, e);

        for (OnListChangedListener<E> listener : listeners) {
            listener.onAdded(this, i);
        }
    }

    @Override
    public E remove(int i) {
        for (OnListChangedListener<E> listener : listeners) {
            listener.onRemove(this, i);
        }
        return list.remove(i);
    }

    @Override
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator() {
        return list.listIterator();
    }

    @NonNull
    @Override
    public ListIterator<E> listIterator(int i) {
        return list.listIterator(i);
    }

    @NonNull
    @Override
    public List<E> subList(int i, int i1) {
        return list.subList(i, i1);
    }
}
