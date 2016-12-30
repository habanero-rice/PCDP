package edu.rice.pcdp.finish;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static edu.rice.pcdp.PCDP.async;
import static edu.rice.pcdp.PCDP.finish;
import static org.junit.Assert.assertEquals;

/**
 * Test finish handles exception.
 */
@RunWith(JUnit4.class)
public class TestFinish6a {

    @After
    public void tearDown() {

    }

    @Test
    public void testMethod() {

        final boolean result = new TestFinish6a().run(true);
        assertEquals(true, result);
    }

    public boolean run(final boolean condition) {

        boolean result = false;

        try {
            finish(() -> {
                async(() -> {
                    finish(() -> {
                        async(() -> {
                            if (condition) {
                                throw new RuntimeException("Nasty Exception!");
                            }
                        });
                    });
                });
            });
        } catch (final RuntimeException ex) {
            result = true;
        }

        return result;
    }
}
