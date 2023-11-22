package com.example.visionproject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UserRetrofitInterface {
    @POST("/api/send-push-notification")
    Call<ResponseBody> sendPushNotification(@Body PushNotificationRequest request);
}