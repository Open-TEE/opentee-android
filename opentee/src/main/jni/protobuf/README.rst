Google protobuf for Android NDK
-------------------------------
This builds a static library to use protobuf with C++ on android.

This is based on protobuf-2.6.1 and it boils down to :

- Copying the minimal set of .cc files to build the library (without tests)

- Write a config.h and Android.mk that work on android

The example directory contains an example protobuf that can be compiled &
linked by NDK to check that the linker has all the dependencies:

  cd example

  ./build.sh
