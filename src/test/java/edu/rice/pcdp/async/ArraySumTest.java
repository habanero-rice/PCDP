package edu.rice.pcdp.async;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static edu.rice.pcdp.PCDP.async;
import static edu.rice.pcdp.PCDP.finish;
import static org.junit.Assert.assertEquals;

/**
 * @author <a href="http://shams.web.rice.edu/">Shams Imam</a> (shams@rice.edu)
 */
@RunWith(JUnit4.class)
public class ArraySumTest {

    @After
    public void tearDown() {

    }

    @Test
    public void sum10Test() {
        final int dataLength = 10;
        performTest(dataLength);
    }

    private void performTest(final int dataLength) {
        final int[] data = new int[dataLength];
        for (int i = 0; i < data.length; i++) {
            data[i] = 2 * i;
        }

        final int actual = kernel(data);
        final int expected = sum(data, 0, data.length);

        assertEquals(expected, actual);
    }

    private static int kernel(final int[] A) {

        final int length = A.length;
        final int mid = A.length / 2;
        final int left = mid / 2;
        final int right = mid + left;

        final int[] res = new int[4];
        finish(() -> {
            async(() -> res[0] = sum(A, 0, left));
            async(() -> res[1] = sum(A, left, mid));
            async(() -> res[2] = sum(A, mid, right));
            res[3] = sum(A, right, length);
        });

        return res[0] + res[1] + res[2] + res[3];
    }

    private static int sum(final int[] A, final int startIndex, final int endIndex) {

        int sum = 0;

        for (int i = startIndex; i < endIndex; i++) {
            sum += A[i];
        }

        return sum;
    }

    @Test
    public void sum100Test() {
        final int dataLength = 100;
        performTest(dataLength);
    }

    @Test
    public void sum1000Test() {
        final int dataLength = 1000;
        performTest(dataLength);
    }
}
