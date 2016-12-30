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
public class TestAwait2 {

    @Test
    public void testMethod1() {
        final boolean[] result = {false};
        result[0] = new TestAwait2().run1();
        assertEquals(true, result[0]);
    }

    public boolean run1() {

        finish(() -> {
            final CompletableFuture<Integer> f1 = new CompletableFuture<>();
            final CompletableFuture<Integer> f2 = new CompletableFuture<>();

            f1.complete(1);
            f2.complete(2);

            asyncAwait(() -> {
                Math.random();
            }, f1, f2);
        });

        return true;
    }

    @Test
    public void testMethod2() {
        final boolean[] result = {false};
        result[0] = new TestAwait2().run2();
        assertEquals(true, result[0]);
    }

    public boolean run2() {

        finish(() -> {
            final CompletableFuture<Number> f1 = new CompletableFuture<>();
            final CompletableFuture<Number> f2 = new CompletableFuture<>();

            f1.complete(1);
            f2.complete(2.0);

            asyncAwait(() -> {
                Math.random();
            }, f1, f2);
        });

        return true;
    }
}
