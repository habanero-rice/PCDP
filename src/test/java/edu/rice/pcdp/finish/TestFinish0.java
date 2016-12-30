package edu.rice.pcdp.finish;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static edu.rice.pcdp.PCDP.finish;
import static org.junit.Assert.assertEquals;

/**
 * Test simple finish that doesn't involve any asyncs
 *
 * @author vcave
 */
@RunWith(JUnit4.class)
public class TestFinish0 {

    @After
    public void tearDown() {

    }

    @Test
    public void testMethod() {

        final boolean result = new TestFinish0().run();
        assertEquals(true, result);
    }

    public boolean run() {
        finish(() -> {
            int acc = 0;
            Math.random();
        });
        return true;
    }
}
