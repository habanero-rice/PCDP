package edu.rice.pcdp.future;

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
public class TestFuture0 {

    @Test
    public void testMethod() {

        final boolean[] result = {false};
        result[0] = new TestFuture0().run();
        assertEquals(true, result[0]);
    }

    public boolean run() {
        final int CONSTANT = 10;
        final boolean[] res = {false};

        finish(() -> {
            final Future<Integer> f = future(() -> {
                return CONSTANT;
            });

            try {
                final int y = f.get();
                res[0] = f.isDone() && (y == CONSTANT);
            } catch (final Exception ex) {
                ex.printStackTrace();
                res[0] = false;
            }
        });

        return res[0];
    }
}
