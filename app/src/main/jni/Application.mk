APP_STL := gnustl_static
#APP_STL := stlport_static
APP_PLATFORM := android-21
APP_ABI := armeabi armeabi-v7a x86 #all
APP_CFLAGS := -g -Wall -O1

APP_BUILD_SCRIPT := Android.mk
NDK_TOOLCHAIN_VERSION := 4.9
