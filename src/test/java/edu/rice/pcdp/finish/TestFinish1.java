package edu.rice.pcdp.finish;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static edu.rice.pcdp.PCDP.async;
import static edu.rice.pcdp.PCDP.finish;
import static org.junit.Assert.assertEquals;

/**
 * Test simple finish. This test may fails if finish doesn't work properly
 *
 * @author vcave
 */
@RunWith(JUnit4.class)
public class TestFinish1 {

    @After
    public void tearDown() {

    }

    @Test
    public void testMethod() {

        final boolean result = new TestFinish1().run();
        assertEquals(true, result);
    }

    public boolean run() {
        final int[] holder = new int[2];

        finish(() -> {
            async(() -> {
                holder[0] = 1;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            async(() -> {
                holder[1] = 2;
            });
        }); // finish ensures holder is initialized

        return ((holder[0] == 1) && (holder[1] == 2));
    }
}
