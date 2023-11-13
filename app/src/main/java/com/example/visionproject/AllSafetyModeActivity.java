package com.example.visionproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.example.visionproject.databinding.ActivityTestBinding;

public class AllSafetyModeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        @NonNull ActivityTestBinding binding = ActivityTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TextView tv = binding.textTest;
        //tv.setText(stringFromJNI());
        tv.setText("전체 감지 페이지");
    }
}