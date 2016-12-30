package edu.rice.pcdp.async;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static edu.rice.pcdp.PCDP.async;
import static edu.rice.pcdp.PCDP.finish;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author vcave
 * @author <a href="http://shams.web.rice.edu/">Shams Imam</a> (shams@rice.edu)
 */
@RunWith(JUnit4.class)
public class TestAsync0 {

    @After
    public void tearDown() {

    }

    /**
     * Simple test that spawns an async, which does nothing interesting. Note: run() is wrapped in a finish by hjTest's
     * execute.
     */
    @Test
    public void testMethod0() {
        final boolean result = new TestAsync0().run0();
        assertEquals(true, result);
    }

    public boolean run0() {
        finish(() -> {
            async(() -> {
                int acc = 0;
            });
        });
        return true;
    }

    /**
     * Test that acc is correctly declared as a local in the inner class that will represent the async Note: run() is
     * wrapped in a finish by hjTest's execute.
     */
    @Test
    public void testMethod1() {

        final boolean result = new TestAsync0().run1();
        assertEquals(true, result);
    }

    public boolean run1() {
        finish(() -> {
            async(() -> {
                int acc = 0;
                for (int j = 0; j < 100; j++) {
                    acc += j;
                }
            });
        });
        return true;
    }

    /**
     * Checks access to final variable works ok.
     */
    @Test
    public void testMethod2() {
        final boolean result = new TestAsync0().run2();
        assertEquals(true, result);
    }

    public boolean run2() {
        final int acc = 1;

        finish(() -> {
            async(() -> {
                int tmp = acc + 1;
            });
        });

        return acc == 1;
    }

    /**
     * Checks access to final array variable works ok.
     */
    @Test
    public void testMethod4() {
        final boolean result = new TestAsync0().run4();
        assertEquals(true, result);
    }

    public boolean run4() {
        final int[] acc = new int[1];

        finish(() -> {
            async(() -> {
                acc[0] = 1;
            });
        });

        return acc[0] == 1;
    }

    /**
     * The implicit finish from the main activity should be able to catch the thrown exception and report it, hence
     * making the test to fail run.
     */
    @Test
    public void testMethod5a() {

        final boolean[] result = {false};
        try {

            result[0] = new TestAsync0().run5a(); // should not reach here
            assertTrue(false);

        } catch (final NullPointerException ex) {

            result[0] = true;
            assertTrue(true);

        }
        assertEquals(true, result[0]);

    }

    public boolean run5a() {

        finish(() -> {
            async(() -> {
                final Object obj = null;
                nullPointerException5a(obj);
            });
        });

        return true;
    }

    protected void nullPointerException5a(final Object obj) {
        obj.toString();
    }

    /**
     * Trying to throw an exception from an async enclosed in a finish not visible in the enclosing lexical scope.
     */
    @Test
    public void testMethod5b() {

        final boolean result = new TestAsync0().run5b();
        assertEquals(true, result);

    }

    public boolean run5b() {

        try {
            finish(() -> {
                nullPointerException5b(null);
            });

            // should not reach here
            assertTrue(false);

        } catch (final NullPointerException ex) {
            assertTrue(true);
        }

        return true;
    }

    protected void nullPointerException5b(final Object obj) {
        async(obj::toString);
    }

    /**
     * Trying to throw an exception from an async enclosed in a finish not visible in the enclosing lexical scope.
     */
    @Test
    public void testMethod5c() {

        final boolean result = new TestAsync0().run5c();
        assertEquals(true, result);

    }

    public boolean run5c() {

        try {
            finish(() -> {
                final String[] strings = {"1", "a", "2", "b", "3", "c"};
                nullPointerException5c(strings);
            });

            // should not reach here
            assertTrue(false);

        } catch (final Exception ex) {
            assertTrue(true);
        }

        return true;
    }

    protected void nullPointerException5c(final String[] strings) {
        for (final String loopStr : strings) {
            async(() -> Integer.parseInt(loopStr));
        }
    }

//    /**
//     * Checks blocking calls inside a non-blocking async.
//     */
//    @Test
//    public void testMethod6() {
//
//        final boolean result = new TestAsync0().run6();
//        assertEquals(true, result);
//    }
//
//    public boolean run6() {
//        final boolean[] result = {false};
//
//        try {
//            finish(() -> {
//                final HjFuture<Integer> f1 = future(() -> {
//                    return computeResult6(6);
//                });
//                final HjFuture<Integer> f2 = future(() -> {
//                    return computeResult6(8);
//                });
//                async(() -> {
//                    final Integer v1 = f1.get();
//                    final Integer v2 = f2.get();
//                });
//            });
//        } catch (final Exception ex) {
//            result[0] = true;
//        }
//
//        return result[0];
//    }
//
//    private int computeResult6(final int v) {
//        // make the futureNb long running
//        for (int i = 0; i < 10_000; i++) {
//            Math.random();
//        }
//        return v;
//    }
}
