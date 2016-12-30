package edu.rice.pcdp.runtime;

import edu.rice.pcdp.config.Configuration;
import edu.rice.pcdp.config.SystemProperty;

import java.util.Stack;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

/**
 * @author Shams Imam (shams@rice.edu)
 * @author Max Grossman (jmg3@rice.edu)
 */

public final class Runtime {
    /**
     * Default constructor.
     */
    private Runtime() {
    }

    /**
     * For each thread, a stack listing the tasks executing on this thread.
     */
    private static final ThreadLocal<Stack<BaseTask>> threadLocalTaskStack =
        new ThreadLocal<Stack<BaseTask>>() {
            @Override
            protected Stack<BaseTask> initialValue() {
                return new Stack<>();
            }
        };

    /**
     * The thread pool backing this instance of Runtime.
     */
    private static ForkJoinPool taskPool = new ForkJoinPool(
            Configuration.readIntProperty(SystemProperty.numWorkers));

    /**
     * A method for altering the number of worker threads used by PCDP at
     * runtime. It is the programmer's responsibility to ensure that no tasks
     * are executing or pending on the runtime when this call is made.
     *
     * @param numWorkers The number of workers to switch to using.
     * @throws InterruptedException An error occurs shutting down the existing
     *         runtime instance.
     */
    public static void resizeWorkerThreads(final int numWorkers)
            throws InterruptedException {
        taskPool.shutdown();
        boolean terminated = taskPool.awaitTermination(10, TimeUnit.SECONDS);
        assert (terminated);

        SystemProperty.numWorkers.set(numWorkers);
        taskPool = new ForkJoinPool(numWorkers);
    }

    /**
     * Get the current task of the current thread.
     * @return Currently executing task.
     */
    public static BaseTask currentTask() {
        final Stack<BaseTask> taskStack = Runtime.threadLocalTaskStack.get();
        if (taskStack.isEmpty()) {
            return null;
        } else {
            return taskStack.peek();
        }
    }

    /**
     * Track the passed task as the currently executing task for this thread.
     * @param task Currently executing task
     */
    public static void pushTask(final BaseTask task) {
        Runtime.threadLocalTaskStack.get().push(task);
    }

    /**
     * Remove the top of the task-tracking stack for the current thread.
     */
    public static void popTask() {
        Runtime.threadLocalTaskStack.get().pop();
    }

    /**
     * Run the provided task on the thread pool backing this runtime.
     * @param task Task to make eligible for execution.
     */
    public static void submitTask(final BaseTask task) {
        taskPool.execute(task);
    }

    /**
     * Print some basic runtime statistics.
     */
    public static void showRuntimeStats() {
        System.out.println("Runtime Stats (" + Configuration.BUILD_INFO
                + "): ");
        System.out.println("   " + taskPool.toString());
        System.out.println("   # finishes = "
                + BaseTask.FinishTask.TASK_COUNTER.get());
        System.out.println("   # asyncs = "
                + BaseTask.FutureTask.TASK_COUNTER.get());
    }
}
