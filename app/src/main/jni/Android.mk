LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

# opensc jni wrapper module
LOCAL_MODULE := libOpenSCjni

LOCAL_C_INCLUDES := $(LOCAL_PATH)/jnix
LOCAL_C_INCLUDES += $(LOCAL_PATH)/jniP11
LOCAL_C_INCLUDES += $(LOCAL_PATH)/jniP11/opensc

PROJECT_FILES := $(wildcard $(LOCAL_PATH)/jnix/*.c)
PROJECT_FILES += $(wildcard $(LOCAL_PATH)/jniP11/*.c)

PROJECT_FILES := $(PROJECT_FILES:$(LOCAL_PATH)/%=%)

LOCAL_SRC_FILES := $(PROJECT_FILES)

LOCAL_CFLAGS := -rdynamic -DANDROID -DOT_LOGGING

LOCAL_SHARED_LIBRARIES := libcutils libc libdl #libc
LOCAL_STATIC_LIBRARIES := libstlport

LOCAL_LDLIBS := -llog

ifeq ($(TARGET_ARCH),arm)
LOCAL_LDFLAGS := -Wl,--hash-style=sysv
endif

include $(BUILD_SHARED_LIBRARY)
