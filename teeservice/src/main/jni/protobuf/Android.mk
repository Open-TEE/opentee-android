LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE    := libprotobuf

LOCAL_CPP_EXTENSION := .cc
LOCAL_SRC_FILES :=\
	src/google/protobuf/descriptor_database.cc \
	src/google/protobuf/descriptor.cc \
	src/google/protobuf/descriptor.pb.cc \
	src/google/protobuf/dynamic_message.cc \
	src/google/protobuf/extension_set_heavy.cc \
	src/google/protobuf/extension_set.cc \
	src/google/protobuf/generated_message_reflection.cc \
	src/google/protobuf/generated_message_util.cc \
	src/google/protobuf/io/coded_stream.cc \
	src/google/protobuf/io/gzip_stream.cc \
	src/google/protobuf/io/printer.cc \
	src/google/protobuf/io/strtod.cc \
	src/google/protobuf/io/tokenizer.cc \
	src/google/protobuf/io/zero_copy_stream_impl_lite.cc \
	src/google/protobuf/io/zero_copy_stream_impl.cc \
	src/google/protobuf/io/zero_copy_stream.cc \
	src/google/protobuf/message_lite.cc \
	src/google/protobuf/message.cc \
	src/google/protobuf/reflection_ops.cc \
	src/google/protobuf/repeated_field.cc \
	src/google/protobuf/stubs/common.cc \
	src/google/protobuf/stubs/once.cc \
	src/google/protobuf/stubs/stringprintf.cc \
	src/google/protobuf/stubs/structurally_valid.cc \
	src/google/protobuf/stubs/strutil.cc \
	src/google/protobuf/stubs/substitute.cc \
	src/google/protobuf/text_format.cc \
	src/google/protobuf/unknown_field_set.cc \
	src/google/protobuf/wire_format_lite.cc \
	src/google/protobuf/wire_format.cc

#$(warning $(TARGET_ARCH))

ifeq ($(TARGET_ARCH),x86)
	LOCAL_SRC_FILES := $(LOCAL_SRC_FILES) \
										 src/google/protobuf/stubs/atomicops_internals_x86_gcc.cc
endif
ifeq ($(TARGET_ARCH),x86_64)
	LOCAL_SRC_FILES := $(LOCAL_SRC_FILES) \
										 src/google/protobuf/stubs/atomicops_internals_x86_gcc.cc
endif

LOCAL_CFLAGS := -D GOOGLE_PROTOBUF_NO_RTTI=1 #-iquote $(LOCAL_PATH)/src
LOCAL_CPPFLAGS += -std=c++11
LOCAL_C_INCLUDES = $(LOCAL_PATH)/src

LOCAL_EXPORT_LDLIBS := -lz
LOCAL_EXPORT_CFLAGS := $(LOCAL_CFLAGS)
LOCAL_EXPORT_CPPFLAGS := $(LOCAL_CPPFLAGS)
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_C_INCLUDES)

include $(BUILD_STATIC_LIBRARY)
