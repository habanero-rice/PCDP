package edu.rice.pcdp;

import edu.rice.pcdp.runtime.BaseTask;
import edu.rice.pcdp.runtime.BaseTask.FinishTask;
import edu.rice.pcdp.runtime.BaseTask.FutureTask;
import edu.rice.pcdp.runtime.Runtime;
import edu.rice.pcdp.runtime.IsolatedManager;
import edu.rice.pcdp.config.SystemProperty;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * The main class of the PCDP framework, containing most of the user-visible
 * APIs.
 *
 * @author Shams Imam (shams@rice.edu)
 * @author Max Grossman (jmg3@rice.edu)
 */
public final class PCDP {
    protected static final String missingFinishMsg = "A new task cannot be " +
        "created at the top-level of your program. It must be created from " +
        "within a finish or another async. Please ensure that all asyncs, " +
        "actor sends, parallel loops, and other types of " +
        "parallelism-creating constructs are not called at the top-level of " +
        "your program.";

    /**
     * Default constructor.
     */
    private PCDP() {
    }

    /**
     * A singleton utility for managing the Java locks used to implement global
     * and object-based isolation.
     */
    private static final IsolatedManager isolatedManager =
        new IsolatedManager();

    /**
     * finish waits for all tasks spawned in runnable to complete, as well as
     * any transitively spawned child, granchild, etc tasks. The method call to
     * finish will only return once all tasks have completed.
     *
     * @param runnable User-written body of the finish scope to execute.
     */
    public static void finish(final Runnable runnable) {
        final BaseTask currentTask = Runtime.currentTask();
        final FinishTask newTask = new FinishTask(runnable);
        if (currentTask == null) {
            Runtime.submitTask(newTask);
        } else {
            newTask.compute();
        }
        newTask.awaitCompletion();
    }

    /**
     * async creates an asynchronously executing task.
     *
     * @param runnable User-written body of the task.
     */
    public static void async(final Runnable runnable) {
        final FutureTask<Void> newTask = createFutureTask(runnable);
        newTask.fork();
    }

    /**
     * Construct to represent loop-level parallelism. There is no implicit
     * finish on the iterations of a forasync.
     * <p>
     * The semantics of
     * <pre><code>
     *   forasync(startInc, endInc, (k) -> S2(k))
     * </code></pre>
     * are as follows:
     * <pre><code>
     *   for (int k = startInc; k <= endInc; k++) {
     *     final int kk = k;
     *     async(() -> {
     *       S2(kk);
     *     });
     *   }
     * </code></pre>
     *
     * @param startInc The start of the loop range this parallel loop executes
     *        over (inclusive).
     * @param endInc   The end of the loop range this parllel loop executes over
     *        (exclusive).
     * @param body     a {@link ProcedureInt1D} object defining the body of the
     *        parallel loop.
     */
    public static void forasync(
            final int startInc,
            final int endInc,
            final ProcedureInt1D body) {
        assert (startInc <= endInc);

        for (int i = startInc; i <= endInc; i++) {
            final int loopIndex = i;
            final Runnable loopRunnable = () -> body.apply(loopIndex);
            async(loopRunnable);
        }
    }

    /**
     * A two-dimensional variant on forasync, analogous to two nested parallel
     * loops.
     *
     * @param startInc0 The starting value for the outermost sequential loop.
     * @param endInc0 The final value for the outermost sequential loop
     *        (inclusive).
     * @param startInc1 The starting value for the innermost sequential loop.
     * @param endInc1 The final value for the innermost sequential loop
     *        (inclusive).
     * @param body a {@link ProcedureInt2D} object defining the body of these
     *        nested loops.
     */
    public static void forasync2d(final int startInc0, final int endInc0,
            final int startInc1, final int endInc1, final ProcedureInt2D body) {
        assert (startInc0 <= endInc0);
        assert (startInc1 <= endInc1);

        for (int i = startInc0; i <= endInc0; i++) {
            final int iCopy = i;

            for (int j = startInc1; j <= endInc1; j++) {
                final int jCopy = j;

                final Runnable loopRunnable = () -> body.apply(iCopy, jCopy);
                async(loopRunnable);
            }
        }
    }

    /**
     * A similar loop-parallel construct to forasync, with the only difference
     * being that forall includes an implicit finish wrapping all asynchronous
     * tasks created.
     *
     * @param startInc The start of the loop range this parallel loop executes
     *        over (inclusive).
     * @param endInc   The end of the loop range this parllel loop executes over
     *        (exclusive).
     * @param body     a {@link ProcedureInt1D} object defining the body of the
     *        parallel loop.
     */
    public static void forall(final int startInc, final int endInc,
            final ProcedureInt1D body) {
        finish(() -> {
            forasync(startInc, endInc, body);
        });
    }

    /**
     * A two-dimensional variant on forall.
     *
     * @param startInc0 The starting value for the outermost sequential loop.
     * @param endInc0 The final value for the outermost sequential loop
     *        (inclusive).
     * @param startInc1 The starting value for the innermost sequential loop.
     * @param endInc1 The final value for the innermost sequential loop
     *        (inclusive).
     * @param body a {@link ProcedureInt2D} object defining the body of these
     *        nested loops.
     */
    public static void forall2d(final int startInc0, final int endInc0,
            final int startInc1, final int endInc1, final ProcedureInt2D body) {
        finish(() -> {
            forasync2d(startInc0, endInc0, startInc1, endInc1, body);
        });
    }

    /**
     * Semantically equivalent to forasync, but internally the runtime chunks
     * the defined parallel loop according to chunkSize. If chunkSize does not
     * evenly divide the loop range specified, the runtime will automatically
     * cut off any extra loop iterations and not execute them.
     *
     * @param start The starting iteration for the parallel loop
     * @param endInclusive The ending iteration for the parallel loop
     *        (inclusive)
     * @param chunkSize The number of iterations to chunk together for
     *        processing by a single asynchronous task
     * @param body The body of the loop to execute
     */
    public static void forasyncChunked(final int start, final int endInclusive,
            final int chunkSize, final ProcedureInt1D body) {
        assert (start <= endInclusive);

        for (int i = start; i <= endInclusive; i += chunkSize) {
            final int iCopy = i;

            async(() -> {
                int end = iCopy + chunkSize - 1;
                if (end > endInclusive) {
                    end = endInclusive;
                }
                for (int innerI = iCopy; innerI <= end; innerI++) {
                    body.apply(innerI);
                }
            });
        }
    }

    /**
     * A variant of one-dimensional forasyncChunked that internally selects a
     * sane chunk size.
     *
     * @param start The starting iteration for the parallel loop
     * @param endInclusive The ending iteration for the parallel loop
     *        (inclusive)
     * @param body The body of the loop to execute

     */
    public static void forasyncChunked(final int start, final int endInclusive,
            final ProcedureInt1D body) {
        forasyncChunked(start, endInclusive,
                getChunkSize(endInclusive - start + 1, numThreads() * 2), body);
    }

    /**
     * A two-dimensional variant of forasyncChunked.
     *
     * @param start0 The starting iteration for the outermost parallel loop
     * @param endInclusive0 The ending iteration for the outermost parallel loop
     *        (inclusive)
     * @param start1 The starting iteration for the innermost parallel loop
     * @param endInclusive1 The ending iteration for the innermost parallel loop
     *        (inclusive)
     * @param chunkSize The number of iterations to chunk together for
     *        processing by a single asynchronous task
     * @param body The body of the loop to execute
     */
    public static void forasync2dChunked(final int start0,
            final int endInclusive0, final int start1, final int endInclusive1,
            final int chunkSize, final ProcedureInt2D body) {
        assert (start0 <= endInclusive0);
        assert (start1 <= endInclusive1);

        final int outerNIters = endInclusive0 - start0 + 1;
        final int innerNIters = endInclusive1 - start1 + 1;
        final int numIters = outerNIters * innerNIters;

        forasyncChunked(0, numIters - 1, chunkSize, (i) -> {
            int outer = i / innerNIters;
            int inner = i % innerNIters;

            body.apply(start0 + outer, start1 + inner);
        });
    }

    /**
     * A variant of two-dimensional forasyncChunked that internally selects a
     * sane chunk size.
     *
     * @param start0 The starting iteration for the outermost parallel loop
     * @param endInclusive0 The ending iteration for the outermost parallel loop
     *        (inclusive)
     * @param start1 The starting iteration for the innermost parallel loop
     * @param endInclusive1 The ending iteration for the innermost parallel loop
     *        (inclusive)
     * @param body The body of the loop to execute
     */
    public static void forasync2dChunked(final int start0,
            final int endInclusive0, final int start1, final int endInclusive1,
            final ProcedureInt2D body) {
        final int numIters = (endInclusive0 - start0 + 1)
            * (endInclusive1 - start1 + 1);
        forasync2dChunked(start0, endInclusive0, start1, endInclusive1,
                getChunkSize(numIters, numThreads() * 2), body);
    }

    /**
     * A chunked variant of the forall APIs. forallChunked has the same chunking
     * semantics as forasyncChunked, but with an implicit, blocking finish for
     * all created tasks.
     *
     * @param start The starting iteration for the parallel loop
     * @param endInclusive The ending iteration for the parallel loop
     *        (inclusive)
     * @param chunkSize The number of iterations to chunk together for
     *        processing by a single asynchronous task
     * @param body The body of the loop to execute
     */
    public static void forallChunked(final int start, final int endInclusive,
            final int chunkSize, final ProcedureInt1D body) {
        finish(() -> {
            forasyncChunked(start, endInclusive, chunkSize, body);
        });
    }

    /**
     * A variant of one-dimensional forallChunked that internally selects a sane
     * chunk size.
     *
     * @param start The starting iteration for the parallel loop
     * @param endInclusive The ending iteration for the parallel loop
     *        (inclusive)
     * @param body The body of the loop to execute
     */
    public static void forallChunked(final int start, final int endInclusive,
            final ProcedureInt1D body) {
        forallChunked(start, endInclusive,
                getChunkSize(endInclusive - start + 1, numThreads() * 2), body);
    }

    /**
     * A two-dimensional variant of forallChunked.
     *
     * @param start0 The starting iteration for the outermost parallel loop
     * @param endInclusive0 The ending iteration for the outermost parallel loop
     *        (inclusive)
     * @param start1 The starting iteration for the innermost parallel loop
     * @param endInclusive1 The ending iteration for the innermost parallel loop
     *        (inclusive)
     * @param chunkSize The number of iterations to chunk together for
     *        processing by a single asynchronous task
     * @param body The body of the loop to execute
     */
    public static void forall2dChunked(final int start0,
            final int endInclusive0, final int start1, final int endInclusive1,
            final int chunkSize, final ProcedureInt2D body) {
        finish(() -> {
            forasync2dChunked(start0, endInclusive0, start1, endInclusive1,
                chunkSize, body);
        });
    }

    /**
     * A variant of two-dimensional forallChunked that internally selects a sane
     * chunk size.
     *
     * @param start0 The starting iteration for the outermost parallel loop
     * @param endInclusive0 The ending iteration for the outermost parallel loop
     *        (inclusive)
     * @param start1 The starting iteration for the innermost parallel loop
     * @param endInclusive1 The ending iteration for the innermost parallel loop
     *        (inclusive)
     * @param body The body of the loop to execute
     */
    public static void forall2dChunked(final int start0, final int endInclusive0,
            final int start1, final int endInclusive1,
            final ProcedureInt2D body) {
        final int numIters = (endInclusive0 - start0 + 1)
            * (endInclusive1 - start1 + 1);
        forall2dChunked(start0, endInclusive0, start1, endInclusive1,
                getChunkSize(numIters, numThreads() * 2), body);
    }

    /**
     * Spawn an asynchronous task that returns a value of type <R>. Return a
     * future object that can be used to wait on the completion of the spawned
     * task, and fetch its resulting value.
     *
     * @param body user-defined body of the task.
     * @param <R> Return type of the launched future task.
     * @return A future that can be used to wait on the completion of the
     *         created task and fetch its return value.
     */
    public static <R> Future<R> future(final Callable<R> body) {
        final FutureTask<R> newTask = createFutureTask(body, false);
        newTask.fork();
        return newTask.future();
    }

    /**
     * Creates an asynchronous task whose execution is predicated on the
     * satisfaction of the futures passed to asyncAwait. runnable will not start
     * executing until all futures have been put on.
     *
     * @param runnable Body of the task.
     * @param futures Future objects to block the spawned task on.
     */
    public static void asyncAwait(final Runnable runnable,
            final Future<? extends Object>... futures) {
        final FutureTask<Void> newTask = createFutureTask(runnable);
        CompletableFuture.
            allOf(wrapToCompletableFutures(futures)).
            whenComplete((a, b) -> newTask.fork());
    }

    /**
     * A hybridization of future() and asyncAwait(). futureAwait creates an
     * asynchronous task whose execution is both predicated on the provided
     * futures, and which also returns a future that is satisfied when it
     * completes.
     *
     * @param runnable Body of the task.
     * @param futures Future objects to block the spawned task on.
     * @param <R> Return type of the launched future task.
     * @return A future that can be used to wait on the completion of the
     *         created task and fetch its return value.
     */
    public static <R> Future<R> futureAwait(final Callable<R> runnable,
            final Future<? extends Object>... futures) {
        final FutureTask<R> newTask = createFutureTask(runnable, false);
        CompletableFuture.
            allOf(wrapToCompletableFutures(futures)).
            whenComplete((a, b) -> newTask.fork());
        return newTask.future();
    }

    /**
     * Internal utility for creating a future to schedule on the runtime.
     *
     * @param runnable Body of the task.
     * @return A FutureTask object to execute on the runtime.
     */
    private static FutureTask<Void> createFutureTask(final Runnable runnable) {
        final BaseTask currentTask = Runtime.currentTask();
        if (currentTask == null) {
            throw new IllegalStateException(missingFinishMsg);
        }
        return createFutureTask(
            () -> {
                runnable.run();
                return null;

            },
            true);
    }

    /**
     * Internal utility for creating a future to schedule on the runtime. This
     * variant of createFutureTask allows changing the exception throwing
     * behavior of the created task.
     *
     * @param body Body of the task.
     * @param rethrowException Control whether exceptions are hidden or
     *        immediately thrown by the calling task.
     * @param <R> Object type for the created future task.
     * @return A FutureTask object to execute on the runtime.
     */
    private static <R> FutureTask<R> createFutureTask(
        final Callable<R> body, final boolean rethrowException) {
        final BaseTask currentTask = Runtime.currentTask();
        if (currentTask == null) {
            throw new IllegalStateException(missingFinishMsg);
        }
        return new FutureTask<>(body, currentTask.ief(), rethrowException);
    }

    /**
     * Convert a list of Java Futures to a list of PCDP CompletableFutures.
     *
     * @param futures List of Java Future objects
     * @return Identical list of CompletableFuture objects.
     */
    private static CompletableFuture<?>[] wrapToCompletableFutures(
            final Future<? extends Object>... futures) {
        final CompletableFuture<?>[] result =
            new CompletableFuture[futures.length];
        for (int i = 0; i < futures.length; i++) {
            final Future<? extends Object> future = futures[i];
            if (future instanceof CompletableFuture) {
                result[i] = (CompletableFuture) future;
            } else {
                throw new IllegalArgumentException("Future at index " + i
                        + " is not an instance of CompletableFuture!");
            }
        }
        return result;
    }

    /**
     * An API in the same style as PCDP's parallel loop APIs, but which executes
     * the defined loop sequentially. All loops have an increment of +1.
     *
     * @param start The starting value for the sequential loop.
     * @param endInclusive The final value for the sequential loop (inclusive).
     * @param body a {@link ProcedureInt1D} object defining the body of these
     *        nested loops.
     */
    public static void forseq(final int start, final int endInclusive,
            final ProcedureInt1D body) {
        assert (start <= endInclusive);

        for (int i = start; i <= endInclusive; i++) {
            body.apply(i);
        }
    }


    /**
     * An API in the same style as PCDP's parallel loop APIs, but which executes
     * the defined loop sequentially. All loops have an increment of +1.
     *
     * @param start0 The starting value for the outermost sequential loop.
     * @param endInclusive0 The final value for the outermost sequential loop
     *        (inclusive).
     * @param start1 The starting value for the innermost sequential loop.
     * @param endInclusive1 The final value for the innermost sequential loop
     *        (inclusive).
     * @param body a {@link ProcedureInt2D} object defining the body of these
     *        nested loops.
     */
    public static void forseq2d(final int start0, final int endInclusive0,
            final int start1, final int endInclusive1,
            final ProcedureInt2D body) {
        assert (start0 <= endInclusive0);
        assert (start1 <= endInclusive1);

        for (int i = start0; i <= endInclusive0; i++) {
            for (int j = start1; j <= endInclusive1; j++) {
                body.apply(i, j);
            }
        }
    }

    /**
     * Retrieve the number of software threads the PCDP runtime was configured
     * with.
     *
     * @return The number of PCDP runtime threads.
     */
    public static int numThreads() {
        return Integer.parseInt(SystemProperty.numWorkers.getPropertyValue());
    }

    /**
     * Use rounded-up integer divide to compute an appropriate chunk size for N
     * elements to ensure nChunks completely covers all elements.
     *
     * @param nElements The number of elements to chunk
     * @param nChunks The number of chunks to create
     * @return The number of elements in each chunk
     */
    private static int getChunkSize(final int nElements, final int nChunks) {
        return (nElements + nChunks - 1) / nChunks;
    }

    /**
     * Global isolated statement. runnable will execute in isolation with all
     * other isolated blocks.
     *
     * @param runnable The body to be executed in isolation.
     */
    public static void isolated(final Runnable runnable) {
        isolatedManager.acquireAllLocks();
        try {
            runnable.run();
        } finally {
            isolatedManager.releaseAllLocks();
        }
    }

    /**
     * Object-based isolation on a single object. isolated guarantees that
     * runnable will not execute in parallel with any other global isolation
     * blocks, or object-based isolated blocks that also are isolated on obj.
     *
     * @param obj The object to implement isolation on.
     * @param runnable The body to be executed in isolation.
     */
    public static void isolated(final Object obj, final Runnable runnable) {
        Object[] objArr = new Object[1];
        objArr[0] = obj;

        isolatedManager.acquireLocksFor(objArr);
        try {
            runnable.run();
        } finally {
            isolatedManager.releaseLocksFor(objArr);
        }
    }

    /**
     * Object-based isolation on two objects, obj1 and obj2. The body runnable
     * is guaranteed to execute in mutual exclusion with any other global
     * isolated blocks, or any object-based isolated blocks that are isolated on
     * obj1 and/or obj2.
     *
     * @param obj1 The first object to implement isolation on.
     * @param obj2 The second object to implement isolation on.
     * @param runnable The body to be executed in isolation.
     */
    public static void isolated(final Object obj1, final Object obj2,
            final Runnable runnable) {
        Object[] objArr = new Object[2];
        objArr[0] = obj1;
        objArr[1] = obj2;

        isolatedManager.acquireLocksFor(objArr);
        try {
            runnable.run();
        } finally {
            isolatedManager.releaseLocksFor(objArr);
        }
    }
}
