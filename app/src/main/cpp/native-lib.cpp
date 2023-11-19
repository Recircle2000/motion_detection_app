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
Java_com_example_visionproject_AllSafetyVisionModeActivity_ConvertAllSafe(JNIEnv *env, jobject thiz,
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
Java_com_example_visionproject_SafetyVisionModeActivity_ConvertSafe(JNIEnv *env, jobject thiz,
                                                                    jlong mat_addr_input, jint x,
                                                                    jint y, jint width, jint height) {
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

    return (jlong)new cv::Mat(inputImage);; // 화면출력 전용. 자른 이미지를 리턴하면 해상도 차이로 크래시 발생.
    // TODO: implement ConvertSafe()
}



/*#include <opencv2/opencv.hpp>
#include <opencv2/imgproc/imgproc.hpp>

using namespace cv;

int main()
{
    VideoCapture cap(0); // 웹캠에서 입력을 받습니다.
    if (!cap.isOpened())
    {
        return -1;
    }

    Mat frame, gray, frameDelta, thresh, firstFrame;
    vector<vector<Point> > cnts;
    cap >> firstFrame;
    cvtColor(firstFrame, firstFrame, COLOR_BGR2GRAY);
    GaussianBlur(firstFrame, firstFrame, Size(21, 21), 0);

    while (true)
    {
        cap >> frame;
        if (frame.empty())
            break;

        cvtColor(frame, gray, COLOR_BGR2GRAY);
        GaussianBlur(gray, gray, Size(21, 21), 0);

        absdiff(firstFrame, gray, frameDelta);
        threshold(frameDelta, thresh, 25, 255, THRESH_BINARY);

        dilate(thresh, thresh, Mat(), Point(-1,-1), 2);
        findContours(thresh.clone(), cnts, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);

        for (int i = 0; i < cnts.size(); i++)
        {
            if (contourArea(cnts[i]) < 500)
            {
                continue;
            }

            putText(frame, "Motion Detected", Point(10, 20), FONT_HERSHEY_SIMPLEX, 0.75, Scalar(0,0,255),2);
        }

        imshow("Camera", frame);

        if (waitKey(1) == 27)
        {
            // ESC 키를 누르면 루프에서 빠져나옵니다.
            break;
        }
    }

    return 0;
}*/


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