//
// Created by wanghonggang on 2017/3/6.
//
#include <string.h>
#include <assert.h>
#include <stdlib.h>
#include "LogUtils.h"
#include "com_example_wanghg_bitmaptest_NdkJniUtil.h"

JNIEXPORT jstring JNICALL getCLanguageString(JNIEnv *env, jobject obj) {
    return (*env)->NewStringUTF(env, "this is jni test !!!");
}

JNIEXPORT jint JNICALL calAAddB(JNIEnv *env, jobject obj, jint a, jint b) {
    LOGI("%d", a + b);
    return (a + b);
}

JNIEXPORT jint JNICALL getRandom(JNIEnv *env, jobject obj, jint i) {
    return (rand() % i);
}

static JNINativeMethod methods[] = {
        {"getCLanguageString", "()Ljava/lang/String;", (void *) getCLanguageString},
        {"calAAddB",           "(II)I",                (void *) calAAddB},
        {"getRandom",          "(I)I",                 (void *) getRandom},
};

static int registerNativeMethods(JNIEnv *env, const char *className, JNINativeMethod *gMethods,
                                 int numMethods) {
    jclass clazz;
    clazz = (*env)->FindClass(env, className);

    if (clazz == NULL) {
        LOGI("findclass failed !");
        return JNI_FALSE;
    }

    LOGI("register.... numMethods : %d ", numMethods);
    if ((*env)->RegisterNatives(env, clazz, gMethods, numMethods) != JNI_OK) {
        LOGI("register.... failed !");
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

static int registerNatives(JNIEnv *env) {
    const char *kClassName = "com/example/wanghg/bitmaptest/NdkJniUtil";
    return registerNativeMethods(env, kClassName, methods, sizeof(methods) / sizeof(methods[0]));
}

//JNIEXPORT jint JNICALL JNI_Onload(JavaVM* vm, void* reserved)
JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = NULL;
    LOGI("JNI_Onload start !");

    if ((*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        LOGI("getenv failed !");
        return -1;
    }

    //assert(env != NULL);

    if (registerNatives(env) != JNI_TRUE) {
        LOGI("register natives failed !");
        return -1;
    }

    return JNI_VERSION_1_4;
}
