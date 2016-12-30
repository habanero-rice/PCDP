package edu.rice.pcdp.finish;

import edu.rice.pcdp.runtime.MultiException;
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
public class TestFinish7Exceptions {

    @After
    public void tearDown() {

    }

    @Test
    public void testMethodRuntimeException() {

        boolean result = false;
        try {
            finish(() -> {
                throw new RuntimeException("test exception 1");
            });
        } catch (final RuntimeException e) {
            result = true;
        }
        assertEquals("No exception thrown!", true, result);
    }

    @Test
    public void testMethodNullPointerException() {

        boolean result = false;
        try {
            finish(() -> {
                throw new NullPointerException("test exception 1");
            });
        } catch (final NullPointerException e) {
            result = true;
        }
        assertEquals("No exception thrown!", true, result);
    }

    @Test
    public void testMethodMultipleNullPointerException() {

        boolean result = false;
        try {
            finish(() -> {
                async(() -> {
                    throw new NullPointerException("test exception 1");
                });
                async(() -> {
                    throw new NullPointerException("test exception 2");
                });
            });
        } catch (final MultiException e) {
            result = true;
        }
        assertEquals("No exception thrown!", true, result);
    }

    @Test
    public void testMethodNullPointerException2() {
        boolean result = false;
        try {
            finish(() -> {
                finish(() -> {
                    throw new NullPointerException("test exception 2");
                });
            });
        } catch (final NullPointerException e) {
            result = true;
        }
        assertEquals("No exception thrown!", true, result);
    }

    @Test
    public void testMethodNullPointerException3() {
        boolean result = false;
        try {
            finish(() -> {
                finish(() -> {
                    async(() -> {
                        throw new NullPointerException("test exception 2");
                    });
                });
            });
        } catch (final NullPointerException e) {
            result = true;
        }
        assertEquals("No exception thrown!", true, result);
    }

    @Test
    public void testMethodNullPointerException4() {
        boolean result = false;
        try {
            finish(() -> {
                finish(() -> {
                    async(() -> {
                        throw new NullPointerException("test exception 2");
                    });
                });
                finish(() -> {
                    async(() -> {
                        throw new NullPointerException("test exception 3");
                    });
                });
                finish(() -> {
                    async(() -> {
                        throw new NullPointerException("test exception 4");
                    });
                });
            });
        } catch (final NullPointerException e) {
            result = true;
        }
        assertEquals("No exception thrown!", true, result);
    }

}
