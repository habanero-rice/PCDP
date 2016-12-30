package edu.rice.pcdp.runtime;

import java.util.TreeSet;
import java.util.Comparator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A wrapper for managing Java locks used to implement global and object-based
 * isolation.
 *
 * @author Shams Imam (shams@rice.edu)
 * @author Max Grossman (jmg3@rice.edu)
 */
public final class IsolatedManager {
    /**
     * The number of locks to use to implement hash-based, object-based
     * isolation. More locks reduce contention, but less locks reduce memory
     * consumption.
     */
    private final int nLocks = 64;
    /**
     * The Java locks that are actually used to implement isolated mutual
     * exclusion.
     */
    private final Lock[] locks = new Lock[nLocks];

    /**
     * Default constructor, initialized backing locks.
     */
    public IsolatedManager() {
        for (int i = 0; i < locks.length; i++) {
            locks[i] = new ReentrantLock();
        }
    }

    /**
     * Compute an index into the locks array for the provided object.
     *
     * @param obj The object to compute a lock index for use in object-based
     *        isolation.
     * @return Lock index for obj.
     */
    private int lockIndexFor(final Object obj) {
        return Math.abs(obj.hashCode()) % nLocks;
    }

    /**
     * Sort the provided list of objects by the lock indices they will need to
     * acquire for object-based isolation.
     *
     * @param objects Objects to implement object-based isolation for.
     * @return Sorted set of objects
     */
    private TreeSet<Object> createSortedObjects(final Object[] objects) {
        TreeSet<Object> sorted = new TreeSet<Object>(new Comparator<Object>() {
            @Override
            public int compare(final Object o1, final Object o2) {
                return lockIndexFor(o1) - lockIndexFor(o2);
            }
        });

        for (Object obj : objects) {
            sorted.add(obj);
        }

        return sorted;
    }

    /**
     * For global isolation, acquire all locks.
     */
    public void acquireAllLocks() {
        for (int i = 0; i < locks.length; i++) {
            locks[i].lock();
        }
    }

    /**
     * For global isolation, release all locks.
     */
    public void releaseAllLocks() {
        for (int i = locks.length - 1; i >= 0; i--) {
            locks[i].unlock();
        }
    }

    /**
     * For object-based isolation, acquire the locks in this isolated manager
     * corresponding to the provided objects.
     *
     * @param objects Objects to perform object-based isolation on.
     */
    public void acquireLocksFor(final Object[] objects) {
        final TreeSet<Object> sorted = createSortedObjects(objects);

        for (Object obj : sorted) {
            final int lockIndex = lockIndexFor(obj);
            locks[lockIndex].lock();
        }
    }

    /**
     * After completing a region of object-based isolation, release the same
     * locks that were originally acquired for the provided objects.
     *
     * @param objects Objects to perform object-based isolation on
     */
    public void releaseLocksFor(final Object[] objects) {
        final TreeSet<Object> sorted = createSortedObjects(objects);
        for (Object obj : sorted) {
            final int lockIndex = lockIndexFor(obj);
            locks[lockIndex].unlock();
        }
    }
}
