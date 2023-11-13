#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>

using namespace std;
using namespace cv;

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_visionproject_MainMenu_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {

    string hello = "Hello from C++ and Test";
    return env->NewStringUTF(hello.c_str());
}


extern "C"
JNIEXPORT void JNICALL
Java_com_example_visionproject_cameraViewActivity_ConvertRGBtoGray(JNIEnv *env, jobject thiz,
                                                                   jlong mat_addr_input,
                                                                   jlong mat_addr_result) {


    // TODO: implement ConvertRGBtoGray()
}