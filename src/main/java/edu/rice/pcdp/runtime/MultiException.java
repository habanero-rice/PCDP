package edu.rice.pcdp.runtime;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>MultiException class.</p>
 *
 * @author Shams Imam (shams@rice.edu)
 * @author Max Grossman (jmg3@rice.edu)
 */
public final class MultiException extends RuntimeException {

    /**
     * Wrapped exceptions.
     */
    private final List<Throwable> exceptions;

    /**
     * <p>Constructor for MultiException.</p>
     *
     * @param inputThrowableList a {@link java.util.List} object.
     */
    public MultiException(final List<Throwable> inputThrowableList) {
        final List<Throwable> throwableList = new ArrayList<>();
        for (final Throwable th : inputThrowableList) {
            if (th instanceof MultiException) {
                final MultiException me = (MultiException) th;
                throwableList.addAll(me.exceptions);
            } else {
                throwableList.add(th);
            }
        }
        this.exceptions = throwableList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void printStackTrace() {
        printStackTrace(System.out);
    }

    @Override
    public void printStackTrace(final PrintStream printStream) {
        super.printStackTrace(printStream);

        final int numExceptions = exceptions.size();
        printStream.println("  Number of exceptions: " + numExceptions);
        final int numExceptionsToDisplay = Math.min(5, numExceptions);
        printStream.println("  Printing " + numExceptionsToDisplay
                + " stack traces...");

        for (int i = 0; i < numExceptionsToDisplay; i++) {
            final Throwable exception = exceptions.get(i);
            exception.printStackTrace(printStream);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return exceptions.toString();
    }

    /**
     * Getter for wrapped exceptions.
     * 
     * @return The exceptions wrapped
     */
    public List<Throwable> getExceptions() {
        return exceptions;
    }
}
