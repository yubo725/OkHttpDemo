package com.test.testokhttp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("OkHttp的用法");
    }

    //Get请求
    public void getClick(View view) {
        startActivity(new Intent(this, GetExampleActivity.class));
    }

    //Post请求
    public void postClick(View view) {
        startActivity(new Intent(this, PostExampleActivity.class));
    }

    //上传文件
    public void uploadClick(View view) {
        startActivity(new Intent(this, UploadActivity.class));
    }

    //下载文件
    public void downloadClick(View view) { startActivity(new Intent(this, DownloadActivity.class));}

}
