package com.test.testokhttp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.test.testokhttp.http.GetUserHttpManager;
import com.test.testokhttp.http.TestPostHttpManager;
import com.test.testokhttp.http.TestUploadHttpManager;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TestActivity extends AppCompatActivity {

    private String token = "i1YxS/o+KUTdJ34KQxbZK0mtaZAZcOBuoAHxQQ1tCso=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
    }

    public void testGet(View v) {
        String url = "http://192.168.1.170:8088/TestOkHttp/test_get.php";
        GetUserHttpManager manager = new GetUserHttpManager(this, url, BaseHttpManager.DataType.JSON);
        manager.addHeader("Authorization", "Bearer " + token);
        manager.setOnRequestListener(new BaseHttpManager.OnRequestListener<String>() {
            @Override
            public void onStart() {
                Log.d("yubo", "onStart...");
            }

            @Override
            public void onLoading(int progress) {

            }

            @Override
            public void onSuccess(String result) {
                if(result != null) {
                    Log.d("yubo", "onSuccess: " + result);
                }else {
                    Log.d("yubo", "nothing return");
                }
            }

            @Override
            public void onFailure(IOException e) {
                Log.d("yubo", "onFailure: " + e.toString());
            }
        });
        Map<String, String> params = new HashMap<>();
        params.put("name", "zhangsan");
        params.put("age", "25");
        manager.startManager(params, BaseHttpManager.Method.GET);
    }

    public void testPost(View view) {
        String url = "http://192.168.1.170:8088/TestOkHttp/test_post.php";
        TestPostHttpManager manager = new TestPostHttpManager(this, url, BaseHttpManager.DataType.JSON);
        manager.addHeader("Authorization", "Bearer " + token);
        manager.setOnRequestListener(new BaseHttpManager.OnRequestListener<String>() {
            @Override
            public void onStart() {
                Log.d("yubo", "onStart...");
            }

            @Override
            public void onLoading(int progress) {

            }

            @Override
            public void onSuccess(String result) {
                if(result != null) {
                    Log.d("yubo", "onSuccess: " + result);
                }else {
                    Log.d("yubo", "nothing return");
                }
            }

            @Override
            public void onFailure(IOException e) {
                Log.d("yubo", "onFailure: " + e.toString());
            }
        });
        Map<String, String> params = new HashMap<>();
        params.put("name", "zhangsan");
        params.put("age", "25");
        manager.startManager(params, BaseHttpManager.Method.POST);
    }

    public void testUpload(View v) {
        String url = "http://192.168.1.170:8088/TestOkHttp/test_upload.php";
        TestUploadHttpManager manager = new TestUploadHttpManager(this, url, BaseHttpManager.DataType.JSON);
        manager.addHeader("Authorization", "Bearer " + token);
        manager.setOnRequestListener(new BaseHttpManager.OnRequestListener<String>() {
            @Override
            public void onStart() {
                Log.d("yubo", "onStart...");
            }

            @Override
            public void onLoading(int progress) {
                Log.e("yubo", "upload progress: " + progress + "%");
            }

            @Override
            public void onSuccess(String result) {
                if(result != null) {
                    Log.d("yubo", "onSuccess: " + result);
                }else {
                    Log.d("yubo", "nothing return");
                }
            }

            @Override
            public void onFailure(IOException e) {
                Log.d("yubo", "onFailure: " + e.toString());
            }
        });
        Map<String, String> params = new HashMap<>();
        params.put("name", "zhangsan");
        params.put("age", "25");
        Map<String, File> fileParams = new HashMap<>();
        File file = new File("mnt/sdcard/test.apk");
        fileParams.put("file", file);
        manager.startPostRequest(null, fileParams);
    }

    public void testDownload(View view) {
        String url = "http://192.168.1.170:8088/yuji.apk";
        String filePath = "mnt/sdcard/yuji.apk";
        BaseHttpManager.download(this, url, filePath, new BaseHttpManager.DownloadListener() {
            @Override
            public void onStart() {
                Log.d("yubo", "start download...");
            }

            @Override
            public void onProgress(int progress) {
                Log.d("yubo", "download progress: " + progress + "%");
            }

            @Override
            public void onFailure(Exception e) {
                Log.d("yubo", "download fail: " + e.toString());
            }

            @Override
            public void onSuccess(File f) {
                Log.d("yubo", "download success, file path: " + f.getAbsolutePath());
            }
        });
    }
}
