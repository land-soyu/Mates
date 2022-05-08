/*
 * Copyright (c) 2015, Ericsson AB.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */
package kr.co.netseason.myclebot.UTIL;

import android.os.Handler;
import android.os.HandlerThread;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import kr.co.netseason.myclebot.Logger.Logger;

public class TestsUtils {
    public static class SynchronousCallBuilder {
        private int mTimeout;
        private int mLatchCount;
        private Handler mAsyncHandler;

        private SynchronousCallBuilder(Handler asyncHandler) {
            mTimeout = 25;
            mLatchCount = 1;
            if (asyncHandler != null) {
                mAsyncHandler = asyncHandler;
            } else {
                HandlerThread thread = new HandlerThread("TestUtils-async-thread");
                thread.start();
                mAsyncHandler = new Handler(thread.getLooper());
            }
        }

        public SynchronousCallBuilder latchCount(int count) {
            mLatchCount = count;
            return this;
        }

        public SynchronousCallBuilder run(final SynchronousBlock block) {
            final CountDownLatch latch = new CountDownLatch(mLatchCount);
            block.run(latch);
            waitForLatch(latch, 0);
            return new SynchronousCallBuilder(mAsyncHandler);
        }

        private void waitForLatch(CountDownLatch latch, int extraMilliseconds) {
            try {
                if (!latch.await(extraMilliseconds + mTimeout * 1000, TimeUnit.MILLISECONDS)) {
                    Logger.d("!!!", "synchronous block timed out");
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static SynchronousCallBuilder synchronous() {
        return new SynchronousCallBuilder(null);
    }

    public interface SynchronousBlock {
        void run(CountDownLatch latch);
    }
}
