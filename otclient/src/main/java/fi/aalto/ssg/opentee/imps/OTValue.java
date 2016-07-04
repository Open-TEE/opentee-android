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
 * OTValue implements the ITEEClient.IValue interface.
 */
public class OTValue implements ITEEClient.IValue {
    Flag mFlag;
    int mA;
    int mB;

    public OTValue(Flag flag, int a, int b){
        this.mFlag = flag;
        this.mA = a;
        this.mB = b;
    }

    @Override
    public int getA(){return this.mA;}

    @Override
    public int getB(){return this.mB;}

    public Flag getFlag(){
        return this.mFlag;
    }

    @Override
    public Type getType() {
        return Type.TEEC_PTYPE_VAL;
    }

    public void setA(int a){this.mA = a;}

    public void setB(int b){this.mB = b;}
}
