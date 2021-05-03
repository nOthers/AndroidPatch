#include <jni.h>
#include <cstdio>
#include <cstring>
#include "dobby.h"

JavaVM *JVM;
jobject mOnSharedObjectLoadedListener;

void onSharedObjectLoaded(const char *name) {
    JavaVM *vm = JVM;
    jobject listener = mOnSharedObjectLoadedListener;
    JNIEnv *env;
    int gotEnv = vm->GetEnv((void **) &env, JNI_VERSION_1_6);
    if (gotEnv == JNI_EDETACHED) {
        vm->AttachCurrentThread(&env, nullptr);
    }
    if (listener) {
        jclass cls = env->GetObjectClass(listener);
        jmethodID method = env->GetMethodID(cls, "onSharedObjectLoaded", "(Ljava/lang/String;)V");
        env->CallVoidMethod(listener, method, env->NewStringUTF(name));
    }
    if (gotEnv == JNI_EDETACHED) {
        vm->DetachCurrentThread();
    }
}

void *(*origin_do_dlopen_v23)(const char *name, int flags, const void *extinfo);

void *replace_do_dlopen_v23(const char *name, int flags, const void *extinfo) {
    void *handle = origin_do_dlopen_v23(name, flags, extinfo);
    onSharedObjectLoaded(name);
    return handle;
}

extern "C" JNIEXPORT void JNICALL Java_utopia_android_patch_killer_Killer_setOnSharedObjectLoadedListener(
        JNIEnv *env,
        jclass cls,
        jobject listener
) {
    env->GetJavaVM(&JVM);
    if (mOnSharedObjectLoadedListener) {
        env->DeleteGlobalRef(mOnSharedObjectLoadedListener);
    }
    mOnSharedObjectLoadedListener = env->NewGlobalRef(listener);
    if (!origin_do_dlopen_v23) {
        void *do_dlopen_v23 = DobbySymbolResolver(nullptr, "__dl__Z9do_dlopenPKciPK17android_dlextinfo");
        DobbyHook(do_dlopen_v23, (void *) replace_do_dlopen_v23, (void **) &origin_do_dlopen_v23);
    }
}

bool get_shared_object_maps(const char *needle, uint32_t *start, uint32_t *end) {
    bool got = false;
    uint32_t left, right;
    char line[1024], path[1024];
    FILE *fp = fopen("/proc/self/maps", "r");
    if (fp != nullptr) {
        while (fgets(line, sizeof(line), fp)) {
            strcpy(path, "");
            sscanf(line, "%x-%x %*s %*x %*x:%*x %*u %s\n", &left, &right, path);
            if (left > right) {
                continue;
            }
            if (strstr(path, needle)) {
                if (!got) {
                    got = true;
                    *start = left;
                    *end = right;
                } else {
                    if (*start > left) {
                        *start = left;
                    }
                    if (*end < right) {
                        *end = right;
                    }
                }
            }
        }
        fclose(fp);
    }
    return got;
}

extern "C" JNIEXPORT jbyteArray JNICALL Java_utopia_android_patch_killer_Killer_dumpSharedObject(
        JNIEnv *env,
        jclass cls,
        jstring filename
) {
    uint32_t start, end;
    const char *filenameUTF = env->GetStringUTFChars(filename, 0);
    if (!get_shared_object_maps(filenameUTF, &start, &end)) {
        start = 0;
        end = 0;
    }
    env->ReleaseStringUTFChars(filename, filenameUTF);
    jbyte *buf = (jbyte *) start;
    jsize length = end - start;
    jbyteArray array = env->NewByteArray(length);
    env->SetByteArrayRegion(array, 0, length, buf);
    return array;
}
