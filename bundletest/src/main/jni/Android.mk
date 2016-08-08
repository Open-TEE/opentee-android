MY_PATH := $(call my-dir)

ifndef NDK_ROOT
include external/stlport/libstlport.mk
endif

LOCAL_PATH := $(MY_PATH)

$(warning $(LOCAL_PATH))

include $(call all-subdir-makefiles)
include $(CLEAR_VARS)