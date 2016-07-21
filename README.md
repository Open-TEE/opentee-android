## Project Description
This project provides a Java API (named OT-J) for using GlobalPlatform compliant Trusted Execution Environments (TEEs). This allows developers to write Android applications that interact with the TEE without having to write native code. Do demonstrate the functionality of this API, this project includes a test application. However, this API can be used with any GP-compliant Trusted Application (TA).<br/>
More detailed information about this project is presented in [Rui Yang's MSc Thesis](document/thesis-main.pdf) from Aalto University.

### Repository Structure

This repository consists of the following directories:

- **document**: contains the design documents for this project, including the Java API documentation and [Rui Yang's MSc Thesis](document/thesis-main.pdf).

- **opentee**: is an Android Library which has [Open-TEE](https://open-tee.github.io) along with utility functions to use Open-TEE in Android. You can import either the source code of this module or using its aar file (see instruction [here](http://stackoverflow.com/questions/16682847/how-to-manually-include-external-aar-package-using-new-gradle-android-build-syst)) after you build its source code.

- **otclient**: contains the Java API (OT-J) and an implementation of this API using Open-TEE. Android Client Applications (CAs) must import this module in order to interact with the **otservice** module.

- **otservice**: contains
	* TEE Proxy Service
	* NativeLibtee
	* Libtee

- **javaapitest**: contains an Android test application which utilizes this Java API.

- **bundletest**: is a standalone Android application which includes Open-TEE by importing the **opentee** module.

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
	$ git checkout integration
	$ git submodule add -f https://github.com/Open-TEE/libtee.git otservice/src/main/jni/libtee
```


### Build with Android Studio
1. Import **opentee-android** into Android Studio. Go to **File->New->Import Project...** and select the **opentee-android** under the **opentee-android-test** directory. Wait for Android Studio to finish the import task.
2. You need either an Android device or an Android emulator to run our test application. To set up a debugging environment, follow the instructions at: https://developer.android.com/studio/run/index.html
3. Run **otservice** run-time configuration by selecting the otservice from the drop-down list on the left side of the **Run** button. Click the **Run** button and select your target device, which can be either a real Android device or an emulator.
4. Follow the same steps as above to run the **javaapitest** run-time configuration.

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

After successful compilation, the output will be two .apk files located in folder **otservice/build/outputs/apk/** and **javaapitest/build/outputs/apk/**. These can be installed and run on emulators or real devices as usual.

For any errors during this process, please refer to the FAQ section.



## Running

### Running the Test App

**Note** The supported Android version is 5.0 to 5.1.1

#### Manually
1. Run the **otservice** app and then the **javaapitest** run-time configurations on a device or emulator;

2. When the **javaapitest** UI is displayed, click the buttons in the following sequence: "CREAT ROOT KEY" -> "INITIALIZE" -> "CREATE DIRECTORY KEY" -> "ENCRYPT DATA" -> "DECRYPT DATA" -> "FINALIZE";

3. After clicking "DECRYPT DATA", the decrypted data should be the same as the initial data buffer. The output should be the same as that shown on page 51 of [Rui Yang's MSc Thesis](document/thesis-main.pdf). If this is not the case, or if there are runtime errors, please refere to **FAQ** section.

#### Unit Test Case
1. Start the **otservice** app and make sure that your device is active (no lock screen);

2. Run the test case **javaapitest/src/androidTest/java/fi/aalto/ssg/opentee/javaapitest/ApplicationTest.java**.
 
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
This project imports libtee as a submodule. Ensure that **otservice/src/main/jni/libtee** exists. If not, pull the content by using the following command:
```shell
	$ git submodule update --init
```

#### No output after clicking "generating root key" in javaapitest
Since the dependency **OPEN-TEE** can only run up to Android 5.1.1, if you deployed the **otservice** application to an Android phone/emulator which has a version higher than 5.1.1, no output will be displayed.

#### Failed to find build tools revision
When you build this project using the command line, you may encounter this error. Make sure you have the right version of build tool. You can check what version you have under the **$ANDROID_HOME/build-tools**. To fix this error, you can either change the build version of the module which throws the error to the version you have in build.gradle. This way is not recommended. Alternatively, you can download the right version using the Android SDK manager.

#### Missing _libtee.so_
You can either use prebuilt libtee.so from the _opentee/src/main/jniLibs/$abiversion/libtee.so_ or build the libtee.so from the libtee source code using _ndk-build_ (make sure that you have ndk-build downloaded and added its path to the PATH environment variable).

#### Other issues
For any issues not mentioned above, please report these in the issue tracker.


## License
This source code is available under the terms of the Apache License, Version 2.0: <http://www.apache.org/licenses/LICENSE-2.0>
This project has used [Protocol Buffers](https://developers.google.com/protocol-buffers/) 2.6.1 of Google Inc.
