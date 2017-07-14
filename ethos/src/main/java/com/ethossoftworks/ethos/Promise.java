package com.ethossoftworks.ethos;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Copyright (c) 2016 Ryan Mitchener
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * ----------------------------------------------------------------------------
 *
 * Notes:
 * Promise class for executing tasks on a separate thread and waiting for a callback (either success or failure).
 * All callbacks happen on the main thread by default.
 *
 * You may specify a specific Looper to send messages on, but you must manage the Looper yourself.
 * As Promise uses handlers, any thread that has an already prepared Looper will need to call
 * Looper.loop() on the looper attached to the thread. Any thread that does not call Looper.loop()
 * will not receive the .then() callbacks
 * ----------------------------------------------------------------------------
 *
 * Example:
 * new Promise(new Promise.Resolver() {
 *     public void run() {
 *         // Do some work and then resolve or reject(false).
 *         // Rejecting will skip to the nearest fail() block.
 *         // Returning after resolve or reject is recommended
 *         // Whatever is passed into resolve or reject will be passed into the corresponging
 *         // then() or fail() blocks
 *         resolve(true);
 *         return;
 *     }
 * }).then(new Promise.Runnable() {
 *     public Object run(Object result) {
 *         // The "result" variable contains the result from the previous block.
 *         // Whatever is returned here is passed to the next corresponding block. If everything
 *         // is successful, all fail() blocks will be skipped until the next then() block.
 *         // If a promise is returned here, the next block will not execute until the promise
 *         // has finished executing
 *         ArrayList<Promise> promises = new ArrayList<>();
 *         promises.add(getThing());
 *         promises.add(getThing2());
 *         return Promise.all(promises);
 *     }
 * }).fail(new Promise.Runnable() {
 *     public Object run(Object result) {
 *         return null;
 *     }
 * }).then(new Promise.Runnable() {
 *     public Object run(Object result) {
 *         // This then() block will run even if the previous fail() was run.
 *     }
 * }).exec(); // Begin to execute the chain
 */
public class Promise {
    public static final int STATE_PENDING = 0;
    public static final int STATE_FULFILLED = 1;
    public static final int STATE_REJECTED = 2;

    private static final int TYPE_FULFILL = 0x10;
    private static final int TYPE_REJECT = 0x11;

    private Resolver mResolver = null;
    private final ArrayList<ChainEntry> mChain = new ArrayList<>();
    private int state = STATE_PENDING;
    private Object value = null;
    private ArrayList<Promise> mChildren = null;
    private AllChainProgress mAllChainProgress = null;
    private Looper mLooper;


    // Constructor
    public Promise(Resolver resolver) {
        mResolver = resolver;
    }


    // Constructor
    private Promise() {}


    // Constructor for Promise.all()
    private Promise(ArrayList<Promise> promises) {
        mChildren = promises;
    }


    // Add a success callback to the promise chain
    public Promise then(Runnable onFulfill) {
        mChain.add(new ChainEntry(TYPE_FULFILL, onFulfill));
        return this;
    }


    // Add a failure callback to the promise chain
    public Promise fail(Runnable onFail) {
        mChain.add(new ChainEntry(TYPE_REJECT, onFail));
        return this;
    }


    // Return a resolved Promise
    public static Promise resolve(final Object value) {
        Promise promise = new Promise();
        promise.state = STATE_FULFILLED;
        promise.value = value;
        return promise;
    }


    // Return a rejected promise
    public static Promise reject(Object reason) {
        Promise promise = new Promise();
        promise.state = STATE_REJECTED;
        promise.value = reason;
        return promise;
    }

    // Return a promise that waits until the completion of all passed in promises to run its callbacks
    public static Promise all(ArrayList<Promise> promises) {
        return new Promise(promises);
    }

    public static Promise all(Promise... promises) {
        return new Promise(new ArrayList<>(Arrays.asList(promises)));
    }


    // Execute the promise
    public void exec() {
        exec(Looper.getMainLooper());
    }


    // Execute the promise with a specified looper for then() and fail()
    public void exec(Looper looper) {
        mLooper = looper;
        if (mChildren != null) {
            _resolveAll();
        } else {
            _resolve();
        }
    }


    private void _resolveAll() {
        final AllChainProgress progress = new AllChainProgress(mChildren.size());
        final Object[] results = new Object[mChildren.size()];
        int count = 0;

        // Handle if Promise.all() was called with no Promises
        if (mChildren.size() == 0) {
            Promise.this.value = results;
            Promise.this.state = STATE_FULFILLED;
            Promise.this._resolve();
            return;
        }

        for (final Promise promise : mChildren) {
            promise.mAllChainProgress = progress;
            Runnable runnable = new AllRunnable(count) {
                public Object run(Object result) {
                    progress.markTaskComplete();
                    if (!progress.isRejected()) {
                        results[resultIndex] = promise.value;
                    }
                    if (promise.state == STATE_REJECTED || progress.isComplete()) {
                        if (promise.state == STATE_REJECTED) {
                            progress.markRejected();
                            Promise.this.value = promise.value;
                            Promise.this.state = STATE_REJECTED;
                        } else {
                            Promise.this.value = results;
                            Promise.this.state = STATE_FULFILLED;
                        }
                        Promise.this._resolve();
                    }
                    return null;
                }
            };
            if (!progress.isComplete() && !progress.isRejected()) {
                promise.then(runnable).fail(runnable).exec();
            }
            count++;
        }
    }


    // Execute all the resolvers on separate threads
    private void _resolve() {
        // Skip creating a new thread if the state is already resolved (this only happens with Promise.reject() and Promise.resolve()
        if (this.state != STATE_PENDING) {
            _nextChainItem();
            return;
        }

        // Create the promise handler for the resolver
        mResolver.handler = new PromiseHandler(Promise.this, mLooper);

        // Execute the resolver
        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... params) {
                if (mAllChainProgress != null && mAllChainProgress.isRejected()) {
                    return null;
                }

                // Execute the resolver
                mResolver.run();
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    // Go to the next item in the chain based on the Promise's state
    private void _nextChainItem() {
        int nextType = (this.state == STATE_FULFILLED) ? TYPE_FULFILL : TYPE_REJECT;
        while (mChain.size() > 0) {
            ChainEntry entry = mChain.remove(0);
            if (entry.type != nextType) {
                continue;
            }
            this.value = entry.runnable.run(this.value);
            if (this.value instanceof Promise) {
                final Promise promise = (Promise) this.value;
                Runnable runnable = new Runnable("_inner promise resolver") {
                    public Object run(Object result) {
                        Promise.this.value = result;
                        Promise.this.state = promise.state;
                        Promise.this._nextChainItem();
                        return null;
                    }
                };
                promise.then(runnable).fail(runnable).exec();
            } else {
                // Reset the state to fulfill so we can continue to another then() after a fail()
                this.state = STATE_FULFILLED;
                _nextChainItem();
            }
            break;
        }
    }



    /**
     * Inner Classes
     * ------------------------------------------------------------------------
     */

    // Runnable used for Promise.all()
    private static class AllRunnable extends Promise.Runnable {
        int resultIndex;

        public AllRunnable(int resultIndex) {
            this.resultIndex = resultIndex;
        }

        public Object run(Object result) { return null; }
    }


    // Handles messages from the resolver that are sent from resolve() and reject()
    private static class PromiseHandler extends Handler {
        Promise promise;

        protected PromiseHandler(Promise promise, Looper looper) {
            super(looper);
            this.promise = promise;
        }

        public void handleMessage(Message msg) {
            Resolver resolver = (Resolver) msg.obj;
            promise.state = resolver.state;
            promise.value = resolver.value;
            promise._nextChainItem();
        }
    }


    // This is used to help determine the type of entry in the promise chain
    private static class ChainEntry {
        int type;
        Runnable runnable;

        ChainEntry(int type, Runnable runnable) {
            this.type = type;
            this.runnable = runnable;
        }
    }


    // Checks progress if multiple resolvers were passed through Promise.all()
    private static class AllChainProgress {
        private final Object mCountLock = new Object();
        private final Object mRejectedLock = new Object();
        int count;
        boolean rejected = false;

        AllChainProgress(int count) {
            this.count = count;
        }

        void markTaskComplete() {
            synchronized (mCountLock) {
                count--;
            }
        }

        void markRejected() {
            synchronized (mRejectedLock) {
                rejected = true;
            }
        }

        boolean isRejected() {
            synchronized (mRejectedLock) {
                return rejected;
            }
        }

        boolean isComplete() {
            if (isRejected()) {
                return true;
            }
            synchronized (mCountLock) {
                return (count <= 0);
            }
        }
    }


    // Runnable class for all then() and fail() methods
    public abstract static class Runnable {
        private String id;

        public Runnable(){}

        public Runnable(String id) {
            this.id = id;
        }

        public abstract Object run(Object result);
    }


    // Resolver class for Promise instantiation
    public abstract static class Resolver {
        private int state = STATE_PENDING;
        private Object value = null;
        private PromiseHandler handler;

        public abstract void run();

        public void resolve(Object value) {
            this.state = STATE_FULFILLED;
            this.value = value;
            _dispatchMessage();
        }

        public void reject(Object reason) {
            this.state = STATE_REJECTED;
            this.value = reason;
            _dispatchMessage();
        }

        private void _dispatchMessage() {
            Message msg = handler.obtainMessage();
            msg.obj = this;
            handler.sendMessage(msg);
        }
    }
}