LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_CPPFLAGS += -std=c++11

LOCAL_MODULE    := libgpdatatypes

LOCAL_C_INCLUDES = $(LOCAL_PATH)
LOCAL_CPP_EXTENSION := .cc
LOCAL_SRC_FILES := GPDataTypes.pb.cc
LOCAL_STATIC_LIBRARIES += libprotobuf
LOCAL_LDLIBS := -lz

include $(BUILD_SHARED_LIBRARY)