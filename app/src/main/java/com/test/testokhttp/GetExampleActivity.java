package com.test.testokhttp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GetExampleActivity extends AppCompatActivity {
    private OkHttpClient okHttpClient;
    private TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_example);
        setTitle("Get请求的用法");

        okHttpClient = new OkHttpClient();
        resultTextView = (TextView) findViewById(R.id.result_textview);
    }

    //简单的Get请求，不带参数
    public void simpleGetClick(View view) {
        RequestBody requestBody = new FormBody.Builder()
                .add("name", "lisi")
                .add("age", "30")
                .build();
        Request request = new Request.Builder()
                .url("http://192.168.1.170:8088/okhttp/test_simple_get.php")
                .build();

        okHttpClient.newCall(request).enqueue(callback);
    }

    //带参数的Get请求
    public void addParamGetClick(View view) {
        Request request = new Request.Builder()
                .addHeader("token", "asdlfjkasdljfaskdjfalsjkljalk")  //请求头中加入参数
                .url("http://192.168.1.170:8088/okhttp/test_param_get.php?username=zhangsan&phone=13888888888") //携带参数
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
                    Toast.makeText(GetExampleActivity.this, "请求成功", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(GetExampleActivity.this, "请求失败", Toast.LENGTH_SHORT).show();
                }
                resultTextView.setText(msg);
            }
        });
    }

}
