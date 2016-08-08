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

import android.content.Context;

/**
 * This class implements the Runnable interface. It will release the lock object once connected to
 * remote service.
 */
public class ServiceGetterThread implements Runnable {
    ProxyApis mProxyApis = null;
    String mTeeName = null;
    Context mContext = null;
    OTLock mLock = null;

    public ServiceGetterThread(String teeName, Context context, OTLock lock){
        this.mTeeName = teeName;
        this.mContext = context;
        this.mLock = lock;
    }

    @Override
    public void run(){
        this.mLock.lock();
        mProxyApis = new ProxyApis(this.mTeeName, this.mContext, this.mLock);
    }

    public ProxyApis getProxyApis(){return this.mProxyApis;}

}
