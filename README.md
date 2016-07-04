## Project Description
This project provides a Java API (named OT-J) for using GlobalPlatform compliant Trusted Execution Environments (TEEs). This allows developers to write Android applications that interact with the TEE without having to write native code. Do demonstrate the functionality of this API, this project includes a test application. However, this API can be used with any GP-compliant Trusted Application (TA).<br/>
More detailed information about this project is presented in [Rui Yang's MSc Thesis](document/thesis-main.pdf) from Aalto University.

### Repository Structure

This repository consists of the following directories:

- **document**: contains the design documents for this project, including the Java API documentation and [Rui Yang's MSc Thesis](document/thesis-main.pdf).

- **opentee**: contains
	* TEE Proxy Service
	* NativeLibtee
	* Libtee
	* [Open-TEE](https://open-tee.github.io)

- **otclient**: contains the Java API (OT-J) and an implementation of this API using Open-TEE. Android Client Applications (CAs) must import this module in order to interact with the **opentee** module.

- **testapp**: contains an Android test application which utilizes this Java API.

### Support Library Dependency
1. Google ProtocolBuffers 2.6.1
2. Open-TEE libtee module.

## Building

### Supported Android Versions

**Supported Android version: 5.0 - 5.1.1**

The current implementation of the API uses Open-TEE in place of a hardware TEE. Open-TEE does not yet support Android versions above 5.1.1. This project has been tested on Android 5.0 & 5.1.1


### Prerequisites

* **Android Studio (optional)** is an IDE to develop Android applications. This can be downloaded and installed by following the official instructions at: <https://developer.android.com/studio/install.html>

* **Android SDK** normally comes bundled with Android Studio. If you are not using Android Studio, you must download and install the Android SDK separately following the official instructions at: <https://developer.android.com/studio/command-line/index.html>

* **Android NDK** provides the ability to compile native code for use in Android applications. Download and install it using the official installation instructions at <https://developer.android.com/ndk/downloads/index.html>


### Obtain the Source Code
Clone this repository:
```shell
	$ git clone --recursive https://git.ssg.aalto.fi/platsec/opentee-android.git
```


### Build with Android Studio
1. Import **opentee-android** into Android Studio. Go to **File->New->Import Project...** and select the **opentee-android** under the **opentee-android-test** directory. Wait for Android Studio to finish the import task.
2. You need either an Android device or an Android emulator to run our test application. To set up a debugging environment, follow the instructions at: https://developer.android.com/studio/run/index.html
3. Run **opentee** run-time configuration by selecting the opentee from the drop-down list on the left side of the **Run** button. Click the **Run** button and select your target device, which can be either a real Android device or an emulator.
4. Follow the same steps as above to run the **testapp** run-time configuration.

For any errors during this process, please refer to the **FAQ** section.

The compilation process may take a couple of minutes the first time it is run. After successfully building this project, you can run the two complied applications.


### Build without Android Studio

It is assumed that you have already installed the Android SDK and NDK (see prerequisites). Run the following commands to build this project:
```shell
	$ cd opentee-android
	$ export ANDROID_HOME="YOUR_ANDROID_SDK_PATH"
	$ export ANDROID_NDK_HOME="YOUR_ANDROID_NDK_PATH"
	$ ./gradlew assembleDebug
```

After successful compilation, the output will be two .apk files located in folder **opentee/build/outputs/apk/** and **testapp/build/outputs/apk/**. These can be installed and run on emulators or real devices as usual.

For any errors during this process, please refer to the FAQ section.



## Running

### Running the Test App

**Note** The supported Android version is 5.0 to 5.1.1

1. Run the **opentee** app and then the **testapp** run-time configurations on a device or emulator. 

2. When the **testapp** UI is displayed, click the buttons in the following sequence: "CREAT ROOT KEY" -> "INITIALIZE" -> "CREATE DIRECTORY KEY" -> "ENCRYPT DATA" -> "DECRYPT DATA" -> "FINALIZE".

3. After clicking "DECRYPT DATA", the decrypted data should be the same as the initial data buffer. The output should be the same as that shown on page 51 of [Rui Yang's MSc Thesis](document/thesis-main.pdf). If this is not the case, or if there are runtime errors, please refere to **FAQ** section.


### Running other TAs

1. Copy the new TAs into **opentee/src/main/assets/$abi_version**.

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

## Update Open-TEE

Follow the instructions on the [Open-TEE github page](https://open-tee.github.io/android/) to build Open-TEE engine for a specific platform. Then copy the generated Open-TEE engine and shared libraries into the assets directory of the opentee module using following commands:
```shell
	$ cp $ANDROID_ROOT/out/target/product/$abiVersion/system/bin/opentee-engine $OPENTEE_Android/opentee/src/main/assets/$ABI/
	$ cp $ANDROID_ROOT/out/target/product/$abiVersion/system/lib/*.so $OPENTEE_Android/opentee/src/main/assets/$ABI/
```
 
## Generate API Javadoc

Note that there is already a generated javadoc in **document/teec_java_api.pdf**.

### Required Tools
1. javadoc: should be included with the Oracle OpenJDK pakcage. Try to issue the $javadoc command to check if this is installed. If not, install latest OpenJDK package.

2. pdfdoclet: used to generate PDF-formatted javadoc. Download and install this tool following the instructions at: <https://sourceforge.net/projects/pdfdoclet/>


### Generate the Javadoc
Generate the java doc using the following command:
```shell
	$ javadoc -doclet com.tarsec.javadoc.pdfdoclet.PDFDoclet -docletpath $PDFDOCLET_UNZIPPED_DIR/pdfdoclet-1.0.3-all.jar -pdf $OUTPUT_FILE_WITH_FULL_PATH $PROJECT_HOME_DIR/opentee-android/otclient/src/main/java/fi/aalto/ssg/opentee/*.* $PROJECT_HOME_DIR/opentee-android/otclient/src/main/java/fi/aalto/ssg/opentee/exception/*.*
```


## FAQ

#### Missing files under libtee
This project imports libtee as a submodule. Ensure that **opentee/src/main/jni/libtee** exists. If not, pull the content by using the following command:
```shell
	$ git submodule update --init
```

#### No output after clicking "generating root key" in testapp
Since the dependency **OPEN-TEE** can only run up to Android 5.1.1, if you deployed the **opentee** application to an Android phone/emulator which has a version higher than 5.1.1, no output will be displayed.

#### Other issues
For any issues not mentioned above, please report these in the issue tracker.


## License
This source code is available under the terms of the Apache License, Version 2.0: <http://www.apache.org/licenses/LICENSE-2.0>
This project has used [Protocol Buffers](https://developers.google.com/protocol-buffers/) 2.6.1 of Google Inc.
