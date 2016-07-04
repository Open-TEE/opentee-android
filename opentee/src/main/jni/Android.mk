MY_PATH := $(call my-dir)

ifndef NDK_ROOT
include external/stlport/libstlport.mk
endif

LOCAL_PATH := $(MY_PATH)
include $(call all-subdir-makefiles)
include $(CLEAR_VARS)

LOCAL_PATH := $(MY_PATH)

local_shared_libraries := libtee libgpdatatypes
LOCAL_STATIC_LIBRARIES += libprotobuf
local_cflags := -DANDROID_NDK

ifeq ($(TARGET_ARCH),arm)
local_ldflags := -Wl,--hash-style=sysv
else
local_ldflags :=
endif

local_ldlibs :=  -lz -llog #-L$(LOCAL_PATH)/../obj/local/$(TARGET_ARCH_ABI)/ -lGPDataTypes

local_src_files :=  LibteeWrapper.cpp

local_c_includes := $(LOCAL_PATH)/libtee/include \
                    $(LOCAL_PATH)/protobuf/src/ \
                    $(LOCAL_PATH) \
                    external/zlib

#################################################
# Target dynamic library

include $(CLEAR_VARS)
LOCAL_CPPFLAGS := -std=c++11
LOCAL_SRC_FILES := $(local_src_files)
LOCAL_C_INCLUDES += $(local_c_includes)
LOCAL_CFLAGS += $(local_cflags)
LOCAL_LDFLAGS += $(local_ldflags)
LOCAL_LDLIBS += $(local_ldlibs)
LOCAL_SHARED_LIBRARIES += libc $(local_shared_libraries)
LOCAL_MODULE := nativelibtee-jni
LOCAL_MODULE_TAGS := optional
include $(BUILD_SHARED_LIBRARY)

###############################################
# Target static library

include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(local_src_files)
LOCAL_C_INCLUDES += $(local_c_includes)
LOCAL_CFLAGS += $(local_cflags)
LOCAL_LDLIBS += $(local_ldlibs)
LOCAL_STATIC_LIBRARIES += libc
LOCAL_SHARED_LIBRARIES += $(local_shared_libraries)
LOCAL_MODULE := nativelibtee-jni_static
LOCAL_MODULE_TAGS := optional
include $(BUILD_STATIC_LIBRARY)
