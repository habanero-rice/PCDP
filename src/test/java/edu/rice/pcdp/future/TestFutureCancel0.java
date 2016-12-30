package edu.rice.pcdp.future;

import edu.rice.pcdp.config.SystemProperty;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.concurrent.Future;

import static edu.rice.pcdp.PCDP.finish;
import static edu.rice.pcdp.PCDP.future;
import static org.junit.Assert.assertEquals;

/**
 * Test explicit calls to force.
 *
 * @author vcave
 */
@RunWith(JUnit4.class)
public class TestFutureCancel0 {

    @Test
    public void testMethod() {

        SystemProperty.numWorkers.setProperty("2");
        final boolean[] result = {false};
        finish(() -> {
            result[0] = new TestFutureCancel0().run();
        });
        assertEquals(true, result[0]);
    }

    public boolean run() {
        final int CONSTANT = 10_000;
        final boolean[] res = {true};

        final int[] cancelCount = {0};
        final boolean[] futureStates = new boolean[CONSTANT];
        final Future[] futures = new Future[CONSTANT];

        finish(() -> {

            for (int i = 0; i < CONSTANT; i++) {
                futures[i] = future(() -> {
                    for (int j = 0; j < 1_000; j++) {
                        Math.random();
                    }
                    return CONSTANT;
                });
            }

            for (int i = 0; i < CONSTANT; i++) {
                final boolean cancel = futures[i].cancel(true);
                futureStates[i] = cancel;
                cancelCount[0] += (cancel ? 1 : 0);
            }
        });

        for (int i = 0; i < CONSTANT; i++) {
            if (futureStates[i]) {
                res[0] = res[0] && futures[i].isCancelled();
            }
            if (!res[0]) {
                break;
            }
        }

        return res[0] && cancelCount[0] > 0;
    }
}
