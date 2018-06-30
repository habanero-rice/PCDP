package edu.rice.pcdp.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Shams Imam (shams@rice.edu)
 * @author Max Grossman (jmg3@rice.edu)
 */
public abstract class BaseTask extends CountedCompleter<Void> {

    /**
     * Default constructor.
     */
    public BaseTask() {
        super();
    }

    /**
     * Fetch the immediately enclosing finish of a task.
     *
     * @return The immediately enclosing finish of the task this is called on.
     *         For finish tasks, returns this.
     */
    public abstract FinishTask ief();

    /**
     * @author Shams Imam (shams@rice.edu)
     */
    public static final class FinishTask extends BaseTask {

        /**
         * Count all finish scopes.
         */
        protected static final AtomicLong TASK_COUNTER = new AtomicLong();

        /**
         * Body of this task.
         */
        private final Runnable runnable;

        /**
         * The Exception list is used to collect exceptions issued when tasks
         * associated with this finish scope terminate abruptly. This field is
         * initialized lazily.
         */
        private List<Throwable> exceptionList;

        /**
         * Constructor for a finish task.
         *
         * @param setRunnable User-defined body of this task.
         */
        public FinishTask(final Runnable setRunnable) {
            super();
            this.runnable = setRunnable;
            this.exceptionList = null;
            TASK_COUNTER.incrementAndGet();
        }

        @Override
        public void compute() {
            // Make this the current task for the current runtime thread
            Runtime.pushTask(this);
            try {
                // Execute the body
                runnable.run();
            } catch (final Throwable th) {
                pushException(th);
            } finally {
                tryComplete();
                Runtime.popTask();
                awaitCompletion();
            }
        }

        /**
         * Wait for all tasks registered on this finish scope to complete.
         */
        public void awaitCompletion() {
            try {
                join();
            } catch (final Exception ex) {
                pushException(ex);
            }

            final List<Throwable> finalExceptionList = exceptions();
            if (!finalExceptionList.isEmpty()) {
                if (finalExceptionList.size() == 1) {
                    final Throwable t = finalExceptionList.get(0);
                    if (t instanceof Error) {
                        throw (Error) t;
                    } else if (t instanceof RuntimeException) {
                        throw (RuntimeException) t;
                    }
                }
                throw new MultiException(finalExceptionList);
            }
        }

        @Override
        public FinishTask ief() {
            return this;
        }

        /**
         * Create a new stack or return current one.
         *
         * @return the finish stack
         */
        private List<Throwable> exceptions() {
            if (exceptionList == null) {
                exceptionList = new ArrayList<>();
            }
            return exceptionList;
        }

        /**
         * Push an exception onto the stack. Do not decrement finishCount ---
         * this exception was thrown by inline code, not a spawned activity.
         *
         * @param throwable Thrown exception
         */
        public void pushException(final Throwable throwable) {
            synchronized (this) {
                this.exceptions().add(throwable);
            }
        }
    }

    /**
     * @author Shams Imam (shams@rice.edu)
     * @param <R> Return type for this future.
     */
    public static final class FutureTask<R> extends BaseTask {

        /**
         * Count all asynchronous tasks.
         */
        protected static final AtomicLong TASK_COUNTER = new AtomicLong();

        /**
         * Body of this task.
         */
        private final Runnable runnable;
        /**
         * Finish scope for this task.
         */
        private final FinishTask immediatelyEnclosingFinish;
        /**
         * Flag used to signal cancellation of this task.
         */
        private final AtomicBoolean cancellationFlag = new AtomicBoolean(false);
        /**
         * Future for this task.
         */
        private final CompletableFuture<R> completableFuture =
            new CompletableFuture<R>() {
                @Override
                public boolean cancel(final boolean mayInterruptIfRunning) {
                    return cancellationFlag.compareAndSet(false, true)
                        && super.cancel(mayInterruptIfRunning);
                }
            };

        /**
         * Constructor for FutureTask.
         *
         * @param setRunnable User-defined body of this task.
         * @param setImmediatelyEnclosingFinish Finish scope for this task.
         * @param rethrowException Whether to respond to caught exceptions by
         *        re-throwing.
         */
        public FutureTask(
            final Callable<R> setRunnable,
            final FinishTask setImmediatelyEnclosingFinish,
            final boolean rethrowException) {
            super();
            if (setImmediatelyEnclosingFinish == null) {
                throw new IllegalStateException(
                        "Async is not executing inside a finish!");
            }
            this.runnable = () -> {
                try {
                    final R result = setRunnable.call();
                    completableFuture.complete(result);
                } catch (final Exception ex) {
                    completableFuture.completeExceptionally(ex);
                    if (rethrowException) {
                        if (ex instanceof RuntimeException) {
                            throw (RuntimeException) ex;
                        } else {
                            throw new RuntimeException(
                                    "Error in executing callable", ex);
                        }
                    }
                }
            };
            this.immediatelyEnclosingFinish = setImmediatelyEnclosingFinish;
            this.immediatelyEnclosingFinish.addToPendingCount(1);
            TASK_COUNTER.incrementAndGet();
        }

        @Override
        public void compute() {
            Runtime.pushTask(this);
            try {
                if (!cancellationFlag.get()) {
                    // execute the body
                    runnable.run();
                }
            } catch (final Throwable th) {
                immediatelyEnclosingFinish.pushException(th);
            } finally {
                immediatelyEnclosingFinish.tryComplete();
                Runtime.popTask();
            }
        }

        @Override
        public FinishTask ief() {
            return immediatelyEnclosingFinish;
        }

        /**
         * Get the future container associated with this task.
         *
         * @return Future for querying this task
         */
        public CompletableFuture<R> future() {
            return completableFuture;
        }
    }
}
