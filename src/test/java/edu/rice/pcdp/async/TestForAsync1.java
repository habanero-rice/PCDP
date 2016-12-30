package edu.rice.pcdp.async;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.concurrent.atomic.AtomicInteger;

import static edu.rice.pcdp.PCDP.finish;
import static edu.rice.pcdp.PCDP.forasync;
import static org.junit.Assert.assertEquals;

/**
 * Test that acc is correctly declared as a local in the inner class that will represent the async Note: run() is
 * wrapped in a finish by hjTest's execute.
 *
 * @author vcave
 */
@RunWith(JUnit4.class)
public class TestForAsync1 {

    @After
    public void tearDown() {

    }

    @Test
    public void testMethod() {

        final boolean result = new TestForAsync1().run();
        assertEquals(true, result);
    }

    public boolean run() {
        final int N = 100;
        final AtomicInteger ai = new AtomicInteger(0);
        int res = 0;

        // anonymous region
        finish(() -> {
            forasync(0, N - 1, ai::addAndGet);
        });

        for (int i = 0; i < N; i++) {
            res += i;
        }

        return (ai.get() == res);
    }
}
