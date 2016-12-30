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
 * @author <a href="http://shams.web.rice.edu/">Shams Imam</a> (shams@rice.edu)
 */
@RunWith(JUnit4.class)
public class FibFutureTest {

    private static int kernel(final int N) {

        final int[] result = new int[1];
        finish(() -> {
            try {
                result[0] = fib(N);
            } catch (final Exception ex) {
                ex.printStackTrace();
                result[0] = -1;
            }
        });

        return result[0];
    }

    private static int fib(final int n) throws ExecutionException, InterruptedException {

        if (n <= 0) {
            return 0;
        } else if (n == 1) {
            return 1;
        }

        final Future<Integer> f1 = future(() -> fib(n - 1));
        final Future<Integer> f2 = future(() -> fib(n - 2));

        final int result = f1.get() + f2.get();
        return result;
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
