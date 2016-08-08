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
 * TEEClientException extends the java.lang.Exception class. All exceptions in this project should
 * subclass it excluding exceptions defined by Android. The origin which causes this exception can be
 * obtained using getReturnOrigin function. In most cases, all exceptions come without a return origin.
 * Only exceptions come from the openSession and invokeCommand function calls have it. So developer
 * should be able to distinguish these two kinds of exceptions. The getReturnOrigin will return null
 * if one exception does not have a return origin.
 */
public abstract class TEEClientException extends java.lang.Exception{
    /**
     * The field indicates the return origin which causes this exception.
     */
    ITEEClient.ReturnOriginCode mReturnOriginCode;

    public TEEClientException() { super(); }
    public TEEClientException(ITEEClient.ReturnOriginCode returnOriginCode){
        super();
        mReturnOriginCode = returnOriginCode;
    }
    public TEEClientException(String message) { super(message); }
    public TEEClientException(String message, ITEEClient.ReturnOriginCode returnOriginCode) {
        super(message);
        mReturnOriginCode = returnOriginCode;
    }
    public TEEClientException(String message, Throwable cause) { super(message, cause); }
    public TEEClientException(Throwable cause) { super(cause); }

    /**
     * Get the return origin.
     * @return The return origin code.
     */
    public ITEEClient.ReturnOriginCode getReturnOrigin(){
        return this.mReturnOriginCode;
    }
}
