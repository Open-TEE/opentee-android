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
 * The requested operation is valid but is not supported in this implementation. This exception can
 * be threw by underlying library when the CA tries to invoke a valid operation which does not exists
 * in current implementation of TA. This exception can be used to notify the CA that it talks to an older
 * version of TA which does not support such an operation. So, this exception can help the CA to be
 * backward compatible.
 */
public class NotSupportedException extends TEEClientException {
    public NotSupportedException(String msg){
        super(msg);
    }

    public NotSupportedException(String msg, ITEEClient.ReturnOriginCode retOrigin){
        super(msg, retOrigin);
    }
}
