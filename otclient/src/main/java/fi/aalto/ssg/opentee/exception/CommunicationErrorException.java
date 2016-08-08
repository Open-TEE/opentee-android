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
package fi.aalto.ssg.opentee.exception;

import fi.aalto.ssg.opentee.ITEEClient;

/**
 * Communication with a remote party failed. This exception includes the communication errors with
 * Android IPC calls which have already been defined in RemoteException and the developers are also
 * suppose to handle it too. On the basis of the CA able to communicate with remote service, this
 * exception is threw by underlying library when the NativeLibtee fails to communicate with the TEE
 * or TAs. This situation can be caused by the following cases:
 * 1. when there are internal errors with TEE which disable the NativeLibtee taking to TEE. Under
 * such a circumstances, the developers are suggested to check the states and configurations of TEE
 * on target device and adjust TEE to run properly before interacting with in Client Application;
 * 2. when TEE is unable to talk to TA especially when the TA is crashed due to internal errors. Under
 * such a circumstance, the developers are suggested to debug the TA and fix corresponding errors before
 * using CA talks to it.
 */
public class CommunicationErrorException extends TEEClientException {
    public CommunicationErrorException(String msg){
        super(msg);
    }

    public CommunicationErrorException(String msg, ITEEClient.ReturnOriginCode retOrigin){
        super(msg, retOrigin);
    }
}
