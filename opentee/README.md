#Opentee-android

This is an android library that packages the Open-TEE compiled files (binaries/libraries) and installs/runs them in a device with application privileges (not root). The necessary files are bundled as assets in the Application. All binaries and libraries loaded by the native code are in src/main/assets/{armeabi,armeabi-v7a,x86} (and are used because the LD_LIBRARY_PATH environment variables is explicitly specified) while all libraries that are dynamically loaded by the native code (and not as a dependency) are in src/main/libs/{armeabi,armeabi-v7a,x86}.

An Android service is exposed and provides an easy interface (via the _OpenTEEConnection_ class) for developers to install or run Open-TEE. The service listens for messages from clients that bind to it and executes whatever task it is given sequentially. Single-threading in the Android Service is used to avoid race conditions where a binary might be executed before it is installed in the home directory or before the engine is restarted etc.

All the files are installed in the home data directory of the app. In android that corresponds to _/data/data/<application package name>/opentee/_. Inside that directory three sub-directories are created: _bin/_ for the binaries, _ta/_ for trusted applications and _tee/_ for libraries loaded by the native code dynamically  (libManagerApi and libLauncherApi). That directory also keeps the process id file (.pid) of the Open-TEE engine and the socket file that is used for communication between the manager and the client applications. Since all these files are contained inside the Android applications directory and are executed by the applications' user no other permissions are necessary to run Open-TEE.

All libraries that are dependencies to Open-TEE and are not loaded dynamically are kept in the src/main/libs folder of the module. This is used as the directory where the system linker will search for dependency libraries and is defined by the environmental variable LD_LIBRARY_PATH.

The module can be bundled along with any Android app that wants to utilize Open-TEE. That app will then bind to the provided Android service and install/run Open-TEE to test any TA on it. Additional functions are also provided for the Application using the service to install TAs in the home directory via byte array streams.

### Including/updating Open-TEE compiled binaries and libraries

You can use the `install_opentee_files.sh` script to install the binaries and libraries to the appropriate directories
easily. The script takes as input the $OUT (output directory) that exists after compiling Open-TEE against the android
source tree. The subdirectories in $OUT should be _system/_, _obj/_, _symbols/_ .

### Importing and testing a TA via Android studio

There are two ways of importing TAs to the Open-TEE android module:

1. By running *./install_opentee_files.sh*  which will copy your compiled CA and TA to the appropriate directories in the Studio project. 
2. Modify OTConstants.java to add the names of your CA/TA
3. Modify the MSG_INSTALL_ALL function in *OpenTEEService.java* to also install your TA to *OTConstants.OPENTEE_TA_DIR* and your CA to *OTConstants.OPENTEE_BIN_DIR*

OR

1. Use only the OpenTEEConnection class and its installByteStreamTA() method to install your TA from any source (file, network etc). An example can be seen in MainActivity.java in the opentee-android module (testapp) demonstrating the usage of the service.

In both cases run your CA via an OpenTEEConnection instance. E.g: `mOpenTEEConnection.runOTBinary(OTConstants.STORAGE_TEST_APP_ASSET_BIN_NAME)`


##License

This source code is available under the terms of the Apache License, Version 2.0:
http://www.apache.org/licenses/LICENSE-2.0

