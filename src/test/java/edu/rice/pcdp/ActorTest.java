package edu.rice.pcdp;

import junit.framework.TestCase;

import static edu.rice.pcdp.PCDP.finish;

public class ActorTest extends TestCase {

    public void testMonotonic() {
        MonotonicActor actor = new MonotonicActor();
        final int countTo = 1_000_000;

        finish(() -> {
            for (int i = 0; i < countTo; i++) {
                actor.send(i);
            }
        });
        assertEquals(countTo - 1, actor.lastValue);
    }

    public void testChainedMonotonic() {
        MonotonicChainedActor actor = new MonotonicChainedActor();
        final int countTo = 1_000_000;

        finish(() -> {
            for (int i = 0; i < countTo; i++) {
                actor.send(i);
            }
        });
        assertEquals(countTo - 1, actor.lastValue);
        assertEquals(countTo - 1, actor.nextActor.lastValue);
    }

    static class MonotonicActor extends Actor {
        public int lastValue = -1;

        @Override
        public void process(Object msg) {
            Integer val = (Integer)msg;
            assertEquals(lastValue + 1, val.intValue());
            lastValue = val.intValue();
        }
    }

    static class MonotonicChainedActor extends Actor {
        public int lastValue = -1;
        MonotonicActor nextActor = new MonotonicActor();

        @Override
        public void process(Object msg) {
            Integer val = (Integer)msg;
            assertEquals(lastValue + 1, val.intValue());
            lastValue = val.intValue();
            nextActor.send(val);
        }
    }
}
