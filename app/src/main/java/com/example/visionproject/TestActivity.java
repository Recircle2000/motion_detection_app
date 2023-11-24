package com.example.visionproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.example.visionproject.databinding.ActivityMainBinding;
import com.example.visionproject.databinding.ActivityTestBinding;

public class TestActivity extends AppCompatActivity {
    private @NonNull ActivityTestBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TextView tv = binding.textTest;
        //tv.setText(stringFromJNI());
        tv.setText("알림 테스트. 가끔 서버 반응이 느릴때가 있습니다. 여러번 들락날락 하다보면 알림이 와요.");
    }
}

