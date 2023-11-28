package com.example.visionproject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.user.UserApiClient;

import java.util.function.Function;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainMenu extends AppCompatActivity {

    // Used to load the 'visionproject' library on application startup.
    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");

    }

    UserRetrofitInterface userRetrofitInterface;
    // 토큰 저장
    String fcmtoken = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseApp.initializeApp(this);
        FirebaseAnalytics.getInstance(this);

        getFirebaseMessagingToken((v) -> {
            Notification.receivedToken = fcmtoken;
            return null;
        });

        //invoke
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasPermissions(PERMISSIONS)) {
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }

        Button testViewButton = findViewById(R.id.CVtest_Button);
        Button SafetyAreaViewButton = findViewById(R.id.Safety_Area_Button);
        Button AllSafetyAreaViewButton = findViewById(R.id.AllSafety_Button);
        Button OptionsButton = findViewById(R.id.Option_button);


        SafetyAreaViewButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainMenu.this, SafetyVisionModeActivity.class);
                intent.putExtra("token",fcmtoken);
                startActivity(intent);
            }
        });

        testViewButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //Notification.sendPushNotification("테스트", "바디");
                Notification.sendPushNotification("안전 카메라","테스트 응답");
                showToast("푸시알림 요청함");
                Log.d("AndroidOpenCv",fcmtoken);
                /*Intent intent = new Intent(MainMenu.this, TestActivity.class);
                intent.putExtra("mode",3);
                startActivity(intent);*/
            }
        });

        AllSafetyAreaViewButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainMenu.this, AllSafetyVisionModeActivity.class);
                intent.putExtra("token",fcmtoken);
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
    private void showToast(String title) {
        String message = title;
        Toast.makeText(MainMenu.this, message, Toast.LENGTH_SHORT).show();
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

    // FCM토큰얻는 함수
    private void getFirebaseMessagingToken(Function<Void, Void> callback) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String token = task.getResult();
                        Log.d("AndroidOpenCv", token);
                        fcmtoken = token;
                        callback.apply(null);
                    } else {
                        Log.e("AndroidOpenCv", "Token retrieval failed");
                    }
                });
    }


}