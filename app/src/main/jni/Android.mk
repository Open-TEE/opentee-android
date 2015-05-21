LOCAL_PATH := $(call my-dir)

# Build libtee
include $(CLEAR_VARS)

LOCAL_MODULE := libtee_prebuilt
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/prebuilt_libs/libtee_include
LOCAL_SRC_FILES := $(LOCAL_PATH)/prebuilt_libs/$(TARGET_ARCH_ABI)/libtee.so
$(info $(LOCAL_SRC_FILES))
#ifeq ($(TARGET_ARCH),arm)
#LOCAL_SRC_FILE := prebuilt_libs/armeabi/libtee_pkcs11.so
#endif

#ifeq ($(TARGET_ARCH),x86)
#LOCAL_SRC_FILE := prebuilt_libs/x86/libtee_pkcs11.so
#endif

include $(PREBUILT_SHARED_LIBRARY)

# Build libtee_pkcs11
include $(CLEAR_VARS)

LOCAL_MODULE := libtee_pkcs11_prebuilt
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/prebuilt_libs/libtee_pkcs11_include
LOCAL_SRC_FILES := $(LOCAL_PATH)/prebuilt_libs/$(TARGET_ARCH_ABI)/libtee_pkcs11.so

#ifeq ($(TARGET_ARCH),arm)
#LOCAL_SRC_FILE := prebuilt_libs/armeabi/libtee_pkcs11.so
#endif

#ifeq ($(TARGET_ARCH),x86)
#LOCAL_SRC_FILE := prebuilt_libs/x86/libtee_pkcs11.so
#endif

include $(PREBUILT_SHARED_LIBRARY)

# Build JNI bindings
include $(CLEAR_VARS)

# opensc jni wrapper module
LOCAL_MODULE := libOpenSCjni

LOCAL_C_INCLUDES := $(LOCAL_PATH)/
LOCAL_C_INCLUDES += $(LOCAL_PATH)/opensc

PROJECT_FILES := $(wildcard $(LOCAL_PATH)/*.c)

PROJECT_FILES := $(PROJECT_FILES:$(LOCAL_PATH)/%=%)

LOCAL_SRC_FILES := $(PROJECT_FILES)

LOCAL_CFLAGS := -rdynamic -DANDROID -DOT_LOGGING

LOCAL_SHARED_LIBRARIES := libcutils libc libdl libtee_prebuilt libtee_pkcs11_prebuilt
LOCAL_STATIC_LIBRARIES := libstlport

LOCAL_LDLIBS := -llog

ifeq ($(TARGET_ARCH),arm)
LOCAL_LDFLAGS := -Wl,--hash-style=sysv
endif

include $(BUILD_SHARED_LIBRARY)
