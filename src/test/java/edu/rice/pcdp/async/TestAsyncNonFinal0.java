package edu.rice.pcdp.async;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static edu.rice.pcdp.PCDP.async;
import static edu.rice.pcdp.PCDP.finish;
import static org.junit.Assert.assertEquals;

/**
 * final keyword restriction removal. Checks asyncs are able to access variables from parent scopes.
 *
 * @author vcave
 */
@RunWith(JUnit4.class)
public class TestAsyncNonFinal0 {

    private boolean res = false;

    @After
    public void tearDown() {

    }

    @Test
    public void testMethod() {

        final boolean result = new TestAsyncNonFinal0().run();
        assertEquals(true, result);
    }

    public boolean run() {
        finish(() -> {
            final int acc0 = 1;
            async(() -> {
                int acc1 = 2;
                async(() -> {
                    int acc2 = 3;
                    async(() -> {
                        int acc3 = acc0 + acc1 + acc2;
                        res = (acc3 == 6);
                    });
                });
            });
        });
        return res;
    }
}
