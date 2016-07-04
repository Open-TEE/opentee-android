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
package fi.aalto.ssg.opentee.openteeandroid;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import fi.aalto.ssg.opentee.imps.OTSharedMemory;

/**
 * Caller resource container. Each CA will have a caller instance corresponding to it.
 */
public class OTCaller {
    String TAG = "OTCaller";

    int mID;
    Map<Integer, OTSharedMemory> mSharedMemoryList; // <smidInJni, sharedMemory> reuse the OTSharedMemory class.
    Map<Integer, Integer> mSessionList; // <sidInJni, session>

    public OTCaller(int id){
        this.mID = id;
        this.mSharedMemoryList = new HashMap<>();
        this.mSessionList = new HashMap<>();
    }

    public Map<Integer, OTSharedMemory> getSharedMemoryList(){return this.mSharedMemoryList;}
    public Map<Integer, Integer> getSessionList(){return this.mSessionList;}

    public synchronized void addSharedMemory(int smIdInJni, OTSharedMemory sharedMemory){
        if ( sharedMemory != null ){
            Log.d(TAG, this.mID + " added SharedMemory");

            mSharedMemoryList.put(smIdInJni, sharedMemory);
        }
    }

    public synchronized void addSession(int sidInJni, int session){
        mSessionList.put(sidInJni, session);
    }

    /* remove shared memory by its ID in JNI layer */
    public synchronized void removeSharedMemoryBySmIdInJni(int smIdInJni){
        if ( mSharedMemoryList.size() == 0 ) {
            Log.e(TAG, "SharedMemoryList empty, nothing to remove.");
            return;
        }

        mSharedMemoryList.remove(smIdInJni);

        Log.i(TAG, mID + "'s shared memory:" + smIdInJni + " removed.");
    }

    /* remove session by its ID in Java layer */
    public synchronized int removeSessionBySid(int sid){
        for(Map.Entry<Integer, Integer> entry: mSessionList.entrySet()){
            if( entry.getValue() == sid ){
                Log.i(TAG, mID + "'s session:" + sid + " with sidInJni: " + entry.getKey() + " removed.");

                int sidInJni =  entry.getKey();
                mSessionList.remove(entry.getKey());
                return sidInJni;
            }
        }

        Log.i(TAG, mID + "'s session:" + sid + " not found.");
        return -1;
    }

    public synchronized void removeSessionBySidInJni(int sidInJni){
        this.mSessionList.remove(sidInJni);
    }

    /* get the ID of shared memory in JNI layer using its ID in Java layer */
    public synchronized int getSmIdInJniBySmid(int smid){
        for( Map.Entry<Integer, OTSharedMemory> entry: mSharedMemoryList.entrySet() ){
            if( entry.getValue().getId() == smid ) return entry.getKey();
        }

        // not found.
        return -1;
    }

    /* get the ID of shared memory in Java layer using its ID in JNI layer */
    public synchronized int getSmIdBySmIdInJni(int smidInJni){
        return mSharedMemoryList.get(smidInJni).getId();
    }

    /* get session ID in JNI layer using its ID in Java Layer */
    public synchronized int getSidInJniBySid(int sid){
        for( Map.Entry<Integer, Integer> entry: mSessionList.entrySet()){
            if(entry.getValue() == sid) return entry.getKey();
        }

        //not found
        return -1;
    }

    public synchronized int getSidBySidInJni(int sidInJni){
        return mSessionList.get(sidInJni);
    }
}
