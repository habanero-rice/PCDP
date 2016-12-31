package edu.rice.pcdp.async;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.concurrent.atomic.AtomicInteger;

import static edu.rice.pcdp.PCDP.finish;
import static edu.rice.pcdp.PCDP.forasync;
import static edu.rice.pcdp.PCDP.forall;
import static edu.rice.pcdp.PCDP.forallChunked;
import static edu.rice.pcdp.PCDP.forall2d;
import static edu.rice.pcdp.PCDP.forall2dChunked;
import static org.junit.Assert.assertEquals;

/**
 * Test that acc is correctly declared as a local in the inner class that will represent the async Note: run() is
 * wrapped in a finish by hjTest's execute.
 *
 * @author vcave
 */
@RunWith(JUnit4.class)
public class TestForall {

    @After
    public void tearDown() {

    }

    @Test
    public void test2D() {
        driver2d(0, 10, 0, 10);
        driver2d(0, 1, 0, 1);
        driver2d(0, 7, 0, 7);
        driver2d(0, 7, 0, 14);
        driver2d(0, 14, 0, 7);
        driver2d(3, 14, 3, 7);
        driver2d(3, 14, 6, 7);
        driver2d(80, 100, 191, 200);
    }

    @Test
    public void test1D() {
        driver1d(0, 10);
        driver1d(0, 1);
        driver1d(0, 7);
        driver1d(3, 7);
        driver1d(3, 13);
        driver1d(3, 14);
        driver1d(13, 100);
    }

    private void clearArray2d(AtomicInteger[][] arr) {
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                arr[i][j] = new AtomicInteger(0);
            }
        }
    }

    private void clearArray1d(AtomicInteger[] arr) {
        for (int i = 0; i < arr.length; i++) {
            arr[i] = new AtomicInteger(0);
        }
    }

    private void verifyArray2d(int start0, int end0, int start1, int end1,
            AtomicInteger[][] arr, int chunk) {
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                final int expectedValue;
                if (i >= start0 && i <= end0 && j >= start1 && j <= end1) {
                    expectedValue = 1;
                } else {
                    expectedValue = 0;
                }
                assertEquals("(" + i + ", " + j + ") for array (" + start0 +
                        "->" + end0 + ", " + start1 + "->"+ end1 +
                        ") with chunk = " + chunk, expectedValue,
                        arr[i][j].get());
            }
        }
    }

    private void verifyArray1d(int start0, int end0, AtomicInteger[] arr,
            int chunk) {
        for (int i = 0; i < arr.length; i++) {
            final int expectedValue;
            if (i >= start0 && i <= end0) {
                expectedValue = 1;
            } else {
                expectedValue = 0;
            }
            assertEquals("(" + i + ") for array (" + start0 + "->" + end0 +
                    ") with chunk = " + chunk, expectedValue, arr[i].get());
        }
    }

    /*
     * Test 2d variants of forall to make sure they all produce correct results.
     */
    private void driver2d(final int start0, final int end0, final int start1,
            final int end1) {
        int xDim = 2 * end0;
        int yDim = 2 * end1;
        final AtomicInteger[][] arr = new AtomicInteger[xDim][yDim];

        clearArray2d(arr);
        forall2d(start0, end0, start1, end1, (i, j) -> {
            arr[i][j].incrementAndGet();
        });
        verifyArray2d(start0, end0, start1, end1, arr, -1);

        final int maxChunkSize = (end0 - start0 + 1) * (end1 - start1 + 1);
        for (int chunk = 1; chunk <= maxChunkSize; chunk++) {
            clearArray2d(arr);
            forall2dChunked(start0, end0, start1, end1, chunk, (i, j) -> {
                arr[i][j].incrementAndGet();
            });
            verifyArray2d(start0, end0, start1, end1, arr, chunk);
        }
    }

    /*
     * Test 1d variants of forall to make sure they all produce correct results.
     */
    private void driver1d(final int start, final int end) {
        int dim = 2 * end;
        final AtomicInteger[] arr = new AtomicInteger[dim];

        clearArray1d(arr);
        forall(start, end, (i) -> {
            arr[i].incrementAndGet();
        });
        verifyArray1d(start, end, arr, -1);

        final int maxChunkSize = (end - start + 1);
        for (int chunk = 1; chunk <= maxChunkSize; chunk++) {
            clearArray1d(arr);
            forallChunked(start, end, chunk, (i) -> {
                arr[i].incrementAndGet();
            });
            verifyArray1d(start, end, arr, chunk);
        }
    }

}
