#OpenSC-android

This is an android library packaging the OpenSC jni bindings, libtee and libtee_pkcs11 from the OpenTEE project.

##Building

It should be directly importable as a project to Android Studio. Alternatively you can import the app (android
application) or openteelib (android library) modules directly. The java/ and jni/ folders in app are symlinked to the
corresponding openteelib ones so that there are no code duplicates.

In either case you will need to also:

1. download the android ndk, add the directory to your environment PATH and also edit the local.properties file in this
   repo and adjust it there too. Same for the android-sdk.

2. set the java target version to 1.7

3. update the openteelib/src/main/jni/prebuilt_libs directory with the latest android build of libtee and libtee_pkcs11
   as well as their header files in prebuilt_libs/libtee_include and prebuilt_libs/libtee_pkcs11_include

##Usage

1. Copy the generated library from openteelib/build/outputs/aar/openteelib-debug.aar to the libs/ folder of your module 
2. Add it as a new module via `File/New/New Module.../Import existing AAR` in Android Studio to import the module.
3. Add it as a dependency to your primary module by right clicking on your module/Module Settings/Dependencies/Module
   and choose ":openteelib". This should modify the build.gradle of your module and add it automatically.

After that you should be able to use the PKCS11Provider from OpenSC.

##License

This source code is available under the terms of the Apache License, Version 2.0:
http://www.apache.org/licenses/LICENSE-2.0

