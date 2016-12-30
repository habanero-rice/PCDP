package edu.rice.pcdp.finish;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static edu.rice.pcdp.PCDP.finish;
import static org.junit.Assert.assertEquals;

/**
 * Exhibit code generation problems related to statement generated for a finish construct. Could ends-up in a
 * verification error.
 *
 * @author vcave
 */
@RunWith(JUnit4.class)
public class TestFinish3 {

    @After
    public void tearDown() {

    }

    @Test
    public void testMethod() {

        final boolean result = new TestFinish3().run();
        assertEquals(true, result);
    }

    public boolean run() {
        final double[] d1 = {0.0d};
        final double[] d2 = {0.0d};

        finish(() -> {
            d1[0] = 2.0d;
        });

        finish(() -> {
            d2[0] = 2.0d;
        });

        return Double.compare(d1[0], d2[0]) == 0;
    }
}
