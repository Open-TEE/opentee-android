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

/**
 * This class defines all the value of return value which should be agreed with the underlying libraries
 * of Client Application and the remote OT service. The remote OT service pass the value back to
 * underlying libraries of Client Application. Then the underlying libraries can determine how to
 * notify the developers in the upper layer such as throwing exceptions defined in ITEEClient interface.
 */
public class OTReturnCode {
    public static final int TEEC_SUCCESS = 0x00000000;
    public static final int TEEC_ERROR_GENERIC = 0xFFFF0000;
    public static final int TEEC_ERROR_ACCESS_DENIED = 0xFFFF0001;
    public static final int TEEC_ERROR_CANCEL = 0xFFFF0002;
    public static final int TEEC_ERROR_ACCESS_CONFLICT = 0xFFFF0003;
    public static final int TEEC_ERROR_EXCESS_DATA = 0xFFFF0004;
    public static final int TEEC_ERROR_BAD_FORMAT = 0xFFFF0005;
    public static final int TEEC_ERROR_BAD_PARAMETERS = 0xFFFF0006;
    public static final int TEEC_ERROR_BAD_STATE = 0xFFFF0007;
    public static final int TEEC_ERROR_ITEM_NOT_FOUND = 0xFFFF0008;
    public static final int TEEC_ERROR_NOT_IMPLEMENTED = 0xFFFF0009;
    public static final int TEEC_ERROR_NOT_SUPPORTED = 0xFFFF000A;
    public static final int TEEC_ERROR_NO_DATA = 0xFFFF000B;
    public static final int TEEC_ERROR_OUT_OF_MEMORY = 0xFFFF000C;
    public static final int TEEC_ERROR_BUSY = 0xFFFF000D;
    public static final int TEEC_ERROR_COMMUNICATION = 0xFFFF000E;
    public static final int TEEC_ERROR_SECURITY = 0xFFFF000F;
    public static final int TEEC_ERROR_SHORT_BUFFER = 0xFFFF0010;
    public static final int TEE_ERROR_EXTERNAL_CANCEL = 0xFFFF0011;
    public static final int TEE_ERROR_OVERFLOW = 0xFFFF300F;
    public static final int TEE_ERROR_TARGET_DEAD = 0xFFFF3024;
    public static final int TEEC_ERROR_TARGET_DEAD = 0xFFFF3024;
    public static final int TEE_ERROR_STORAGE_NO_SPACE = 0xFFFF3041;
}
