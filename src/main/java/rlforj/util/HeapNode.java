/*
 * Copyright (c) 2017, Fabio Ticconi, fabio.ticconi@gmail.com
 * Copyright (c) 2013, kba
 * All rights reserved.
 */

package rlforj.util;

/**
 * A heapnode interface that will allow the index to be stored and retrieved
 * from the object directly.
 * <p>
 * This is for maximum efficiency, so that when the heap is notified that a
 * object has changed, instead of searching for the object in O(N) time, it
 * will directly jump to the object and put it in the right place in O(log n)
 * time.
 * <p>
 * Implementing class SHOULD NOT modify the int index. Ideally it should be a
 * SimpleHeapContext instead of int but I am saving an unnecessary new() call.
 * <p>
 * Downside: An object can be stored in only one heap. Hopefully this is the
 * typical case.
 *
 * @author sidatta
 */
public interface HeapNode extends Comparable
{
    int getHeapIndex();

    void setHeapIndex(int heapIndex);
}
