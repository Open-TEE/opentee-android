/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fi.aalto.opentee;

import android.util.Log;

import org.opensc.pkcs11.PKCS11EventCallback;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

public class PinEntryUI implements CallbackHandler {
    public enum PinType {
        SO_PIN,
        USER_PIN
    }

    private static final String TAG = "PinEntryUI";

    private PinType mLastPinTypeRequested;
    private String mUserPin, mSOPin;

    public String getUserPin() {
        return mUserPin;
    }

    public String getSOPin() {
        return mSOPin;
    }

    public void setPin(PinType lastPinTypeRequested, String lastPin) {
        this.mLastPinTypeRequested = lastPinTypeRequested;
        if (mLastPinTypeRequested.equals(PinType.SO_PIN)) {
            this.mSOPin =  lastPin;
        } else {
            this.mUserPin = lastPin;
        }
    }

    public String getLastPin() {
        if (mLastPinTypeRequested.equals(PinType.SO_PIN)) {
            return mSOPin;
        } else {
            return mUserPin;
        }
    }

    public PinType getLastPinTypeRequested() {
        return mLastPinTypeRequested;
    }

    public void setLastPinTypeRequested(PinType lastPinTypeRequested) {
        this.mLastPinTypeRequested = lastPinTypeRequested;
    }

    public PinEntryUI() {
        // Use user pin by default
        mLastPinTypeRequested = PinType.USER_PIN;
        mUserPin = "";
    }

    public PinEntryUI(PinType lastPinTypeRequested, String lastPin) {
        setPin(lastPinTypeRequested, lastPin);
    }

    /***
     * This is called by the Keystore whenever user authentication is required.
     * Normally this would instantiate a popup and request a password from the user to input to the
     * Callback the Keystore provides. Since android doesn't allow easy non-blocking UI (unless you
     * use threading) the password is currently asked only once from the user by the Class that
     * constructs the PinEntryUI and is kept in the PinEntryUI memory. Otherwise this would have to
     * be in a different thread than the UI and: 1) call the UI thread to show an AlertDialog to the
     * user and 2) block.
     * This AlertDialog would need another callback to this thread that would trigger re-execution
     * when the user inputs the password and then return the result to the Keystore that would be waiting.
     * This isn't necessary since for now both threads are executed in the same application context
     * and thus there is no advantage in requesting the password multiple times in a blocking mode.
     * Inside the handle it checks if the event it got from the Keystore is expecting an SO password
     * or a Userpassword and responds with the corresponding password.
     * @param callbacks
     * @throws IOException
     * @throws UnsupportedCallbackException
     */
    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (Callback callback : callbacks)
        {
            if (callback instanceof PasswordCallback)
            {
                PasswordCallback pwCb = (PasswordCallback)callback;

                Log.i(TAG, "Returning stored " + mLastPinTypeRequested + " entry...");
                pwCb.setPassword(getLastPin().toCharArray());

                Log.i(TAG, "PIN has successfully been entered to callback.");
            }
            else if (callback instanceof PKCS11EventCallback)
            {
                PKCS11EventCallback evCb = (PKCS11EventCallback)callback;

                Log.i(TAG, "Received event [" + evCb + "].");
                if (evCb.toString().equals("WAITING_FOR_SW_SO_PIN")) {
                    mLastPinTypeRequested = PinType.SO_PIN;
                } else if (evCb.toString().equals("WAITING_FOR_SW_PIN")) {
                    mLastPinTypeRequested = PinType.USER_PIN;
                }
            }
            else
                throw new UnsupportedCallbackException(callback,"Only PasswordCallback or PKCS11EventCallback is supported.");

        }
    }
}
