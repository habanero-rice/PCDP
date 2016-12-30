package edu.rice.pcdp.async;

import edu.rice.pcdp.config.SystemProperty;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static edu.rice.pcdp.PCDP.async;
import static edu.rice.pcdp.PCDP.finish;

/**
 * @author <a href="http://shams.web.rice.edu/">Shams Imam</a> (shams@rice.edu)
 */
@RunWith(JUnit4.class)
public class NestedAsyncFinishTest {

    private static void fubar(final int dataLength, final int node, final int received, final int index, final int depth) {
        // Base case
        if (index >= dataLength) {
            return;
        }
        // Normal case
        finish(() -> {
            async(() -> {
                fubar(dataLength, node + 1, received, index * 2, depth + 1);
            });
            fubar(dataLength, node, received, index * 2 + 1, depth + 1);
        });
    }

    @After
    public void tearDown() {

    }

    @Test
    public void nestedFinishTest() {
        final int dataLength = 8;
        kernelBody(dataLength);
    }

    protected void kernelBody(final int dataLength) {

        finish(() -> {
            fubar(dataLength, 0, 1, 1, 0);
        });
    }
}
