package com.ethossoftworks.ethos;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

/**
 * Unit test for Promises
 */
@RunWith(AndroidJUnit4.class)
public class PromiseTest {
    private static final int STATE_FINISHED = 0x00;
    private static final Object lock = new Object();
    private static int nestedAllTestResult = 0;

    private Promise createPromise(final boolean resolve, final Object value) {
        return new Promise(new Promise.Resolver() {
            public void run() {
                try {
                    int range = (500 - 100) + 1;
                    int amount = (int) ((Math.random() * range) + 100);
                    Thread.sleep(amount);
                    if (resolve) {
                        resolve(value);
                    } else {
                        reject(value);
                    }
                } catch (InterruptedException e) {
                    reject("Problem sleeping");
                }
            }
        });
    }


    private void finish(Object[] results, Object expected) {
        synchronized (results) {
            while (results[0] == null) {
                try {
                    results.wait(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (expected instanceof Object[]) {
            assertArrayEquals((Object[]) expected, (Object[]) results[1]);
        } else {
            assertEquals(expected, results[1]);
        }
    }


    @Test
    public void reject() throws Exception {
        final Object[] results = {null, null};
        createPromise(false, null).then(new Promise.Runnable() {
            public Object run(Object result) {
                results[0] = STATE_FINISHED;
                results[1] = false;
                return null;
            }
        }).fail(new Promise.Runnable() {
            public Object run(Object result) {
                results[0] = STATE_FINISHED;
                results[1] = true;
                return null;
            }
        }).exec();
        finish(results, true);
    }


    @Test
    public void rejectThenThen() throws Exception {
        final Object[] results = {null, null};
        createPromise(false, null).fail(new Promise.Runnable() {
            public Object run(Object result) {
                results[1] = 0;
                return null;
            }
        }).then(new Promise.Runnable() {
            public Object run(Object result) {
                results[1] = (int) results[1] + 1;
                return null;
            }
        }).then(new Promise.Runnable() {
            public Object run(Object result) {
                results[0] = STATE_FINISHED;
                results[1] = (int) results[1] + 1;
                return null;
            }
        }).exec();
        finish(results, 2);
    }



    @Test
    public void resolve() throws Exception {
        final Object[] results = {null, null};
        createPromise(true, null).then(new Promise.Runnable() {
            public Object run(Object result) {
                results[0] = STATE_FINISHED;
                results[1] = true;
                return null;
            }
        }).fail(new Promise.Runnable() {
            public Object run(Object result) {
                results[0] = STATE_FINISHED;
                results[1] = false;
                return null;
            }
        }).exec();
        finish(results, true);
    }


    @Test
    public void staticReject() throws Exception {
        final Object[] results = {null, null};
        Promise.reject(false).fail(new Promise.Runnable() {
            public Object run(Object result) {
                results[0] = STATE_FINISHED;
                results[1] = true;
                return null;
            }
        }).exec();
        finish(results, true);
    }


    @Test
    public void staticResolve() throws Exception {
        final Object[] results = {null, null};
        Promise.resolve(true).then(new Promise.Runnable() {
            public Object run(Object result) {
                results[0] = STATE_FINISHED;
                results[1] = true;
                return null;
            }
        }).exec();
        finish(results, true);
    }


    @Test
    public void all() {
        final Object[] results = {null, null};
        ArrayList<Promise> promises = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            promises.add(createPromise(true, i));
        }
        Promise.all(promises).then(new Promise.Runnable() {
            public Object run(Object result) {
                results[0] = STATE_FINISHED;
                results[1] = result;
                return null;
            }
        }).exec();
        finish(results, new Object[] {0,1,2,3,4,5,6,7,8,9});
    }


    @Test
    public void allWithThen() {
        final Object[] results = {null, null};
        ArrayList<Promise> promises = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            promises.add(createPromise(true, i).then(new Promise.Runnable() {
                public Object run(Object result) {
                    return "Inner " + result;
                }
            }));
        }
        Promise.all(promises).then(new Promise.Runnable() {
            public Object run(Object result) {
                results[0] = STATE_FINISHED;
                results[1] = result;
                return null;
            }
        }).exec();
        finish(results, new Object[] {"Inner 0","Inner 1","Inner 2","Inner 3","Inner 4","Inner 5","Inner 6","Inner 7","Inner 8","Inner 9"});
    }


    @Test
    public void allRejected() {
        final Object[] results = {null, null};
        ArrayList<Promise> promises = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            promises.add(createPromise(!(i == 5), i));
        }
        Promise.all(promises).then(new Promise.Runnable() {
            public Object run(Object result) {
                results[0] = STATE_FINISHED;
                results[1] = result;
                return null;
            }
        }).fail(new Promise.Runnable() {
            public Object run(Object result) {
                results[0] = STATE_FINISHED;
                results[1] = result;
                return null;
            }
        }).exec();
        finish(results, 5);
    }


    @Test
    public void allWithNoPromises() {
        final Object[] results = {null, null};
        Promise.all(new ArrayList<Promise>()).then(new Promise.Runnable() {
            @Override
            public Object run(Object result) {
                results[0] = STATE_FINISHED;
                results[1] = true;
                return null;
            }
        }).exec();
        finish(results, true);
    }


    @Test
    public void promiseWithInnerThread() {
        final Object[] results = {null, null};
        new Promise(new Promise.Resolver() {
            public void run() {
                new Thread() {
                    public void run() {
                        try {
                            Thread.sleep(500);
                            resolve(true);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            reject(false);
                        }
                    }
                }.start();
            }
        }).then(new Promise.Runnable() {
            public Object run(Object result) {
                results[0] = STATE_FINISHED;
                results[1] = result;
                return null;
            }
        }).exec();
        finish(results, true);
    }


    @Test
    public void nestedAllTest() {
        final Object[] results = {null, null};
        nestedAllTestInnerPromise(1).then(new Promise.Runnable() {
            public Object run(Object result) {
                synchronized (lock) {
                    nestedAllTestResult += 10;
                }
                results[0] = STATE_FINISHED;
                results[1] = nestedAllTestResult;
                return null;
            }
        }).exec();
        finish(results, 59);
    }


    private Promise nestedAllTestInnerPromise(final int num) {
        Promise promise = new Promise(new Promise.Resolver() {
            public void run() {
                synchronized (lock) {
                    nestedAllTestResult += num;
                }
                resolve(num);
            }
        });
        if (num <= 3) {
            promise.then(new Promise.Runnable() {
                public Object run(Object result) {
                    ArrayList<Promise> promises = new ArrayList<>();
                    promises.add(nestedAllTestInnerPromise(num + 1));
                    promises.add(nestedAllTestInnerPromise(num + 1));
                    return Promise.all(promises);
                }
            });
        }
        return promise;
    }
}