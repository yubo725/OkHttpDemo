package com.test.testokhttp;

import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PostExampleActivity extends AppCompatActivity {
    private OkHttpClient okHttpClient;
    private TextView resultTextView;
    private File tempFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_example);
        setTitle("Post请求的用法");

        okHttpClient = new OkHttpClient();
        resultTextView = (TextView) findViewById(R.id.result_textview);
    }

    //简单的带参数和Header的post请求
    public void simplePostClick(View view) {
        okHttpClient = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("username", "wangwu")
                .add("password", "hello12345")
                .add("gender", "female")
                .build();
        Request request = new Request.Builder()
                .url("http://192.168.1.170:8088/okhttp/test_simple_post.php")
                .post(requestBody)
                .addHeader("token", "helloworldhelloworldhelloworld")
                .build();
        okHttpClient.newCall(request).enqueue(callback);
    }

    //带文本参数和文件参数的post请求
    public void filePostClick(View view) {
        tempFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "info.txt");
        try {
            AssetManager assetManager = getAssets();
            InputStream inputStream = assetManager.open("info.txt");
            FileWriter fileWriter = new FileWriter(tempFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while((line = br.readLine()) != null) {
                fileWriter.write(line);
            }
            br.close();
            fileWriter.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        RequestBody fileBody = RequestBody.create(MediaType.parse("text/plain; charset=utf-8"), tempFile);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("username", "wangwu")
                .addFormDataPart("password", "hello12345")
                .addFormDataPart("gender", "female")
                .addFormDataPart("file", "info.txt", fileBody)
                .build();
        Request request = new Request.Builder()
                .url("http://192.168.1.170:8088/okhttp/test_param_post.php")
                .post(requestBody)
                .addHeader("token", "helloworldhelloworldhelloworld")
                .build();
        okHttpClient.newCall(request).enqueue(callback);
    }

    //请求后的回调方法
    private Callback callback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            setResult(e.getMessage(), false);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            setResult(response.body().string(), true);
        }
    };

    //显示请求返回的结果
    private void setResult(final String msg, final boolean success) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(success) {
                    Toast.makeText(PostExampleActivity.this, "请求成功", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(PostExampleActivity.this, "请求失败", Toast.LENGTH_SHORT).show();
                }
                resultTextView.setText(msg);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }
}
