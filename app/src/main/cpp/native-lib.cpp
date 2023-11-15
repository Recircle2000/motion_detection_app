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


//안전구역 감지모드 구현
extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_visionproject_SafetyModeActivity_ConvertSafe(JNIEnv *env, jobject thiz,
                                                              jlong mat_addr_input) {

    Mat &inputImage = *(Mat *) mat_addr_input;
    cvtColor(inputImage, inputImage, COLOR_RGBA2GRAY);
    Mat noise(inputImage.size(), CV_32SC1);
    //예시
    randn(noise, 0, 10);

    add(inputImage, noise, inputImage, Mat(), CV_8U);
    return mat_addr_input;
    // TODO: implement ConvertSafe()
}