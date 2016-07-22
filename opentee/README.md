## Opentee-android

This is an android library that packages the Open-TEE compiled files (binaries/libraries) and provides utility functions to install/run them in a device with application privileges (not root). The necessary binary files(including libLauncher.so and libManager.so) along with its setting file _opentee.conf.android_ are bundled as assets in the Application. And required library files are included under the **jniLibs** folder, which will be bundled with the APK and pushed to the **lib** directory during installation of the application. The path of these libraries is stored in the LD_LIBRARY_PATH environment variables which must be specified in the setting file so that those binary files can find and load them correctly. In addition, all binaries and libraries loaded by the native code have different target version {armeabi,armeabi-v7a,x86}.

All the files excluding those library files are installed in the home data directory of the app. In android that corresponds to _/data/data/<application package name>/opentee/_. Inside that directory three sub-directories are created: _bin/_ for the binaries _opentee-engine_, _ta/_ for trusted applications and _tee/_ for libraries loaded by the native code dynamically  (libManagerApi and libLauncherApi). That directory also keeps the process id file (.pid) of the Open-TEE engine and the socket file that is used for communication between the manager and the client applications. Since all these files are contained inside the Android applications directory and are executed by the applications' user no other permissions are necessary to run Open-TEE.

The module can be bundled along with any Android app that wants to utilize Open-TEE. An example about how to deploy Open-TEE using this module can be found in another module named **bundletest**.

### Update Open-TEE

Follow the instructions on the [Open-TEE github page](https://open-tee.github.io/android/) to build Open-TEE engine for a specific platform. Then copy the generated Open-TEE engine and shared libraries into the assets directory of the opentee module using following commands:
```shell
	$ cp $ANDROID_ROOT/out/target/product/$abiVersion/system/bin/opentee-engine $OPENTEE_Android/opentee/src/main/assets/$ABI/
	$ cp $ANDROID_ROOT/out/target/product/$abiVersion/system/lib/*.so $OPENTEE_Android/opentee/src/main/jniLibs/$ABI/
	$ mv $OPENTEE_Android/opentee/src/main/jniLibs/$ABI/libLauncher.so $OPENTEE_Android/opentee/src/main/assets/$ABI/
	$ mv $OPENTEE_Android/opentee/src/main/jniLibs/$ABI/libManager.so $OPENTEE_Android/opentee/src/main/assets/$ABI/
```

### Running other TAs

#### Method 1 - install TA along with Open-TEE

The following steps describe how to install TAs during the installation of the Open-TEE.

1. Copy the new TAs into **opentee/src/main/assets/$abi_version/** folder;

2. Change the value of TA_List in **opentee/src/main/assets/config.properties** to include the names of the new TAs. Mutiple names must be separated using "," as in the following example:

```shell
TA_List=ta_1.so,ta_2.so,ta_3.so
```

Once installed, the new TAs will be started by Open-TEE automatically. You can review the **opentee** log (using standard [Android logging](https://developer.android.com/studio/debug/index.html#systemLog)) to ensure that your TAs are installed. In the log message, you will see something similar to the following text:
```c
I/TEE Proxy Service: -------- begin installing TAs -----------
I/TEE Proxy Service: installing TA:ta_1.so
I/TEE Proxy Service: installing TA:ta_2.so
I/TEE Proxy Service: installing TA:ta_3.so
I/TEE Proxy Service: -----------------------------------------
```

#### Method 2 - install TA from CA

**OTInstallTA** can be used for the Application to install TAs in the form of byte array streams during runtime. See the following example.

```java
// create a new install TA task. The TA will be stored into $APP_DATA_PATH/opentee/ta with the name TA_NAME. Open-TEE will automatically start it once installed.
OTInstallTA installTA = new OTInstallTA(getApplicationContext(),    // application context
                TA_NAME,	// the name to be stored.
                taInBytes,	// TA in raw bytes.
                true);		// overwrite previous TA if exists.

// the installTATask is a Runnable instance. There are many options to run it. For instance, you can just post it to a running thread like the following line of code. At the same time, make sure that your TA is not rejected by looking the logcat.
mHandler.post(installTA.installTATask);
```

## License

This source code is available under the terms of the Apache License, Version 2.0: <http://www.apache.org/licenses/LICENSE-2.0>