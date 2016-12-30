package edu.rice.pcdp.future;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static edu.rice.pcdp.PCDP.finish;
import static edu.rice.pcdp.PCDP.future;
import static org.junit.Assert.assertEquals;

/**
 * Test explicit calls to force on async<void>.
 *
 * @author vcave
 */
@RunWith(JUnit4.class)
public class TestFuture4 {

    @Test
    public void testMethod() {

        final boolean[] result = {false};
        result[0] = new TestFuture4().run();
        assertEquals(true, result[0]);
    }

    public boolean run() {

        final boolean[] result = {false};

        finish(() -> {
            final Future<Void> b = future(() -> {
                throw new IllegalStateException("Nasty exception!");
            });
            try {
                b.get();
            } catch (final ExecutionException ex) {
                result[0] = ex.getCause() instanceof IllegalStateException;
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        });

        return result[0];
    }
}
