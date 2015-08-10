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
package fi.aalto.ssg.opentee_mainapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.io.File;

public class OpenTEEConnection {

    private static final String OPENTEECONNECTION_TAG = "OPENTEE_CONNECTION";

    /** Messenger for communicating with the service. */
    private Messenger mService = null;

    /** Flag indicating whether we have called bind on the service. */
    private boolean mBound;

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection;

    private Context mContext;

    public OpenTEEConnection(Context context) {
        this(context, null);
    }

    public OpenTEEConnection(Context context, final OTCallback otCallback) {
        mContext = context;
        mConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                // This is called when the connection with the service has been
                // established, giving us the object we can use to
                // interact with the service.  We are communicating with the
                // service using a Messenger, so here we get a client-side
                // representation of that from the raw IBinder object.
                mService = new Messenger(service);
                mBound = true;
                if (otCallback != null) otCallback.onConnectionEstablished();
            }

            public void onServiceDisconnected(ComponentName className) {
                // This is called when the connection with the service has been
                // unexpectedly disconnected -- that is, its process crashed.
                mService = null;
                mBound = false;
                if (otCallback != null) otCallback.onConnectionDestroyed();
            }
        };
        boolean result = startConnection();
        Log.d(OPENTEECONNECTION_TAG, "Connection to OPTEEService returned: " + result);
    }

    public boolean startConnection() {// Bind to the service
        return mContext.bindService(new Intent(mContext, OpenTEEService.class), mConnection,
                Context.BIND_AUTO_CREATE);
    }

    public void stopConnection() {// Unbind from the service
        if (mBound) {
            mContext.unbindService(mConnection);
            mBound = false;
        }
    }

    public void startOTEngine() {
        if (!mBound) return;
        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, OpenTEEService.MSG_START_OPENTEE_ENGINE, 0, 0);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void restartOTEngine() {
        if (!mBound) return;
        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, OpenTEEService.MSG_RESTART_OPENTEE_ENGINE, 0, 0);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void stopOTEngine() {
        if (!mBound) return;
        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, OpenTEEService.MSG_STOP_OPENTEE_ENGINE, 0, 0);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void changeSELinuxToPermissive() {
        if (!mBound) return;
        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, OpenTEEService.MSG_SELINUX_TO_PERMISSIVE, 0, 0);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void installOpenTEEToHomeDir(boolean overwrite) {
        if (!mBound) return;
        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, OpenTEEService.MSG_INSTALL_ALL, 0, 0);
        Bundle b = new Bundle();
        b.putBoolean(OpenTEEService.MSG_OVERWRITE, overwrite);
        msg.setData(b);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void installByteStreamTA(byte[] inBytes, String taName, String subdir, boolean overwrite) {
        if (!mBound) return;
        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, OpenTEEService.MSG_INSTALL_BYTE_BLOB, 0, 0);
        Bundle b = new Bundle();

        b.putString(OpenTEEService.MSG_ASSET_NAME, taName);
        b.putString(OpenTEEService.MSG_DEST_SUBDIR, subdir);
        b.putBoolean(OpenTEEService.MSG_OVERWRITE, overwrite);
        b.putByteArray(OpenTEEService.MSG_BYTE_ARRAY, inBytes);
        msg.setData(b);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Runs opentee binary from home dir bin/ folder
     * e.g. to run opentee provide the following as argument:
     * Constants.OPENTEE_BIN_DIR + File.separator + Constants.OPENTEE_ENGINE_ASSET_BIN_NAME
     */
    public void runOTBinary(String openteeBinary) {
        if (!mBound) return;
        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, OpenTEEService.MSG_RUN_BIN, 0, 0);
        Bundle b = new Bundle();
        String dataHomeDir = Utils.getFullFileDataPath(mContext);
        b.putString(OpenTEEService.MSG_ASSET_NAME, Constants.OPENTEE_BIN_DIR + File.separator + openteeBinary);
        msg.setData(b);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
