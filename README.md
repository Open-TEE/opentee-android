OpenSC-android
==============

This is the android port of the OpenSC jni bindings.

##Building

It should be directly importable as a project to Android Studio. Alternatively you can import the app (android
application) or openteelib (android library) modules directly. The java/ and jni/ folders in app are symlinked to the
corresponding openteelib ones so that there are no code duplicates.

In either case you will need to also:

1. download the android ndk, add the directory to your enviroment PATH and also edit the local.properties file in this
   repo and adjust it there too. Same for the android-sdk.

2. set the java target version to 1.7

3. update the openteelib/src/main/jni/prebuilt_libs directory with the latest android build of libtee and libtee_pkcs11
   as well as their header files in prebuilt_libs/libtee_include and prebuilt_libs/libtee_pkcs11_include


