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

// IOTConnectionInterface.aidl
package fi.aalto.ssg.opentee;

// Declare any non-default types here with import statements
import fi.aalto.ssg.opentee.imps.OTSharedMemory;
import fi.aalto.ssg.opentee.ISyncOperation;

interface IOTConnectionInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

    int teecInitializeContext(String name);

    void teecFinalizeContext();

    // It is sophisticated to pass enum in AIDL.
    int teecRegisterSharedMemory(inout OTSharedMemory sharedMemory);

    void teecReleaseSharedMemory(int smId);

    // open session without operation.
    int teecOpenSessionWithoutOp(int sid, in ParcelUuid parcelUuid, int connMethod, int connData, out int[] retOrigin);

    int teecOpenSession(int sid, in ParcelUuid parcelUuid, int connMethod, int connData, in byte[] teecOperation, out int[] retOrigin, in ISyncOperation syncOperation, int opHashCode);
    //int teecOpenSessionWithByteArrayWrapper(int sid, in ParcelUuid parcelUuid, int connMethod, int connData, inout ByteArrayWrapper teecOperation, out int[] retOrigin);

    void teecCloseSession(int sid);

    int teecInvokeCommandWithoutOp(int sid, int commandId, out int[] returnOrigin);

    int teecInvokeCommand(int sid, int commandId, in byte[] teecOperation, out int[] returnOrigin, in ISyncOperation syncOperation, int opHashCode);

    void teecRequestCancellation(int opId);
}
