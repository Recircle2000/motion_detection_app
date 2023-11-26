package com.example.visionproject;

import android.util.Log;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Notification {

    private static final String TAG = "AndroidOpenCv";
    static String receivedToken;

    public Notification(String receivedToken) {
        this.receivedToken = receivedToken;
    }

    public static void sendPushNotification(String Title, String Body) {
        PushNotificationRequest request = new PushNotificationRequest();
        request.setTargetToken(receivedToken);
        request.setTitle(Title);
        request.setBody(Body);
        request.setId("123");  // 예시로 임의의 ID 부여
        request.setIsEnd("false");  // 예시로 "false" 부여

        UserRetrofitInterface userRetrofitInterface = RetrofitClient.getInstance().create(UserRetrofitInterface.class);

// Null 체크
        if (userRetrofitInterface != null) {
            // Retrofit 객체 사용 가능
            Call<ResponseBody> call = userRetrofitInterface.sendPushNotification(request);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    // 성공적으로 응답 받았을 때의 처리
                    if (response.isSuccessful()) {
                        Log.d(TAG, "성공");
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("통신", "실패");
                }
            });
        } else {
            Log.e("초기화", "Retrofit 객체 안됨");
        }
    }
}
