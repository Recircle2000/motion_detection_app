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
import android.view.SurfaceView;
import android.view.View;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import static org.opencv.imgproc.Imgproc.rectangle;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Collections;
import java.util.List;

public class SafetyModeActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "SafeMode";
    private CameraBridgeViewBase SafeCameraView;

    private Mat mInputMat;
    private Mat mResultMat;
    private int mMode;

    public native long ConvertSafe(long matAddrInput);

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
        setContentView(R.layout.activity_safety_mode);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasPermissions(PERMISSIONS)) {
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }

        SafeCameraView = (CameraBridgeViewBase)findViewById(R.id.activity_safe_view);
        SafeCameraView.setVisibility(SurfaceView.VISIBLE);
        //AllSafeCameraView.setMaxFrameSize(1280, 720);
        SafeCameraView.setCvCameraViewListener(this);
        SafeCameraView.setCameraIndex(0);
        SafeCameraView.setCameraPermissionGranted();
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
        Log.d(TAG, "onCameraFrame: Processing frame...");
        Mat rgbaMat = inputFrame.rgba();
        long matAddr = ConvertSafe(rgbaMat.getNativeObjAddr());
        rgbaMat = new Mat(matAddr);

        return rgbaMat;
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

        AlertDialog.Builder builder = new AlertDialog.Builder(SafetyModeActivity.this);
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