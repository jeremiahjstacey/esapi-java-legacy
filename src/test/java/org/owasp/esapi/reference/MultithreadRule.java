package org.owasp.esapi.reference;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;

public class MultithreadRule implements TestRule {
    private static final String CONCURRENCY_PROP = "MultithreadRule.concurrency";
    private final String CONCURRENCY_SETTING = System.getProperty(CONCURRENCY_PROP, "1");
    private List<Throwable> errors = new ArrayList<Throwable>();

    private final int threadCount;
    public MultithreadRule() {
        this.threadCount = Integer.parseInt(CONCURRENCY_SETTING);
    }
    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                final CountDownLatch latch = new CountDownLatch(1);
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            while(!latch.await(100, TimeUnit.MILLISECONDS)) {
                                //Active wait for latch to be released
                            }
                            System.out.println("Running Test");
                            base.evaluate();
                        } catch (Throwable e) {
                            errors.add(e);
                        }
                    }
                };

                List<Thread> threads = new ArrayList<>();
                for (int count = 0 ; count < threadCount; count ++) {
                    Thread thread = new Thread(r, "MultiThreadRule " + count);
                    thread.start();
                    threads.add(thread);
                }

                latch.countDown();

                for (Thread th : threads) {
                    th.join();
                }
                MultipleFailureException.assertEmpty(errors);
            }
        };
    }

}
