package com.genexus.util;
import java.util.Vector;

public class Quicksort
{
    // Sorts entire array
    public static void sort(FastVector array, Comparer comparer)
    {
        sort(array, 0, array.size() - 1, comparer);
    }

    // Sorts partial array
    public static void sort(FastVector array, int start, int end, Comparer comparer)
    {
        int p;
        if (end > start)
        {
            p = partition(array, start, end, comparer);
            sort(array, start, p-1, comparer);
            sort(array, p+1, end, comparer);
        }
    }

    protected static int partition(FastVector array, int start, int end, Comparer comparer)
    {
        int left, right;
        Object partitionElement;

        // Arbitrary partition start...there are better ways...
        partitionElement = array.elementAt(end);

        left = start - 1;
        right = end;
        for (;;)
        {
            while (comparer.compare(partitionElement, array.elementAt(++left)) >= 1)
            {
                if (left == end) break;
            }
            while (comparer.compare(partitionElement, array.elementAt(--right)) <= -1)
            {
                if (right == start) break;
            }
            if (left >= right) break;
            swap(array, left, right);
        }
        swap(array, left, end);

        return left;
    }

    protected static void swap(FastVector array, int i, int j)
    {
        Object temp;

        temp = array.elementAt(i);
        array.setElementAt(array.elementAt(j), i);
        array.setElementAt(temp, j);
    }
	
	
    // Sorts entire array
    public static void sort(Vector array, Comparer comparer)
    {
        sort(array, 0, array.size() - 1, comparer);
    }

    // Sorts partial array
    public static void sort(Vector array, int start, int end, Comparer comparer)
    {
        int p;
        if (end > start)
        {
            p = partition(array, start, end, comparer);
            sort(array, start, p-1, comparer);
            sort(array, p+1, end, comparer);
        }
    }

    protected static int partition(Vector array, int start, int end, Comparer comparer)
    {
        int left, right;
        Object partitionElement;

        // Arbitrary partition start...there are better ways...
        partitionElement = array.elementAt(end);

        left = start - 1;
        right = end;
        for (;;)
        {
            while (comparer.compare(partitionElement, array.elementAt(++left)) >= 1)
            {
                if (left == end) break;
            }
            while (comparer.compare(partitionElement, array.elementAt(--right)) <= -1)
            {
                if (right == start) break;
            }
            if (left >= right) break;
            swap(array, left, right);
        }
        swap(array, left, end);

        return left;
    }

    protected static void swap(Vector array, int i, int j)
    {
        Object temp;

        temp = array.elementAt(i);
        array.setElementAt(array.elementAt(j), i);
        array.setElementAt(temp, j);
    }	
}
