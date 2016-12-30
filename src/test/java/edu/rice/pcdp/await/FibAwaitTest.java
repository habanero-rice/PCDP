package edu.rice.pcdp.await;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.concurrent.CompletableFuture;

import static edu.rice.pcdp.PCDP.async;
import static edu.rice.pcdp.PCDP.asyncAwait;
import static edu.rice.pcdp.PCDP.finish;
import static org.junit.Assert.assertEquals;

/**
 * @author <a href="http://shams.web.rice.edu/">Shams Imam</a> (shams@rice.edu)
 */
@RunWith(JUnit4.class)
public class FibAwaitTest {

    private static int kernel(final int N) {

        final int[] result = new int[1];
        finish(() -> {
            final CompletableFuture<Integer> f1 = new CompletableFuture<>();
            fib(N, f1);
            asyncAwait(() -> {
                try {
                    result[0] = f1.get();
                } catch (final Exception ex) {
                    throw new RuntimeException(ex);
                }
            }, f1);
        });

        return result[0];
    }

    private static void fib(final int n, final CompletableFuture<Integer> res) {

        if (n <= 0) {
            res.complete(0);
            return;
        } else if (n == 1) {
            res.complete(1);
            return;
        }

        // compute f1 asynchronously
        final CompletableFuture<Integer> f1 = new CompletableFuture<>();
        async(() -> fib(n - 1, f1));

        // compute f2 serially (f1 is done asynchronously).
        final CompletableFuture<Integer> f2 = new CompletableFuture<>();
        fib(n - 2, f2);

        // wait for dependences, before updating the result
        asyncAwait(() -> {
            try {
                res.complete(f1.get() + f2.get());
            } catch (final Exception ex) {
                throw new RuntimeException(ex);
            }
        }, f1, f2);
    }

    @Test
    public void fib1Test() {
        final int actual = kernel(1);
        final int expected = 1;

        assertEquals(expected, actual);
    }

    @Test
    public void fib5Test() {
        final int actual = kernel(5);
        final int expected = 5;

        assertEquals(expected, actual);
    }

    @Test
    public void fib7Test() {
        final int actual = kernel(7);
        final int expected = 13;

        assertEquals(expected, actual);
    }
}
