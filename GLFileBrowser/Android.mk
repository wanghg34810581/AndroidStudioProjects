ifeq ("$(TYAPP_TYFILEBROWSER_SUPPORT)","true")

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_JAVA_LIBRARIES := ext

LOCAL_PACKAGE_NAME := TYFileBrowser

LOCAL_OVERRIDES_PACKAGES := FileExplorer CMFileManager
LOCAL_PROGUARD_ENABLED := disabled

#LOCAL_STATIC_JAVA_LIBRARIES := android-support-v13 android-support-v4
LOCAL_STATIC_JAVA_LIBRARIES := libv4 libv13

include $(BUILD_PACKAGE)



include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := libv4:libs/android-support-v4.jar \
                                        libv13:libs/android-support-v13.jar
include $(BUILD_MULTI_PREBUILT)


# Use the following include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))

endif
