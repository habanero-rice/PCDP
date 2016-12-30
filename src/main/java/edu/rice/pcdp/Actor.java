package edu.rice.pcdp;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import edu.rice.pcdp.runtime.ActorMessageWrapper;

/**
 * An abstract base class for all user-defined actor implementations.
 *
 * @author Max Grossman (jmg3@rice.edu)
 */
public abstract class Actor {
    /**
     * A queue of messages for this single actor.
     */
    private final ConcurrentLinkedQueue<ActorMessageWrapper> queue =
        new ConcurrentLinkedQueue<ActorMessageWrapper>();

    /**
     * Separate tracker for queue size of this actor, used to detect completion.
     */
    private final AtomicInteger queueSize = new AtomicInteger(0);

    /**
     * Method that extensions to Actor must implement to process a single
     * message sent to this actor.
     *
     * @param msg The message transmitted to this actor.
     */
    public abstract void process(Object msg);

    /**
     * Call send to transmit a message to this actor, consisting of an arbitrary
     * object.
     *
     * @param msg The message to send.
     */
    public final void send(final Object msg) {
        ActorMessageWrapper wrapper = new ActorMessageWrapper(msg);

        final int oldQueueSize = queueSize.getAndIncrement();
        queue.add(wrapper);

        if (oldQueueSize == 0) {
            PCDP.async(() -> {
                /*
                 * Guaranteed on entry that head must be non-null because the
                 * above code is synchronized, and just set it to non-null.
                 */
                boolean done = false;
                while (!done) {
                    /*
                     * We know a new message must be incoming soon because queue
                     * size was > 0.
                     */
                    ActorMessageWrapper curr;
                    do {
                        curr = queue.poll();
                    } while (curr == null);

                    process(curr.getMessage());

                    final int newQueueSize = queueSize.decrementAndGet();
                    done = (newQueueSize == 0);
                }
            });
        }
    }
}
