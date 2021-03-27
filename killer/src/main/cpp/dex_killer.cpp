#include <jni.h>

extern "C" JNIEXPORT jbyteArray JNICALL Java_utopia_android_patch_killer_Killer_dumpDexFile_1v23(
        JNIEnv *env,
        jclass cls,
        jlong cookie
) {
    cookie += sizeof(void *);
    uint8_t *begin_ = *(uint8_t **) cookie;
    cookie += sizeof(uint8_t *);
    size_t size_ = *(size_t *) cookie;
    cookie += sizeof(size_t);
    jbyte *buf = (jbyte *) begin_;
    jsize length = size_;
    jbyteArray array = env->NewByteArray(length);
    env->SetByteArrayRegion(array, 0, length, buf);
    return array;
}
