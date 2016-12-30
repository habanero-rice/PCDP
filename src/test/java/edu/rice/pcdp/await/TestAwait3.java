package edu.rice.pcdp.await;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.concurrent.CompletableFuture;

import static edu.rice.pcdp.PCDP.asyncAwait;
import static edu.rice.pcdp.PCDP.finish;
import static org.junit.Assert.assertEquals;

/**
 * Simple test that spawns an async await, which does nothing interesting.
 *
 * @author vcave
 */
@RunWith(JUnit4.class)
public class TestAwait3 {

    @Test
    public void testMethod() {
        final boolean[] result = {false};
        result[0] = new TestAwait3().run();
        assertEquals(true, result[0]);
    }

    public boolean run() {

        final int CONSTANT = 3;
        final int[] value = {0};

        Integer i = 3;
        final CompletableFuture<Integer> ddf = new CompletableFuture<>();

        finish(() -> {
            asyncAwait(() -> {
                final Integer v = ddf.getNow(0);
                value[0] = v;
            }, ddf);

            ddf.complete(i);
        });

        return value[0] == CONSTANT;
    }
}
