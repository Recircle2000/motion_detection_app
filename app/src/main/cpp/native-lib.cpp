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