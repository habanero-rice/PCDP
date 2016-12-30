package edu.rice.pcdp.await;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.concurrent.CompletableFuture;

import static edu.rice.pcdp.PCDP.async;
import static edu.rice.pcdp.PCDP.asyncAwait;
import static edu.rice.pcdp.PCDP.finish;
import static org.junit.Assert.assertEquals;

/**
 * Simple test that spawns an async await, which does nothing interesting. It waits for two futures to be resolved by
 * two other asyncs.
 *
 * @author vcave
 */
@RunWith(JUnit4.class)
public class TestAwait1c {

    @Test
    public void testMethod() {

        final boolean[] result = {false};
        result[0] = new TestAwait1c().run();
        assertEquals(true, result[0]);
    }

    public boolean run() {

        finish(() -> {
            final CompletableFuture<Integer> f1 = new CompletableFuture<>();
            final CompletableFuture<Integer> f2 = new CompletableFuture<>();

            asyncAwait(() -> {
                Math.random();
            }, f1, f2);

            async(() -> {
                f1.complete(1);
            });
            async(() -> {
                f2.complete(2);
            });
        });

        return true;
    }
}
