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
JNIEXPORT jlong JNICALL
Java_com_example_visionproject_cameraViewActivity_ConvertRGBtoGray(JNIEnv *env, jobject thiz,
                                                                   jlong mat_addr_input) {
    Mat &inputImage = *(Mat *) mat_addr_input;

    //예시
    cvtColor(inputImage, inputImage, COLOR_RGBA2GRAY);
    Canny(inputImage,inputImage,50,150);
    return mat_addr_input;
    // TODO: implement ConvertRGBtoGray()
}


//전체 영역 감지 모드 구현
extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_visionproject_AllSafetyModeActivity_ConvertAllSafe(JNIEnv *env, jobject thiz,
                                                                    jlong mat_addr_input) {

    Mat &inputImage = *(Mat *) mat_addr_input;

    //예시
    cvtColor(inputImage, inputImage, COLOR_RGBA2GRAY);

    return mat_addr_input;

    // TODO: implement ConvertAllSafe()
}