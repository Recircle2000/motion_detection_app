package com.example.visionproject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;


import static org.opencv.imgproc.Imgproc.COLOR_RGBA2GRAY;
import static org.opencv.imgproc.Imgproc.rectangle;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class cameraViewActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, View.OnTouchListener{
    private static final String TAG = "AndroidOpenCv";
    private CameraBridgeViewBase mCameraView;

    private Mat mInputMat;
    private Mat mResultMat;
    private int mMode;

    Queue<Mat> MatQueue2 = new LinkedList<>();

    private Rect roi = new Rect(0,0,0,0); // roi 초기화
    Mat frame1 = new Mat();
    Mat frame2 = new Mat();
    Mat frame3 = new Mat();
    Mat diff = new Mat();

    Mat rgbaMat = new Mat();


    public native long ConvertRGBtoGray(long matAddrInput1, long matAddrInput2, long matAddrInput3);

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
        Intent intent = getIntent();
        mMode = intent.getIntExtra("mode", 0);

        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Log.d(TAG, "onCreate: Starting cameraViewActivity");
        setContentView(R.layout.activity_cameraview);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasPermissions(PERMISSIONS)) {
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }

        mCameraView = (CameraBridgeViewBase) findViewById(R.id.activity_surface_view);
        mCameraView.setVisibility(SurfaceView.VISIBLE);
        //mCameraView.setMaxFrameSize(1280, 720);
        mCameraView.setCvCameraViewListener(this);
        mCameraView.setOnTouchListener(this);
        mCameraView.setCameraIndex(1);
        mCameraView.setCameraPermissionGranted();

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
        rgbaMat = inputFrame.rgba();
        Imgproc.cvtColor(rgbaMat, rgbaMat, COLOR_RGBA2GRAY);
        MatQueue2 = processqueue(processRoi(rgbaMat));
        diff.release();
        return processRoi(rgbaMat);
    }

    private Mat processRoi(Mat rgbaMat){
        if (roi.x != 0 && roi.width > 0 && roi.height > 0){
            Mat RoiImage = new Mat(rgbaMat, roi);
            Core.bitwise_not(RoiImage, RoiImage);
            Imgproc.rectangle(rgbaMat,roi, new Scalar(0,255,0),4);
            Log.d(TAG, "컨버팅 Roi x: " + roi.x + ",y: " + roi.y + ",width: " + roi.width + ",height: " + roi.height );
            return rgbaMat;
        } else{
            Log.d(TAG,"roi처리안됨");
            return rgbaMat;
        }
    }


    private Mat processframe(){

        Mat diff1 = new Mat();
        Mat diff2 = new Mat();

        Core.absdiff(frame1, frame2, diff1);
        Core.absdiff(frame2, frame3, diff2);

        double thresh = 30;
        Mat diff1_t = new Mat();
        Mat diff2_t = new Mat();

        Imgproc.threshold(diff1, diff1_t, thresh, 255, Imgproc.THRESH_BINARY);
        Imgproc.threshold(diff2, diff2_t, thresh, 255, Imgproc.THRESH_BINARY);
        Core.bitwise_and(diff1_t, diff2_t, diff);
        Size size = new Size(3,3);
        Mat k;
        k = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, size);
        Imgproc.morphologyEx(diff, diff, Imgproc.MORPH_OPEN, k);

        diff1.release();
        diff2.release();
        diff1_t.release();
        diff2_t.release();
        k.release();
// 폭과 높이를 각각 얻기
        Log.d(TAG, "onCameraFrame: Processing frame...");
        //움직임 감지 연산을 위해 프레임 3개를 한번에 보냄.
        //rgbaMat = new Mat(ConvertRGBtoGray(frame1.getNativeObjAddr(), frame2.getNativeObjAddr(), frame3.getNativeObjAddr()));
        //출력
        return diff;
    }
    private Queue<Mat> processqueue(Mat input) {

        if (MatQueue2.isEmpty()) { // 큐가 비어있으면 같은 프레임3개를 일단 채움.
            MatQueue2.offer(input);
            MatQueue2.offer(input);
            MatQueue2.offer(input);
        } else { // 아니면 앞에있는 프레임1개를 빼고 뒤에다가 프레임을 채움.
            MatQueue2.remove();
            MatQueue2.offer(input);
        }
        frame1 = MatQueue2.poll();
        frame2 = MatQueue2.poll();
        frame3 = MatQueue2.poll();

        MatQueue2.offer(frame1); // 비워진 큐를 다시 순서대로 채워넣음.
        MatQueue2.offer(frame2);
        MatQueue2.offer(frame3);

        return MatQueue2;
    }
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        float x = motionEvent.getX();
        float y = motionEvent.getY();

        switch (action){
            case MotionEvent.ACTION_DOWN:
                roi.width = 0;
                roi.height = 0;
                roi.x = (int)x;
                roi.y = (int)y;
                Log.d(TAG, "down" + roi.x + " " + roi.y + "");
                break;
            case MotionEvent.ACTION_MOVE:
                roi.width = (int)x - roi.x;
                roi.height = (int)y - roi.y;
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
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

        AlertDialog.Builder builder = new AlertDialog.Builder(cameraViewActivity.this);
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