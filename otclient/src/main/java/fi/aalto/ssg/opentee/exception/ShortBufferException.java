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
 * The supplied buffer is too short for the generated output. This exception can be threw by underlying
 * library when the TA tries to copy the result to a shorter output buffer which is previously given
 * by CA. This scenario can happen when the provided Shared Memory or Value for output is too short.
 * Under such a circumstance, the developers are suggested to allocate a bigger buffer for output. If
 * the Value is not bigger enough for the output, the Shared Memory should be used instead.
 */
public class ShortBufferException extends TEEClientException {
    public ShortBufferException(String msg){
        super(msg);
    }

    public ShortBufferException(String msg, ITEEClient.ReturnOriginCode retOrigin){
        super(msg, retOrigin);
    }
}
