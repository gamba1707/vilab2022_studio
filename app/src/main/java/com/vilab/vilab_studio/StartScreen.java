package com.vilab.vilab_studio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import java.io.Serializable;


public class StartScreen extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //loading.xmlを読み込む
        setContentView(R.layout.loading);
        //0.5秒後にMainActivity.javaを読み込んで画面を切り替える
        //向こうのデータ読み込み完了まで画面はそのままなので0.5秒より長くなるはず
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplication(), MainActivity.class);
                startActivity(intent);
            }
        }, 500);
    }
}

