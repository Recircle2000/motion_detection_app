package com.example.visionproject;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Collections;
import java.util.List;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.annotation.TargetApi;
import android.Manifest;
import android.widget.Toast;




public class AllSafetyVisionModeActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "AllSafeMode";
    private CameraBridgeViewBase AllSafeCameraView;

    private Mat mInputMat;
    private Mat mResultMat;
    private int mMode;

    // JNI 함수 선언
    public native long[] ConvertRGBtoGray(long matAddrInput1, long matAddrInput2, long matAddrInput3);

    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Log.d(TAG, "onCameraFrame: Processing frame...");
        Mat rgbaMat = inputFrame.rgba();
        long[] result = ConvertRGBtoGray(rgbaMat.getNativeObjAddr(), rgbaMat.getNativeObjAddr(), rgbaMat.getNativeObjAddr());
        Mat diffMat = new Mat(result[1]);

        // 움직임이 감지되면 알림 보내기
        if (result[0] == 1) {
            sendNotification("Motion detected");
        }

        return rgbaMat;
    }

    // 알림 보내는 메서드
    private void sendNotification(String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notification_icon)// 아이콘 추가완료
                .setContentTitle("Motion Detection")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId는 알림을 업데이트하거나 삭제하기 위한 고유 ID입니다.
        int notificationId = 1;
        notificationManager.notify(notificationId, builder.build());
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

        // 카메라 권한 체크 및 요청 코드
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }


        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Log.d(TAG, "onCreate: Starting cameraViewActivity");
        setContentView(R.layout.activity_all_safety_mode);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasPermissions(PERMISSIONS)) {
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }

        AllSafeCameraView = (CameraBridgeViewBase)findViewById(R.id.activity_AllSafe_view);
        AllSafeCameraView.setVisibility(SurfaceView.VISIBLE);
        AllSafeCameraView.setMaxFrameSize(1920, 1080);
        AllSafeCameraView.setCvCameraViewListener(this);
        AllSafeCameraView.setCameraIndex(0);
        AllSafeCameraView.setCameraPermissionGranted();
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
        return Collections.singletonList(AllSafeCameraView);
    }




    @Override
    public void onPause() {
        super.onPause();
        if (AllSafeCameraView != null)
            AllSafeCameraView.disableView();
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
                    AllSafeCameraView.enableView();
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

        if (AllSafeCameraView != null) {
            AllSafeCameraView.disableView();
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


    /*@Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Log.d(TAG, "onCameraFrame: Processing frame...");
        Mat rgbaMat = inputFrame.rgba();
        long matAddr = ConvertAllSafe(rgbaMat.getNativeObjAddr());
        rgbaMat = new Mat(matAddr);

        return rgbaMat;
    }*/

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
    public void useCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // 권한이 부여되지 않았을 경우
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            // 권한이 이미 부여된 경우, 카메라 기능을 사용합니다.
            startCamera();
        }
    }
    public void startCamera() {
        // 카메라를 시작하는 코드
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
            case CAMERA_PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 권한이 부여된 경우, 카메라 관련 작업을 계속합니다.
                } else {
                    // 권한이 거부된 경우, 사용자에게 알리거나 다른 작업을 수행합니다.
                    Toast.makeText(this, "Camera permission is required to use this feature.", Toast.LENGTH_SHORT).show();
                }
                break;
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