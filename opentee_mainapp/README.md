#Opentee-android

This lib has a service which can install or run the prepackaged binaries.
All binaries and libraries loaded by the native code are in src/main/assets/{armeabi,armeabi-v7a,x86} while all
libraries that are dynamically loaded by the native code (and not as a dependency) are in
src/main/libs/{armeabi,armeabi-v7a,x86} and are loaded by the java middleware layer.

You can use the `install_opentee_files.sh` script to install the binaries and libraries to the appropriate directories
easily.

##License

This source code is available under the terms of the Apache License, Version 2.0:
http://www.apache.org/licenses/LICENSE-2.0

