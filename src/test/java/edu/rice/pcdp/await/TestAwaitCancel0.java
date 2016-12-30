package edu.rice.pcdp.await;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.concurrent.CompletableFuture;

import static edu.rice.pcdp.PCDP.asyncAwait;
import static edu.rice.pcdp.PCDP.finish;
import static org.junit.Assert.assertEquals;

/**
 * Simple test that spawns an async await, which does nothing interesting.
 *
 * @author vcave
 */
@RunWith(JUnit4.class)
public class TestAwaitCancel0 {

    @Test
    public void testMethod1() {
        final boolean[] result = {false};
        result[0] = new TestAwaitCancel0().run1();
        assertEquals(true, result[0]);
    }

    public boolean run1() {

        final boolean[] res = {false};

        finish(() -> {
            final CompletableFuture<Integer> f1 = new CompletableFuture<>();
            final CompletableFuture<Integer> f2 = new CompletableFuture<>();

            f1.cancel(true);
            f2.complete(2);

            asyncAwait(() -> {
                final boolean f1Failed = f1.isCancelled();
                final boolean f2Resolved = f2.isDone();
                res[0] = f1Failed && f2Resolved;
            }, f1, f2);
        });
        return res[0];
    }

    @Test
    public void testMethod2() {
        final boolean[] result = {false};
        result[0] = new TestAwaitCancel0().run2();
        assertEquals(true, result[0]);
    }

    public boolean run2() {

        final boolean[] res = {false};

        finish(() -> {
            final CompletableFuture<Integer> f1 = new CompletableFuture<>();
            final CompletableFuture<Integer> f2 = new CompletableFuture<>();

            asyncAwait(() -> {
                final boolean f1Failed = f1.isCancelled();
                final boolean f2Resolved = f2.isDone();
                res[0] = f1Failed && f2Resolved;
            }, f1, f2);

            f1.cancel(true);
            f2.complete(2);
        });
        return res[0];
    }
}
