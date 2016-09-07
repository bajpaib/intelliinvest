package com.intelliinvest.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class SubListIterator implements Iterator<Collection<?>>, Iterable<Collection<?>> {

    private final List<?> whole;
    private final int elementsEachPart;
    private int fromIndex;
    private int toIndex;

	public static SubListIterator create(Collection<?> whole, int itemsEach){
    	return new SubListIterator(whole, itemsEach);
    }
    
	@SuppressWarnings("unchecked")
    private SubListIterator(Collection<?> whole, int itemsEach) {
        this.whole = new ArrayList(whole);
        this.elementsEachPart = itemsEach;
        this.fromIndex = 0;
        this.toIndex = Math.min(elementsEachPart, whole.size());
    }

    public boolean hasNext() {
        return fromIndex < toIndex;
    }

    public Collection<?> next() {
    	Collection<?> nextSubList = whole.subList(fromIndex, toIndex);
        fromIndex = toIndex;
        toIndex = Math.min(toIndex + elementsEachPart, whole.size());
        return nextSubList;
    }

    public void remove() {
        throw new UnsupportedOperationException("This method is not supported");
    }

    public Iterator<Collection<?>> iterator() {
        return this;
    }
}