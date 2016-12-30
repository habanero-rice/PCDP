package edu.rice.pcdp.runtime;

/**
 * A wrapper class for the object sent in an actor message.
 *
 * @author Shams Imam (shams@rice.edu)
 * @author Max Grossman (jmg3@rice.edu)
 */
public final class ActorMessageWrapper {
    /**
     * The transmitted object.
     */
    private final Object msg;

    /**
     * The next message transmitted to the same target actor. null if no next
     * message.
     */
    private ActorMessageWrapper next;

    /**
     * Constructor.
     *
     * @param setMsg The object sent
     */
    public ActorMessageWrapper(final Object setMsg) {
        this.msg = setMsg;
        this.next = null;
    }

    /**
     * Set the next message sent to the same actor.
     *
     * @param setNext New next message.
     */
    public void setNext(final ActorMessageWrapper setNext) {
        this.next = setNext;
    }

    /**
     * Getter for next message in the queue.
     *
     * @return The next message sent to the same actor
     */
    public ActorMessageWrapper getNext() {
        return next;
    }

    /**
     * The body of this message.
     *
     * @return The value sent by the user code.
     */
    public Object getMessage() {
        return msg;
    }
}
