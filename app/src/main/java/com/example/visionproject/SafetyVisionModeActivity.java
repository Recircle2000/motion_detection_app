package com.example.visionproject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;

import androidx.core.content.ContextCompat;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;


import static org.opencv.imgproc.Imgproc.rectangle;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Collections;
import java.util.List;

public class SafetyVisionModeActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, View.OnTouchListener {
    private static final String TAG = "AndroidOpenCv";
    private CameraBridgeViewBase SafeCameraView;
    private Scalar rectColor;
    private Rect roi = new Rect(0,0,0,0); // roi 초기화
    private static final float ASPECT_RATIO_16_9 = 16.0f / 9.0f; // 터치 영역 좌표 차이로 인한 변환 사전 준비
    private RectF rect16x9; // 16:9 영역을 나타내는 사각형



    public native long ConvertSafe(long matAddrInput,int x, int y, int width, int height);

    public native long DrawROI(long matAddrInput,int x, int y, int width, int height);

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

        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate: Starting cameraViewActivity");
        setContentView(R.layout.activity_safety_mode);
        rect16x9 = calculate16x9Rect();

        Button initROIButton = findViewById(R.id.init_ROI);
        initROIButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                roi.x = 0;
                roi.y = 0;
                roi.width = 0;
                roi.height = 0;
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasPermissions(PERMISSIONS)) {
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }
        SafeCameraView = (CameraBridgeViewBase)findViewById(R.id.activity_safe_view);
        SafeCameraView.setVisibility(SurfaceView.VISIBLE);
        //SafeCameraView.setMaxFrameSize(displayMetrics.widthPixels,displayMetrics.heightPixels);
        SafeCameraView.setCvCameraViewListener(this);
        //SafeCameraView.setMaxFrameSize(1920, 1080);
        SafeCameraView.setCameraIndex(0);
        SafeCameraView.setOnTouchListener(this);
        SafeCameraView.setCameraPermissionGranted();
        Toast.makeText(getApplicationContext(), "위험 구역을 드래그 하여 지정해주세요! ",Toast.LENGTH_LONG).show();
        rectColor = new Scalar(255,0,0,255);
        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private static final int CAMERA_PERMISSION_CODE = 200;

    @Override
    protected void onStart() {
        super.onStart();

        boolean _Permission = true; //변수 추가
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){//최소 버전보다 버전이 높은지 확인
            if(checkSelfPermission(CAMERA_SERVICE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{CAMERA_SERVICE}, CAMERA_PERMISSION_CODE);
                _Permission = false;
            }
        }
        if(_Permission){
            onCameraPermissionGranted();


        }
    }


    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(SafeCameraView);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (SafeCameraView != null)
            SafeCameraView.disableView();
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

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.d(TAG, "정상");
                    SafeCameraView.enableView();
                }
                break;
                default: {
                    Log.d(TAG, "에러");
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (SafeCameraView != null) {
            SafeCameraView.disableView();
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

        //Log.d(TAG, "onCameraFrame: Processing frame...");
        Mat rgbaMat = inputFrame.rgba();
        long matAddr;
        long matROI;
        if (roi.height != 0 && roi.width > 0 && roi.height > 0) { //roi 지정이 되었을경우.
            matAddr = DrawROI(rgbaMat.getNativeObjAddr(),roi.x, roi.y, roi.width, roi.height); // 원본 영상에 ROI 사각형 그리기
            rgbaMat = new Mat(matAddr);
            Mat test1 = rgbaMat;
            matROI = ConvertSafe(rgbaMat.getNativeObjAddr(),roi.x, roi.y, roi.width, roi.height); // roi영역 자르고 움직임 감지
            Log.d(TAG, "컨버팅 Roi x: " + roi.x + ",y: " + roi.y + ",width: " + roi.width + ",height: " + roi.height );
            rgbaMat = new Mat(matROI);
            return rgbaMat;
        } else {//roi지정이 안되었을경우.
            matAddr = ConvertSafe(rgbaMat.getNativeObjAddr(),roi.x, roi.y, roi.width, roi.height);
            rgbaMat = new Mat(matAddr);
            return rgbaMat;
        }

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        float x = motionEvent.getX();
        float y = motionEvent.getY();
        PointF convertedPoint = convertTo16x9Coordinates(x, y);

        switch (action){
            case MotionEvent.ACTION_DOWN:
                roi.width = 0;
                roi.height = 0;
                roi.x = (int)convertedPoint.x;
                roi.y = (int)convertedPoint.y;
                Log.d(TAG, "down" + roi.x + " " + roi.y + "");
                break;
            case MotionEvent.ACTION_MOVE:
                roi.width = (int)convertedPoint.x - roi.x;
                roi.height = (int)convertedPoint.y - roi.y;
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }

    private RectF calculate16x9Rect() {
        // 화면 크기 가져오기
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        // 16:9 비율에 따라 중앙 영역 계산
        float width = Math.min(screenWidth, screenHeight * ASPECT_RATIO_16_9);
        float height = width / ASPECT_RATIO_16_9;

        // 중앙 좌표 계산
        float centerX = screenWidth / 2.0f;
        float centerY = screenHeight / 2.0f;

        // 16:9 영역을 나타내는 RectF 반환
        return new RectF(centerX - width / 2.0f, centerY - height / 2.0f, centerX + width / 2.0f, centerY + height / 2.0f);
    }

    private PointF convertTo16x9Coordinates(float touchX, float touchY) {
        // 16:9 영역에서의 좌표로 변환
        float convertedX = touchX - rect16x9.left;
        float convertedY = touchY - rect16x9.top;
        Log.d(TAG, "convertTo16x9Coordinates" + convertedX + " " + convertedY);

        return new PointF(convertedX, convertedY);
    }

    // ...


    //펄미션
    protected void onCameraPermissionGranted() {
        List<? extends CameraBridgeViewBase> cameraViews = getCameraViewList();
        if (cameraViews == null) {
            return;
        }
        for (CameraBridgeViewBase cameraBridgeViewBase: cameraViews) {
            if (cameraBridgeViewBase != null) {
                cameraBridgeViewBase.setCameraPermissionGranted();
            }
        }
    }
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

        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean cameraPermissionAccepted = grantResults[0]
                            == PackageManager.PERMISSION_GRANTED;

                    if (!cameraPermissionAccepted)
                        Log.e(TAG, "onRequestPermissionsResult: Camera permission denied.");
                    showDialogForPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");
                }
                break;
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(SafetyVisionModeActivity.this);
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