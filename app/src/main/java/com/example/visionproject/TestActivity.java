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
        tv.setText("테스트 페이지");
    }
}