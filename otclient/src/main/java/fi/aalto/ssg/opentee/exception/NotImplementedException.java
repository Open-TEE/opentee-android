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
 * The requested operation should exist but is not yet implemented. This exception can threw by
 * underlying library when the CA invokes an operation which has not been implemented in TA yet. TA
 * can use this exception to notify the CA that the function it invoked is not ready right now but might
 * be available in the future.
 */
public class NotImplementedException extends TEEClientException{
    public NotImplementedException(String msg){
        super(msg);
    }

    public NotImplementedException(String msg, ITEEClient.ReturnOriginCode retOrigin){
        super(msg, retOrigin);
    }
}
