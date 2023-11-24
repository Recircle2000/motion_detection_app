#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>
#include <android/log.h>

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
JNIEXPORT jlongArray JNICALL
Java_com_example_visionproject_AllSafetyVisionModeActivity_ConvertRGBtoGray(JNIEnv *env, jobject thiz,
                                                                   jlong mat_addr_input1,jlong mat_addr_input2,jlong mat_addr_input3) {
    //받아온 프레임3개를 각각 inputImage에 저장
    Mat &inputImage1 = *(Mat *) mat_addr_input1;
    Mat &inputImage2 = *(Mat *) mat_addr_input2;
    Mat &inputImage3 = *(Mat *) mat_addr_input3;

    //차이점 계산을 위한 임시 Mat 클래스 및 결과물 Mat 클래스.
    Mat diff1, diff2, diff;
    //1-2 차이점 계산 후, 2-3차이점 계산.
    absdiff(inputImage1,inputImage2,diff1);
    absdiff(inputImage2,inputImage3,diff2);
    double thresh = 30;
    Mat diff1_t, diff2_t;
    // diff1에 대한 이진화 수행
    threshold(diff1, diff1_t, thresh, 255, THRESH_BINARY);

    // diff2에 대한 이진화 수행
    threshold(diff2, diff2_t, thresh, 255, THRESH_BINARY);

    //이진화 수행한 1-2, 2-3 프레임을 비트연산으로 합침.
    bitwise_and(diff1_t,diff2_t,diff);

    //작은 노이즈 제거
    Mat k = getStructuringElement(MORPH_CROSS,Size(3,3));
    morphologyEx(diff, diff, MORPH_OPEN,k);

    return (jlong)new cv::Mat(diff);
    // TODO: implement ConvertRGBtoGray()
}


//전체 영역 감지 모드 구현
extern "C" JNIEXPORT jlong JNICALL
Java_com_example_visionproject_AllSafetyVisionModeActivity_ConvertAllSafe(JNIEnv *env, jobject thiz, jlong mat_addr_input)
{
    Mat &inputImage = *(Mat *) mat_addr_input; // 원본
    Mat RoiImage;

    // 로그 출력
    __android_log_print(ANDROID_LOG_INFO, "SafeMode", "원본 사이즈 %d x %d", inputImage.size().width,inputImage.size().height);

    cvtColor(inputImage, RoiImage, COLOR_RGBA2GRAY);

    static Mat prevRoiImage; // 이전 프레임을 저장
    Mat diff;
    if (!prevRoiImage.empty()) {
        absdiff(RoiImage, prevRoiImage, diff);
    }
    prevRoiImage = RoiImage.clone();

    if (!diff.empty() && countNonZero(diff) > 0) {
        return 1;
    } else {
        return 0;
    }
}





//안전구역 감지모드 구현
extern "C"
JNIEXPORT jlongArray JNICALL
Java_com_example_visionproject_AllSafetyVisionModeActivity_ConvertAllSafeWithROI(JNIEnv *env, jobject thiz, jlong mat_addr_input, jint x, jint y, jint width, jint height)
{

    Rect roiRect(x, y, width, height); // 관심영역 좌표

    Mat &inputImage = *(Mat *) mat_addr_input; // 원본
    Mat displayImage = inputImage.clone(); // 출력전용
    Mat RoiImage;
    //roi지정이 되었을 경우에만 원본 이미지를 자름
    if (roiRect.x != 0 && roiRect.y != 0 && roiRect.width > 0 && roiRect.height > 0) {
        RoiImage = inputImage(roiRect); // 원본 자른거
        //RoiImage = ~RoiImage; //디버그용
        //cvtColor(RoiImage, RoiImage, COLOR_RGBA2GRAY);
        __android_log_print(ANDROID_LOG_INFO, "SafeMode", " roi 지정됨. 원본 사이즈 %d x %d 자른거 사이즈 %d x %d",
                            inputImage.size().width,inputImage.size().height ,RoiImage.size().width,RoiImage.size().height);
    } else { //roi지정이 안되었을 경우 자르지 않고 그대로 얕은복사.
        RoiImage = inputImage;
        __android_log_print(ANDROID_LOG_INFO, "SafeMode", " roi 지정안됨. 원본 사이즈 %d x %d 자른거 사이즈 %d x %d",
                            inputImage.size().width,inputImage.size().height ,RoiImage.size().width,RoiImage.size().height);
    }
    // 소스 영상 Mat클래스 inputImage 대신 RoiImage로 이름만 바꿔서 개발하면 자른 이미지에 대한 움직임 감지 가능\
    // 움직임 감지 연산만 RoiImage에서 진행.
    cvtColor(RoiImage, RoiImage, COLOR_RGBA2GRAY);

    //return (jlong)new cv::Mat(inputImage);; // 화면출력 전용. 자른 이미지를 리턴하면 해상도 차이로 크래시 발생.
    // TODO: implement ConvertSafe()
    // 움직임 감지 여부와 결과 Mat 주소를 저장할 jlongArray 생성
    static Mat prevRoiImage; // 이전 프레임을 저장
    Mat diff;
    if (!prevRoiImage.empty()) {
        absdiff(RoiImage, prevRoiImage, diff);
    }
    prevRoiImage = RoiImage.clone();

    jlongArray result = env->NewLongArray(2);

    if (countNonZero(diff) > 0) {
        __android_log_print(ANDROID_LOG_INFO, "Motion Detection", "Motion detected");
        jlong temp[2] = {1, (jlong)new cv::Mat(diff)};
        env->SetLongArrayRegion(result, 0, 2, temp);
    } else {
        jlong temp[2] = {0, (jlong)new cv::Mat(diff)};
        env->SetLongArrayRegion(result, 0, 2, temp);
    }

    return result;

}




extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_visionproject_SafetyVisionModeActivity_DrawROI(JNIEnv *env, jobject thiz, jlong mat_addr_input, jint x,
                                                                jint y, jint width, jint height) {

    Rect roiRect(x, y, width, height);
    Mat &inputImage = *(Mat *) mat_addr_input;

    rectangle(inputImage, roiRect, Scalar(0,255,0),4);

   // inputImage=inputImage(roiRect);


    return mat_addr_input;
    // TODO: implement SetROI()
}