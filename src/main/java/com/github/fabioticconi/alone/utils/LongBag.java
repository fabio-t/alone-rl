/*
 * Copyright (C) 2015-2017 Fabio Ticconi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.github.fabioticconi.alone.utils;

import java.util.Arrays;

import static java.lang.Math.max;

/**
 * Collection type a bit like ArrayList but does not preserve the order of its
 * entities, speedwise it is very good, especially suited for games.
 * <p>
 * Author: based on IntBag from artemis-odb
 * Date: 05/11/17
 */
public class LongBag
{
    /**
     * The number of values stored by this bag.
     */
    protected int    size = 0;
    /**
     * The backing array.
     */
    private   long[] data;

    /**
     * Constructs an empty Bag with an initial capacity of 64.
     */
    public LongBag()
    {
        this(64);
    }

    /**
     * Constructs an empty Bag with the specified initial capacity.
     *
     * @param capacity the initial capacity of Bag
     */
    public LongBag(final int capacity)
    {
        data = new long[capacity];
    }

    /**
     * Removes the first occurrence of the value from this LongBag, if
     * it is present.
     *
     * @param value the value to be removed
     * @return true, if value was removed
     */
    public boolean removeValue(final long value) throws ArrayIndexOutOfBoundsException
    {
        final int index = indexOf(value);
        if (index > -1L)
            remove(index);

        return index > -1L;
    }

    /**
     * Removes the element at the specified position in this Bag.
     * <p>
     * It does this by overwriting it was last element then removing last
     * element
     * </p>
     *
     * @param index the index of element to be removed
     * @return element that was removed from the Bag
     * @throws ArrayIndexOutOfBoundsException
     */
    public long remove(final int index) throws ArrayIndexOutOfBoundsException
    {
        final long e = data[index]; // make copy of element to remove so it can be returned
        data[index] = data[--size]; // overwrite item to remove with last element
        data[size] = 0; // null last element, so gc can do its work
        return e;
    }

    /**
     * Find index of element.
     *
     * @param value element to check
     * @return index of element, or {@code -1} if there is no such index.
     */
    public int indexOf(final long value)
    {
        for (int i = 0; size > i; i++)
        {
            if (value == data[i])
            {
                return i;
            }
        }
        return -1;
    }

    /**
     * Check if bag contains this element.
     *
     * @param value element to check
     * @return {@code true} if the bag contains this element
     */
    public boolean contains(final long value)
    {
        for (int i = 0; size > i; i++)
        {
            if (value == data[i])
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the element at the specified position in Bag.
     *
     * @param index index of the element to return
     * @return the element at the specified position in bag
     * @throws ArrayIndexOutOfBoundsException
     */
    public long get(final int index) throws ArrayIndexOutOfBoundsException
    {
        if (index >= size)
        {
            final String message = "tried accessing element " + index + "/" + size;
            throw new ArrayIndexOutOfBoundsException(message);
        }

        return data[index];
    }

    /**
     * Returns the number of elements in this bag.
     *
     * @return the number of elements in this bag
     */
    public int size()
    {
        return size;
    }

    /**
     * Returns the number of elements the bag can hold without growing.
     *
     * @return the number of elements the bag can hold without growing
     */
    public int getCapacity()
    {
        return data.length;
    }

    /**
     * Checks if the internal storage supports this index.
     *
     * @param index index to check
     * @return {@code true} if the index is within bounds
     */
    public boolean isIndexWithinBounds(final int index)
    {
        return index < getCapacity();
    }

    /**
     * Returns true if this bag contains no elements.
     *
     * @return {@code true} if this bag contains no elements
     */
    public boolean isEmpty()
    {
        return size == 0;
    }

    /**
     * Adds the specified element to the end of this bag.
     * <p>
     * If required, it also increases the capacity of the bag.
     * </p>
     *
     * @param value element to be added to this list
     */
    public void add(final long value)
    {
        // is size greater than capacity increase capacity
        if (size == data.length)
            grow(2 * data.length);

        data[size++] = value;
    }

    /**
     * Adds the specified elements to the end of this bag.
     * <p>
     * If required, it also increases the capacity of the bag.
     * </p>
     *
     * @param other elements to be added to this list
     */
    public void addAll(final LongBag other)
    {
        for (int i = 0; i < other.size(); i++)
        {
            add(other.data[i]);
        }
    }

    /**
     * Set element at specified index in the bag.
     *
     * @param index position of element
     * @param value the element
     */
    public void set(final int index, final long value)
    {
        if (index >= data.length)
        {
            grow(max((2 * data.length), index + 1));
        }

        size = max(size, index + 1);
        data[index] = value;
    }

    @SuppressWarnings("unchecked")
    private void grow(final int newCapacity) throws ArrayIndexOutOfBoundsException
    {
        final long[] oldData = data;
        data = new long[newCapacity];
        System.arraycopy(oldData, 0, data, 0, oldData.length);
    }

    /**
     * Check if an item, if added at the given item will fit into the bag.
     * <p>
     * If not, the bag capacity will be increased to hold an item at the index.
     * </p>
     *
     * @param index index to check
     */
    public void ensureCapacity(final int index)
    {
        if (index >= data.length)
        {
            grow(index + 1);
        }
    }

    /**
     * Removes all of the elements from this bag.
     * <p>
     * The bag will be empty after this call returns.
     * </p>
     */
    public void clear()
    {
        Arrays.fill(data, 0, size, 0L);
        size = 0;
    }

    /**
     * Returns this bag's underlying array.
     * <p>
     * Use with care.
     * </p>
     *
     * @return the underlying array
     * @see LongBag#size()
     */
    public long[] getData()
    {
        return data;
    }

    /**
     * Set the size.
     * <p>
     * This will not resize the bag, nor will it clean up contents beyond the
     * given size. Use with caution.
     * </p>
     *
     * @param size the size to set
     */
    public void setSize(final int size)
    {
        this.size = size;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final LongBag longBag = (LongBag) o;
        if (size != longBag.size())
            return false;

        for (int i = 0; size > i; i++)
        {
            if (data[i] != longBag.data[i])
                return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        for (int i = 0, s = size; s > i; i++)
        {
            hash = (127 * hash) + Long.hashCode(data[i]);
        }

        return hash;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("LongBag(");
        for (int i = 0; size > i; i++)
        {
            if (i > 0)
                sb.append(", ");
            sb.append(data[i]);
        }
        sb.append(')');
        return sb.toString();
    }
}
