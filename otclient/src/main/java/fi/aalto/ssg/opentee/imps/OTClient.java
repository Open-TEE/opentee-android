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
import android.os.RemoteException;

import fi.aalto.ssg.opentee.ITEEClient;
import fi.aalto.ssg.opentee.exception.BadParametersException;
import fi.aalto.ssg.opentee.exception.TEEClientException;

/**
 * This class implements the ITEEClient interface.
 */
public class OTClient implements ITEEClient {
    @Override
    public IOperation newOperation() {
        return new OTOperation(0);
    }

    @Override
    public IOperation newOperation(IParameter firstParam) {
        return new OTOperation(0, firstParam);
    }

    @Override
    public IOperation newOperation(IParameter firstParam, IParameter secondParam) {
        return new OTOperation(0, firstParam, secondParam);
    }

    @Override
    public IOperation newOperation(IParameter firstParam, IParameter secondParam, IParameter thirdParam) {
        return new OTOperation(0, firstParam, secondParam, thirdParam);
    }

    @Override
    public IOperation newOperation(IParameter firstParam, IParameter secondParam, IParameter thirdParam, IParameter forthParam) {
        return new OTOperation(0, firstParam, secondParam, thirdParam, forthParam);
    }

    @Override
    public IRegisteredMemoryReference newRegisteredMemoryReference(ISharedMemory sharedMemory, IRegisteredMemoryReference.Flag flag, int offset) throws BadParametersException {
        if(sharedMemory == null || sharedMemory.asByteArray().length < offset) throw new BadParametersException("Incorrect input parameters", ReturnOriginCode.TEEC_ORIGIN_COMMS);
        return new OTRegisteredMemoryReference(sharedMemory, flag, offset);
    }

    @Override
    public IValue newValue(IValue.Flag flag, int a, int b) {
        return new OTValue(flag, a, b);
    }

    @Override
    public IContext initializeContext(String teeName, Context context) throws TEEClientException{
        return new OTContext(teeName, context);
    }
}
