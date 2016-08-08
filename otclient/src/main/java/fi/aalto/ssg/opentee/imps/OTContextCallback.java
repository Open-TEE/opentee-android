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
import fi.aalto.ssg.opentee.exception.BadFormatException;
import fi.aalto.ssg.opentee.exception.BusyException;
import fi.aalto.ssg.opentee.exception.CommunicationErrorException;
import fi.aalto.ssg.opentee.exception.ExcessDataException;
import fi.aalto.ssg.opentee.exception.ExternalCancelException;
import fi.aalto.ssg.opentee.exception.TEEClientException;

/**
 * Callback function for OTContext.
 */
interface OTContextCallback {
    void closeSession(int sid) throws RemoteException, CommunicationErrorException;
    ReturnValueWrapper invokeCommand(int sid, int commandId, ITEEClient.IOperation iOperation) throws TEEClientException;
}
