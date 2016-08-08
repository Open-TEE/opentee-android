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

import android.os.RemoteException;

import fi.aalto.ssg.opentee.ITEEClient;
import fi.aalto.ssg.opentee.exception.CommunicationErrorException;
import fi.aalto.ssg.opentee.exception.TEEClientException;

/**
 * This class implements the ISession interface.
 */
public class OTSession implements ITEEClient.ISession {
    final String TAG = "OTSession";

    int mSessionId;
    OTContextCallback mContextCallback = null;

    public OTSession(int sid, OTContextCallback contextCallback){
        this.mSessionId = sid;
        this.mContextCallback = contextCallback;
    }

    public int getSessionId(){
        return this.mSessionId;
    }

    @Override
    public void invokeCommand(int commandId, ITEEClient.IOperation operation) throws TEEClientException {

        ReturnValueWrapper rv = this.mContextCallback.invokeCommand(mSessionId, commandId, operation);

        if(rv.getReturnCode() != OTReturnCode.TEEC_SUCCESS){
            OTFactoryMethods.throwExceptionWithReturnOrigin(TAG, rv.getReturnCode(), rv.getReturnOrigin());
        }
    }

    @Override
    public void closeSession() throws TEEClientException {
        try {
            this.mContextCallback.closeSession(mSessionId);
        } catch (RemoteException e) {
            throw new CommunicationErrorException(e.getMessage(), ITEEClient.ReturnOriginCode.TEEC_ORIGIN_API);
        }
    }
}
