package edu.rice.pcdp.future;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static edu.rice.pcdp.PCDP.finish;
import static edu.rice.pcdp.PCDP.future;
import static org.junit.Assert.assertEquals;

/**
 * Test explicit calls to force on async<void>.
 *
 * @author vcave
 */
@RunWith(JUnit4.class)
public class TestFuture2 {

    @Test
    public void testMethod() throws ExecutionException, InterruptedException {

        final boolean[] result = {false};
        finish(() -> {
            try {
                result[0] = new TestFuture2().run();
            } catch (final Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        assertEquals(true, result[0]);
    }

    public boolean run() throws ExecutionException, InterruptedException {

        final Future<A> promise = future(() -> {
            return new B();
        });

        final A a = promise.get();
        final B b = (B) promise.get();

        return true;
    }

    public static class A {
    }

    public static class B extends A {
    }
}
