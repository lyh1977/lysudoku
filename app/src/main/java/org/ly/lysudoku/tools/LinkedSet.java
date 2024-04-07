package org.ly.lysudoku.tools;


import java.util.AbstractSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reflective set implementation. Like a <code>LinkedHashSet</code>
 * but with a weird method:
 * {@link #get(Object)}, which returns the element
 * of the set that is equal to the given element.
 * This is especially usefull when the implementation
 * of <code>equals</code> does not compare all fields.
 */
public class LinkedSet<T> extends AbstractSet<T> {

    /*
     * This implementation uses the wrapper pattern on the following map:
     */
    private final LinkedHashMap<T,T> target = new LinkedHashMap<T,T>();


    @Override
    public boolean add(T o) {
        return (target.put(o, o) != null);
    }

    @Override
    public void clear() {
        target.clear();
    }

    @Override
    public boolean contains(Object o) {
        return target.containsKey(o);
    }

    public T get(T o) {
        return target.get(o);
    }

    @Override
    public Iterator<T> iterator() {
        return target.keySet().iterator();
    }

    @Override
    public boolean remove(Object o) {
        return (target.remove(o) != null);
    }

    @Override
    public int size() {
        return target.size();
    }

    @Override
    public int hashCode() {
        int ret = 0;
        for(Map.Entry<T,T> e : target.entrySet()) {
            ret ^= e.getKey().hashCode();
        }
        return ret;
    }
}
