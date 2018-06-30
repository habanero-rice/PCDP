package edu.rice.pcdp.finish;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static edu.rice.pcdp.PCDP.async;
import static edu.rice.pcdp.PCDP.finish;

/**
 * There are no  dead lock and blocked workers
 * when execution task haves quantity of finish
 * statements grater then quantity of workers.
 *
 * This test does not proof correct invocation
 * in concurrency environment.
 *
 * @author BadPitt
 */
@RunWith(JUnit4.class)
public class TestFinish9TooMoreFinish {

    private static final int ARRAY_LENGTH = 10_000;
    private static final int[] ARRAY = new int[ARRAY_LENGTH];

    @BeforeClass
    public static void setUp() {
        for (int i = 0; i < ARRAY.length; i++) {
            ARRAY[i] = i+1;
        }
    }

    @Test
    public void nestedFinishTest() {
        final int dataLength = Runtime.getRuntime().availableProcessors() * 2;
        final int[] parResult =  new int[1];

        finish(() -> {
            parResult[0] = sum(dataLength, 0, 0, ARRAY.length);
        });
        System.out.printf("Parallel result is: %d\n", parResult[0]);

        int seqResult =  0;
        for (int i = 0; i < ARRAY.length; i++) {
            seqResult += ARRAY[i];
        }
        System.out.printf("Sequencial result is: %d", seqResult);
    }

    private int sum(final int dataLength,
                    final int depth,
                    final int left,
                    final int right) {
        // Base case
        if (depth >= dataLength) {
            int result =  0;
            for (int i = left; i < right; i++) {
                result += ARRAY[i];
            }
            return result;
        }
        // Normal case
        // we don't need additional synchronization like atomic, volatile and etc.
        final int[] finishResult = new int[]{0};
        final int[] firstResult = new int[]{0};
        final int[] secondResult = new int[]{0};
        finish(() -> {
            async(() -> {
                firstResult[0] = sum(dataLength, depth + 1, left, (right+left)/2);
            });
            async(() -> {
                secondResult[0] = sum(dataLength, depth + 1, (right+left)/2, right);
            });
        });
        finishResult[0] = firstResult[0] + secondResult[0];

        return finishResult[0];
    }
}
