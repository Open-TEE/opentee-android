/*
 * Copyright (c) 2016 Aalto University
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
package fi.aalto.ssg.opentee.testapp;

import java.util.UUID;

/**
 * OmniShare Utils from Rui.
 */
public class OmnishareUtils {
    public static long strToLong(String strVal){
        if(strVal == null || strVal.isEmpty()) return 0;

        byte[] vals = strVal.getBytes();
        int tailFlag = vals.length > 8 ? 8 : vals.length;
        long result = 0;
        for(int i = 0; i < tailFlag; i++){
            result = result << 8;
            result += vals[i];
        }
        return result;
    }

    public static synchronized UUID getOmnishareTaUuid(){
        long clockSeqAndNode = strToLong(new String("OMNISHAR"));
        UUID TA_CONN_TEST_UUID = new UUID(0x1234567887654321L, clockSeqAndNode);

        return TA_CONN_TEST_UUID;
    }
}
