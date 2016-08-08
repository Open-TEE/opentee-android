/*
 * Copyright (c ) 2016 Aalto University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fi.aalto.ssg.opentee.imps;

import android.util.Log;

/**
 * Lock for multi-threading.
 */
public class OTLock{
    final String TAG = "OTLock";
    private boolean locked = false;

    public synchronized void lock(){
        Log.i(TAG, "lock in " + Thread.currentThread().getId());

        try {
            while(locked) {
                Log.d(TAG, "waiting to get lock...");
                wait();
            }
            locked = true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized void unlock(){
        Log.i(TAG, "unlock" + Thread.currentThread().getId());

        locked = false;
        notify();
    }
}
