package edu.rice.pcdp.future;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.concurrent.Future;

import static edu.rice.pcdp.PCDP.async;
import static edu.rice.pcdp.PCDP.finish;
import static edu.rice.pcdp.PCDP.future;
import static org.junit.Assert.assertEquals;

/**
 * Test explicit calls to force on async<void>.
 *
 * @author vcave
 */
@RunWith(JUnit4.class)
public class TestFuture1 {

    @Test
    public void testMethod() {

        final boolean[] result = {false};
        finish(() -> {
            result[0] = new TestFuture1().run();
        });
        assertEquals(true, result[0]);
    }

    public boolean run() {

        final Future<Void> b = future(() -> {
            Math.random();
            return null;
        });

        final Future<Void> c = future(() -> {
            Math.random();
            return null;
        });

        finish(() -> {
            async(() -> {
                try {
                    b.get();
                    c.get();
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            });
            try {
                c.get();
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        });

        return true;
    }
}
