LOCAL_PATH := $(call my-dir)

###########################
# Rust compilation outputs
###########################

include $(CLEAR_VARS)
LOCAL_MODULE := sunny-rs
LOCAL_EXPORT_C_INCLUDES := src/core/include
LOCAL_LIB = ../out/$(TARGET_ARCH_ABI)/libsunny-rs.a
ifneq (,$(wildcard $(LOCAL_PATH)/$(LOCAL_LIB)))
LOCAL_SRC_FILES := $(LOCAL_LIB)
include $(PREBUILT_STATIC_LIBRARY)
else
include $(BUILD_STATIC_LIBRARY)
endif

include $(CLEAR_VARS)
LOCAL_MODULE := boot-rs
LOCAL_LIB = ../out/$(TARGET_ARCH_ABI)/libsunnyboot-rs.a
ifneq (,$(wildcard $(LOCAL_PATH)/$(LOCAL_LIB)))
LOCAL_SRC_FILES := $(LOCAL_LIB)
include $(PREBUILT_STATIC_LIBRARY)
else
include $(BUILD_STATIC_LIBRARY)
endif

include $(CLEAR_VARS)
LOCAL_MODULE := init-rs
LOCAL_LIB = ../out/$(TARGET_ARCH_ABI)/libsunnyinit-rs.a
ifneq (,$(wildcard $(LOCAL_PATH)/$(LOCAL_LIB)))
LOCAL_SRC_FILES := $(LOCAL_LIB)
include $(PREBUILT_STATIC_LIBRARY)
else
include $(BUILD_STATIC_LIBRARY)
endif

include $(CLEAR_VARS)
LOCAL_MODULE := policy-rs
LOCAL_LIB = ../out/$(TARGET_ARCH_ABI)/libsunnypolicy-rs.a
ifneq (,$(wildcard $(LOCAL_PATH)/$(LOCAL_LIB)))
LOCAL_SRC_FILES := $(LOCAL_LIB)
include $(PREBUILT_STATIC_LIBRARY)
else
include $(BUILD_STATIC_LIBRARY)
endif
