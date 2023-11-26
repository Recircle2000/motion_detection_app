package com.example.visionproject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.core.content.ContextCompat;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;


import static org.opencv.core.CvType.CV_32FC2;
import static org.opencv.imgproc.Imgproc.COLOR_RGBA2GRAY;
import static org.opencv.imgproc.Imgproc.FONT_HERSHEY_SIMPLEX;
import static org.opencv.imgproc.Imgproc.rectangle;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllSafetyVisionModeActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "AndroidOpenCv";
    private CameraBridgeViewBase mCameraView;
    private boolean detect = true;
    private boolean start = true;
    private boolean trigger = true;
    boolean istogle = false;
    Queue<Mat> MatQueue2 = new LinkedList<>();
    Mat[] frames = new Mat[3];
    Mat removeMat = new Mat();
    Mat diff = new Mat();
    private Rect Uroi = new Rect(0, 0, 0, 0);
    Mat roiMat = new Mat();
    List<MatOfPoint> contours = new ArrayList<>();
    Mat hierarchy = new Mat();
    Mat motionMat = new Mat();
    Mat rgbaMat = new Mat();
    Mat displayMat = new Mat();
    Mat black = new Mat();
    UserRetrofitInterface userRetrofitInterface;
    String receivedToken;

    // public native long ConvertRGBtoGray(long matAddrInput1, long matAddrInput2, long matAddrInput3);

    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this);
        FirebaseAnalytics.getInstance(this);
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String receivedToken = intent.getStringExtra("token");
        Notification notification = new Notification(receivedToken);
        Log.d(TAG,  receivedToken);

        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Log.d(TAG, "onCreate: Starting cameraViewActivity");
        setContentView(R.layout.activity_all_safety_mode);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasPermissions(PERMISSIONS)) {
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }

        mCameraView = (CameraBridgeViewBase) findViewById(R.id.activity_AllSafe_view);
        mCameraView.setVisibility(SurfaceView.VISIBLE);
        //mCameraView.setMaxFrameSize(1280, 720);
        mCameraView.setCvCameraViewListener(this);
        mCameraView.setCameraIndex(99);
        mCameraView.setCameraPermissionGranted();



        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Switch switch2 = findViewById(R.id.switch2);

        switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                if (isChecked) {
                    istogle = true;
                } else {
                    istogle = false;
                }
            }
        });

    }

    private static final int CAMERA_PERMISSION_CODE = 200;

    @Override
    protected void onStart() {
        super.onStart();
        boolean _Permission = true; //변수 추가
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//최소 버전보다 버전이 높은지 확인
            if (checkSelfPermission(CAMERA_SERVICE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{CAMERA_SERVICE}, CAMERA_PERMISSION_CODE);
                _Permission = false;
            }
        }
        if (_Permission) {
            onCameraPermissionGranted();


        }
    }

    protected void onCameraPermissionGranted() {
        List<? extends CameraBridgeViewBase> cameraViews = getCameraViewList();
        if (cameraViews == null) {
            return;
        }
        for (CameraBridgeViewBase cameraBridgeViewBase : cameraViews) {
            if (cameraBridgeViewBase != null) {
                cameraBridgeViewBase.setCameraPermissionGranted();
            }
        }
    }

    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mCameraView);
    }


    @Override
    public void onPause() {
        super.onPause();
        if (mCameraView != null)
            mCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    private final BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.d(TAG, "정상");
                mCameraView.enableView();
            } else {
                Log.d(TAG, "에러");
                super.onManagerConnected(status);
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mCameraView != null) {
            mCameraView.disableView();
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.d(TAG, "onCameraViewStarted: Camera dimensions - Width: " + width + ", Height: " + height);
    }

    @Override
    public void onCameraViewStopped() {
        Log.d(TAG, "카메라 뷰 정지");
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        rgbaMat.release();
        displayMat.release();
        //원본 : rgbaMat, Roi그린거 : roiMat, 움직임 감지 : motionMat
        rgbaMat = inputFrame.rgba();
        displayMat = rgbaMat.clone();
        processDetect(rgbaMat);
        processTrigger();

        int diff_cnt = Core.countNonZero(motionMat);
        //Log.d(TAG, "diff_cnt : " + diff_cnt);
        if (istogle) //토글에 따라 모드가 바뀜
            return motionMat; // 흑백 감지표시
        else return displayMat; // 감지 영역표시
    }

    private void processDetect(Mat inputColor) {
        black.release();
        Mat black = inputColor.clone();
        Imgproc.cvtColor(inputColor, black, COLOR_RGBA2GRAY);
        if (start) {
            start = false;
            // 큐가 비어있으면 같은 프레임3개를 일단 채움.
            // 얕은 복사로 인해 같은 주소를 참조하는 3개의 객체가 같이 삭제되는 현상을 방지하기 위해 최초 2개 프레임만 깊은복사.
            Log.d(TAG, "프레임 3개 채움");
            MatQueue2.offer(black.clone());
            MatQueue2.offer(black.clone());
            MatQueue2.offer(black);
        } else { //가장 오래된 프레임을 꺼내 메모지 해제 후 최근 프레임을 끼워넣어줌.
            removeMat = MatQueue2.remove();
            removeMat.release();
            MatQueue2.offer(black);
        }
        //각 프레임을 꺼내 배열에 임시 저장.
        frames[0] = MatQueue2.poll();
        frames[1] = MatQueue2.poll();
        frames[2] = MatQueue2.poll();


        //----------------여기부터 움직임 감지 알고리즘
        Mat diff1 = new Mat();
        Mat diff2 = new Mat();
        //각 프레임의 차이를 비교
        Core.absdiff(frames[0], frames[1], diff1);
        Core.absdiff(frames[1], frames[2], diff2);

        //배열 객체들을 모두 사용하였으므로 다시 큐에 저장.
        for (Mat frame : frames) {
            MatQueue2.offer(frame);
        }

        double meanDiff1 = Core.mean(diff1).val[0];
        double meanDiff2 = Core.mean(diff2).val[0];

        double thresh = ((meanDiff1 + meanDiff2) / 2.0)+10;
        Mat diff1_t = new Mat();
        Mat diff2_t = new Mat();

        //이진화
        Imgproc.threshold(diff1, diff1_t, thresh, 255, Imgproc.THRESH_BINARY);
        Imgproc.threshold(diff2, diff2_t, thresh, 255, Imgproc.THRESH_BINARY);
        //이진화 한 결과물2개를 논리연산하여 두개의 결과가 모두 감지되었을 경우에만 표시
        Core.bitwise_and(diff1_t, diff2_t, diff);

        //커널 생성
        Mat kernelErode = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Imgproc.erode(diff, diff, kernelErode);

        // 많은 확장
        Mat kernelDilate = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Imgproc.dilate(diff, diff, kernelDilate);
        //morphology연산, 침식으로 작은 노이즈 제거 후 확장
        //Imgproc.morphologyEx(diff, diff, Imgproc.MORPH_OPEN, k);
        diff.copyTo(motionMat);

        //램 점유 해제
        hierarchy.release();
        diff.release();
        diff1.release();
        diff2.release();
        diff1_t.release();
        diff2_t.release();
        kernelErode.release();
        kernelDilate.release();
    }
    long detectionStartTime = 0;
    long detectionDurationThreshold = 3000; //ms
    long lastDetectionTime = 0;
    long noDetectionCooldown = 5000; //ms

    String detectText = "";
    Scalar detectScalar = new Scalar(200,200,200);
    private void processTrigger() {
        //초기화
        contours.clear();
        //객체 외각 탐지
        Imgproc.findContours(motionMat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        List<Rect> boundingRects = new ArrayList<>();
        //contours를 기준으로 사각형을 그림
        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);
            //일정 크기 이하의 사각형 무시
            if (area > 50) {
                //contour의 꼭짓점들을 찾음.
                MatOfPoint2f points = new MatOfPoint2f();
                contour.convertTo(points, CV_32FC2);
                //꼭짓점들을 기준으로 초기 사각형을 그림.
                Rect rect = Imgproc.boundingRect(points);
                boundingRects.add(rect);
            }
        }
        //겹치는 사각형 병합
        List<Rect> mergedRectangles = mergeRectangles(boundingRects, 1);
        //주변 사각형 병합
        List<Rect> finalRectangles = mergeAdjacentRectangles(mergedRectangles, 1000);


        for (Rect rect : finalRectangles) {
            if (rect.area() > 100000) { // 제일 큰 사각형의 넓이가 50000 이상일 경우 트리거.
                long currentTime = System.currentTimeMillis();
                //반복 실행 방지. 감지된 객체가 사라져야 다시 활성화
                if (detect) {
                    // 움직임이 처음 감지된 경우
                    detect = false;
                    detectionStartTime = currentTime;
                    detectText = "detected!";
                    detectScalar = new Scalar(200,200,200);
                    Log.d(TAG, "Motion detected!");
                } else

                if (trigger && currentTime - detectionStartTime > detectionDurationThreshold) {
                    // 움직임이 지속되고, 지정된 지속 시간 이상일 경우 트리거
                    trigger = false;
                    Log.d(TAG, "Triggered!!" + SettingsUtil.getAlartEnabled(this));
                    detectScalar = new Scalar(200,0,0);
                    if (SettingsUtil.getAlartEnabled(this)) {
                        // 알림 활성화된 경우
                        Notification.sendPushNotification("안전 카메라","움직임이 발견되었습니다!");
                    }

                    detectText = "Triggered!!";
                    lastDetectionTime = currentTime;
                }
            }
            Imgproc.putText(displayMat, detectText, new Point(20,200),FONT_HERSHEY_SIMPLEX,2, detectScalar,5);
            //출력
            Imgproc.rectangle(displayMat, rect.tl(), rect.br(), new Scalar(0, 0, 255), 2);
        }
        long currentTime = System.currentTimeMillis();
        //사각형 영역이 비었고, 5초가 지났을 때에만 트리거 재설정.
        if (finalRectangles.isEmpty() && currentTime - lastDetectionTime > noDetectionCooldown) {
            // 아무 것도 감지되지 않았을 때 trigger 재설정
            detect  = true;
            detectText = "Cooldown";
            trigger = true;

        }
    }
    //인접한 사각형 병합 구현
    private static List<Rect> mergeAdjacentRectangles(List<Rect> rects, double maxDistance) {
        List<Rect> mergedRectangles = new ArrayList<>();

        for (Rect rect : rects) {
            boolean merged = false;

            for (Rect existingRect : mergedRectangles) {
                double distance = calculateDistance(rect, existingRect);

                if (distance < maxDistance) {
                    // Merge rectangles
                    existingRect.x = Math.min(rect.x, existingRect.x);
                    existingRect.y = Math.min(rect.y, existingRect.y);
                    existingRect.width = Math.max(rect.x + rect.width, existingRect.x + existingRect.width) - existingRect.x;
                    existingRect.height = Math.max(rect.y + rect.height, existingRect.y + existingRect.height) - existingRect.y;

                    merged = true;
                    break;
                }
            }

            if (!merged) {
                mergedRectangles.add(rect);
            }
        }

        return mergedRectangles;
    }
    //사각형 거리 계산
    private static double calculateDistance(Rect rect1, Rect rect2) {
        Point center1 = new Point(rect1.x + rect1.width / 2.0, rect1.y + rect1.height / 2.0);
        Point center2 = new Point(rect2.x + rect2.width / 2.0, rect2.y + rect2.height / 2.0);

        return Math.sqrt(Math.pow(center1.x - center2.x, 2) + Math.pow(center1.y - center2.y, 2));
    }

    //겹치는 사각형 병합 구현
    private static List<Rect> mergeRectangles(List<Rect> rects, double overlapThreshold) {
        List<Rect> mergedRectangles = new ArrayList<>();

        for (Rect rect : rects) {
            boolean merged = false;

            for (Rect existingRect : mergedRectangles) {
                double overlap = calculateOverlap(rect, existingRect);

                if (overlap > overlapThreshold) {
                    // Merge rectangles
                    existingRect.x = Math.min(rect.x, existingRect.x);
                    existingRect.y = Math.min(rect.y, existingRect.y);
                    existingRect.width = Math.max(rect.x + rect.width, existingRect.x + existingRect.width) - existingRect.x;
                    existingRect.height = Math.max(rect.y + rect.height, existingRect.y + existingRect.height) - existingRect.y;

                    merged = true;
                    break;
                }
            }

            if (!merged) {
                mergedRectangles.add(rect);
            }
        }

        return mergedRectangles;
    }

    //겹치는 사각형 위치계산
    private static double calculateOverlap(Rect rect1, Rect rect2) {
        int intersectionArea = Math.max(0, Math.min(rect1.x + rect1.width, rect2.x + rect2.width) - Math.max(rect1.x, rect2.x)) *
                Math.max(0, Math.min(rect1.y + rect1.height, rect2.y + rect2.height) - Math.max(rect1.y, rect2.y));

        int area1 = rect1.width * rect1.height;
        int area2 = rect2.width * rect2.height;

        return (double) intersectionArea / Math.min(area1, area2);
    }
    //펄미션
    static final int PERMISSIONS_REQUEST_CODE = 1000;
    String[] PERMISSIONS = {"android.permission.CAMERA"};

    private boolean hasPermissions(String[] permissions) {
        int result;
        for (String perms : permissions) {
            result = ContextCompat.checkSelfPermission(this, perms);
            if (result == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }


    @SuppressLint("SuspiciousIndentation")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d(TAG, "onRequestPermissionsResult: Permission request result received.");

        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean cameraPermissionAccepted = grantResults[0]
                        == PackageManager.PERMISSION_GRANTED;

                if (!cameraPermissionAccepted)
                    Log.e(TAG, "onRequestPermissionsResult: Camera permission denied.");
                showDialogForPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");
            }
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(AllSafetyVisionModeActivity.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
            }
        });
        builder.create().show();
    }
}