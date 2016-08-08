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

import fi.aalto.ssg.opentee.ITEEClient;

/**
 * OTRegisteredMemoryReference implements ITEEClient.IRegisteredMemoryReference interface.
 */
public class OTRegisteredMemoryReference implements ITEEClient.IRegisteredMemoryReference {
    ITEEClient.ISharedMemory mSharedMemory;
    int mOffset = 0; // initialized to 0.
    Flag mFlag;

    /**
     * Create a registered memory reference with a valid ISharedMemory interface and a
     * flag to indicate the I/O direction for this memory reference. The flag is only valid when
     * the corresponding shared memory also has such a flag.
     * @param sharedMemory
     * @param flag
     */
    public OTRegisteredMemoryReference(ITEEClient.ISharedMemory sharedMemory, Flag flag, int offset){
        this.mSharedMemory = sharedMemory;
        this.mFlag = flag;
        this.mOffset = offset;
    }

    /**
     * Get the referenced registered shared memory.
     * @return ISharedMemory interface for the referenced registered shared memory.
     */
    @Override
    public ITEEClient.ISharedMemory getSharedMemory(){
        return this.mSharedMemory;
    }

    public Flag getFlag(){return this.mFlag;}

    @Override
    public int getOffset(){return this.mOffset;}

    @Override
    public int getReturnSize() {
        //return this.mSharedMemory.getReturnSize();
        return ((OTSharedMemory)mSharedMemory).getReturnSize();
    }

    @Override
    public Type getType() {
        return Type.TEEC_PTYPE_RMR;
    }
}
