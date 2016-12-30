package edu.rice.pcdp.finish;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static edu.rice.pcdp.PCDP.finish;
import static org.junit.Assert.assertEquals;

/**
 * Triggers a bug when complex pass is activated soot eliminates some variable whereas it shouldn't1
 *
 * @author vcave
 */
@RunWith(JUnit4.class)
public class TestFinish5 {

    @After
    public void tearDown() {

    }

    @Test
    public void testMethod() {

        final boolean result = new TestFinish5().run();
        assertEquals(true, result);
    }

    public boolean run() {
        final int[] x = {0};
        finish(() -> {
            x[0] = 3;
        });
        return !((x[0] == 3) && (x[0] == 2));
    }
}
