package com.example.visionproject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.user.UserApiClient;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class MainMenu extends AppCompatActivity {

    // Used to load the 'visionproject' library on application startup.
    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //invoke
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasPermissions(PERMISSIONS)) {
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }

        Button cameraViewButton = findViewById(R.id.Camera_View_Button);
        Button testViewButton = findViewById(R.id.CVtest_Button);
        Button SafetyAreaViewButton = findViewById(R.id.Safety_Area_Button);
        Button AllSafetyAreaViewButton = findViewById(R.id.AllSafety_Button);
        Button OptionsButton = findViewById(R.id.Option_button);
        Button LoginButton = findViewById(R.id.Login_button);
        cameraViewButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainMenu.this, cameraViewActivity.class);
                intent.putExtra("mode",0);
                startActivity(intent);
            }
        });

        SafetyAreaViewButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainMenu.this, SafetyVisionModeActivity.class);
                intent.putExtra("mode",2);
                startActivity(intent);
            }
        });

        testViewButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainMenu.this, TestActivity.class);
                intent.putExtra("mode",3);
                startActivity(intent);
            }
        });

        AllSafetyAreaViewButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainMenu.this, AllSafetyVisionModeActivity.class);
                intent.putExtra("mode",4);
                startActivity(intent);
            }
        });

        OptionsButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainMenu.this, SettingsActivity.class);
                intent.putExtra("mode",5);
                startActivity(intent);
            }
        });

        Function2<OAuthToken, Throwable, Unit> callback = new Function2<OAuthToken, Throwable, Unit>() {
            @Override
            public Unit invoke(OAuthToken oAuthToken, Throwable throwable) {
                if (oAuthToken != null)
                {

                }
                if (throwable != null)
                {

                }
                return null;
            }
        };

        //로그인 연동 코드
        LoginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(UserApiClient.getInstance().isKakaoTalkLoginAvailable(MainMenu.this))
                {
                    UserApiClient.getInstance().loginWithKakaoTalk(MainMenu.this, callback);
                }
                else
                {
                    UserApiClient.getInstance().loginWithKakaoAccount(MainMenu.this, callback);
                }
            }
        });

        // Example of a call to a native method

    }

    /**
     * A native method that is implemented by the 'visionproject' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    static final int PERMISSIONS_REQUEST_CODE = 1000;
    String[] PERMISSIONS = {"android.permission.CAMERA", "android.permission.READ_EXTERNAL_STORAGE"};


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

    // 불필요
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean isPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (!isPermission) {
                        showDialogForPermission("실행을 위해 권한 허가가 필요합니다.");
                    }
                }
                break;
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainMenu.this);
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