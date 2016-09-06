package com.test.testokhttp;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ForwardingSink;
import okio.ForwardingSource;
import okio.Okio;
import okio.Sink;
import okio.Source;

/**
 * Created by yubo on 2016/4/18.
 * 网络请求的基类，封装了OkHttp，提供基本的GET,PUT,POST,DELETE请求<br/>
 * 支持文件上传和下载，支持多种类型的参数传递
 */
public abstract class BaseHttpManager<T> {

    public static OkHttpClient okHttpClient;

    private String urlStr;
    private Context context;
    private DataType returnDataType;
    private Request.Builder requestBuilder;

    private OnRequestListener<T> onRequestListener;

    private XmlPullParser xmlPullParser;

    //初始化OkHttpClient，所有的请求共用这一个对象
    static {
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    //请求的方法
    public enum Method {
        GET, POST, DELETE, PUT
    }

    //返回的数据格式
    public enum DataType {
        JSON, XML
    }

    //请求的监听器，所有的回调方法都在主线程中回调
    public interface OnRequestListener<T> {
        void onStart();
        void onLoading(int progress);
        void onSuccess(T result);
        void onFailure(IOException e);
    }

    //构造方法
    public BaseHttpManager(Context context, String url, DataType returnDataType) {
        this.context = context;
        this.urlStr = url;
        this.returnDataType = returnDataType;

        this.xmlPullParser = Xml.newPullParser();
        this.requestBuilder = new Request.Builder();
    }

    //设置请求的监听器
    public void setOnRequestListener(OnRequestListener<T> onRequestListener) {
        if(onRequestListener != null) {
            this.onRequestListener = onRequestListener;
        }
    }

    //添加请求头
    public void addHeader(String key, String value) {
        if(this.requestBuilder != null) {
            this.requestBuilder.addHeader(key, value);
        }
    }

    //解析xml数据，需要由子类去实现该方法
    public abstract T parseXml(XmlPullParser xmlPullParser);

    //解析json数据，需要由子类去实现该方法
    public abstract T parseJson(String json);

    //开始一个网络请求
    public void startManager(Map<String, String> params, Method method) {
        if(TextUtils.isEmpty(urlStr)) {
            throw new IllegalArgumentException("request url should not be null.");
        }
        if(this.onRequestListener == null) {
            throw new IllegalArgumentException("onRequestListener should not be null.");
        }
        onRequestListener.onStart();
        switch(method) {
            case GET:
                startGetRequest(params);
                break;
            case POST:
                startPostRequest(params);
                break;
            case PUT:
                startPutRequest(params);
                break;
            case DELETE:
                startDeleteRequest(params);
                break;
        }
    }

    //开始get请求
    private void startGetRequest(Map<String, String> params) {
        if(params != null && params.size() > 0) {
            Iterator<String> paramsIterator = params.keySet().iterator();
            StringBuilder sb = new StringBuilder();
            String paramKey;
            sb.append("?");
            while (paramsIterator.hasNext()) {
                paramKey = paramsIterator.next();
                sb.append(paramKey);
                sb.append("=");
                sb.append(params.get(paramKey));
                sb.append("&");
            }
            //拼接参数
            this.urlStr += sb.toString().substring(0, sb.length() - 1);
        }
        Request request = this.requestBuilder.url(this.urlStr).build();
        okHttpClient.newCall(request).enqueue(callback);
    }

    //开始post请求，这里的请求只包含字符串类型的参数
    private void startPostRequest(Map<String, String> params) {
        FormBody.Builder builder = new FormBody.Builder();
        if(params != null && params.size() > 0) {
            Iterator<String> paramsIterator = params.keySet().iterator();
            String paramKey;
            while (paramsIterator.hasNext()) {
                paramKey = paramsIterator.next();
                builder.add(paramKey, params.get(paramKey));
            }
        }
        Request request = this.requestBuilder
                .url(this.urlStr)
                .post(builder.build())
                .build();
        okHttpClient.newCall(request).enqueue(callback);
    }

    //开始post请求，参数包括字符串类型和文件类型
    public void startPostRequest(Map<String, String> strParams, Map<String, File> fileParams) {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        String paramKey;
        File file;
        if(strParams != null && strParams.size() > 0) {
            Iterator<String> strParamsIterator = strParams.keySet().iterator();
            while(strParamsIterator.hasNext()) {
                paramKey = strParamsIterator.next();
                builder.addFormDataPart(paramKey, strParams.get(paramKey));
            }
        }
        if(fileParams != null && fileParams.size() > 0) {
            Iterator<String> fileParamsIterator = fileParams.keySet().iterator();
            while(fileParamsIterator.hasNext()) {
                paramKey = fileParamsIterator.next();
                file = fileParams.get(paramKey);
                String mediaType = URLConnection.getFileNameMap().getContentTypeFor(file.getName());
                mediaType += "; charset=utf-8";
                builder.addFormDataPart(paramKey, file.getName(), RequestBody.create(MediaType.parse(mediaType), file));
                Log.e("yubo", "add file part, media type: " + mediaType);
            }
        }
        //请求参数带文件时，可以显示进度
        ProgressRequestBody progressRequestBody = new ProgressRequestBody(builder.build(), progressListener);
        Request request = this.requestBuilder
                .url(this.urlStr)
                .post(progressRequestBody)
                .build();
        okHttpClient.newCall(request).enqueue(callback);
    }

    //请求参数带文件时，监听请求进度的监听器
    private ProgressListener progressListener = new ProgressListener() {
        @Override
        public void update(long bytesRead, long contentLength, boolean done) {
            final int progress = (int) (100.0 * bytesRead / contentLength);
            if(onRequestListener != null) {
                ((Activity)context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onRequestListener.onLoading(progress);
                    }
                });
            }
        }
    };

    //开始put请求
    private void startPutRequest(Map<String, String> params) {
        FormBody.Builder builder = new FormBody.Builder();
        if(params != null && params.size() > 0) {
            Iterator<String> paramsIterator = params.keySet().iterator();
            String paramKey;
            while (paramsIterator.hasNext()) {
                paramKey = paramsIterator.next();
                builder.add(paramKey, params.get(paramKey));
            }
        }
        Request request = this.requestBuilder
                .url(this.urlStr)
                .put(builder.build())
                .build();
        okHttpClient.newCall(request).enqueue(callback);
    }

    //开始delete请求
    private void startDeleteRequest(Map<String, String> params) {
        FormBody.Builder builder = new FormBody.Builder();
        if(params != null && params.size() > 0) {
            Iterator<String> paramsIterator = params.keySet().iterator();
            String paramKey;
            while (paramsIterator.hasNext()) {
                paramKey = paramsIterator.next();
                builder.add(paramKey, params.get(paramKey));
            }
        }
        Request request = this.requestBuilder
                .url(this.urlStr)
                .delete(builder.build())
                .build();
        okHttpClient.newCall(request).enqueue(callback);
    }

    //注意callback是在子线程中回调
    private Callback callback = new Callback() {
        T t;

        @Override
        public void onFailure(Call call, IOException e) {
            final IOException exception = e;
            ((Activity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onRequestListener.onFailure(exception);
                }
            });
        }

        @Override
        public void onResponse(Call call, final Response response) throws IOException {
            //获取请求后的返回数据，然后解析，解析完成再调用回调方法
            if(response.isSuccessful()) {
                String resultStr = response.body().string();
                if(!TextUtils.isEmpty(resultStr)) {
                    t = null;
                    //根据不同的数据返回类型，做不同的解析处理
                    switch(returnDataType) {
                        case XML:
                            try {
                                xmlPullParser.setInput(new StringReader(resultStr));
                                t = parseXml(xmlPullParser);
                            } catch (XmlPullParserException e) {
                                e.printStackTrace();
                            }
                            break;
                        case JSON:
                            t = parseJson(resultStr);
                            break;
                    }
                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onRequestListener.onSuccess(t);
                        }
                    });
                }
            }else {
                ((Activity)context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onRequestListener.onFailure(new IOException(response.toString()));
                    }
                });
            }
        }
    };

    //自定义的RequestBody，能够显示进度
    public class ProgressRequestBody extends RequestBody {
        //实际的待包装请求体
        private final RequestBody requestBody;
        //进度回调接口
        private final ProgressListener progressListener;
        //包装完成的BufferedSink
        private BufferedSink bufferedSink;

        /**
         * 构造函数，赋值
         *
         * @param requestBody      待包装的请求体
         * @param progressListener 回调接口
         */
        public ProgressRequestBody(RequestBody requestBody, ProgressListener progressListener) {
            this.requestBody = requestBody;
            this.progressListener = progressListener;
        }

        /**
         * 重写调用实际的响应体的contentType
         *
         * @return MediaType
         */
        @Override
        public MediaType contentType() {
            return requestBody.contentType();
        }

        /**
         * 重写调用实际的响应体的contentLength
         *
         * @return contentLength
         * @throws IOException 异常
         */
        @Override
        public long contentLength() throws IOException {
            return requestBody.contentLength();
        }

        /**
         * 重写进行写入
         *
         * @param sink BufferedSink
         * @throws IOException 异常
         */
        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            if (bufferedSink == null) {
                //包装
                bufferedSink = Okio.buffer(sink(sink));
            }
            //写入
            requestBody.writeTo(bufferedSink);
            //必须调用flush，否则最后一部分数据可能不会被写入
            bufferedSink.flush();

        }

        /**
         * 写入，回调进度接口
         *
         * @param sink Sink
         * @return Sink
         */
        private Sink sink(Sink sink) {
            return new ForwardingSink(sink) {
                //当前写入字节数
                long bytesWritten = 0L;
                //总字节长度，避免多次调用contentLength()方法
                long contentLength = 0L;

                @Override
                public void write(Buffer source, long byteCount) throws IOException {
                    super.write(source, byteCount);
                    if (contentLength == 0) {
                        //获得contentLength的值，后续不再调用
                        contentLength = contentLength();
                    }
                    //增加当前写入的字节数
                    bytesWritten += byteCount;
                    //回调
                    progressListener.update(bytesWritten, contentLength, bytesWritten == contentLength);
                }
            };
        }
    }

    //自定义的ResponseBody，在其中处理进度
    private static class ProgressResponseBody extends ResponseBody {

        private final ResponseBody responseBody;
        private final ProgressListener progressListener;
        private BufferedSource bufferedSource;

        public ProgressResponseBody(ResponseBody responseBody, ProgressListener progressListener) {
            this.responseBody = responseBody;
            this.progressListener = progressListener;
        }

        @Override public MediaType contentType() {
            return responseBody.contentType();
        }

        @Override public long contentLength() {
            return responseBody.contentLength();
        }

        @Override public BufferedSource source() {
            if (bufferedSource == null) {
                bufferedSource = Okio.buffer(source(responseBody.source()));
            }
            return bufferedSource;
        }

        private Source source(Source source) {
            return new ForwardingSource(source) {
                long totalBytesRead = 0L;

                @Override public long read(Buffer sink, long byteCount) throws IOException {
                    long bytesRead = super.read(sink, byteCount);
                    // read() returns the number of bytes read, or -1 if this source is exhausted.
                    totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                    progressListener.update(totalBytesRead, responseBody.contentLength(), bytesRead == -1);
                    return bytesRead;
                }
            };
        }
    }

    //进度回调接口
    interface ProgressListener {
        void update(long bytesRead, long contentLength, boolean done);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////下载部分的接口///////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    //下载的接口，url为下载地址，filePath为下载的文件路径，listener为下载的监听器
    public static void download(Context context, String url, String filePath, DownloadListener listener) {
        if(TextUtils.isEmpty(url)) {
            throw new IllegalArgumentException("download url should not be null.");
        }
        if(listener == null) {
            throw new IllegalArgumentException("DownloadListener should not be null.");
        }
        downloadContext = context;
        downloadListener = listener;
        downloadFilePath = filePath;
        OkHttpClient client = new OkHttpClient.Builder()
                .addNetworkInterceptor(new Interceptor() {
                    @Override public Response intercept(Interceptor.Chain chain) throws IOException {
                        Response originalResponse = chain.proceed(chain.request());
                        return originalResponse.newBuilder()
                                .body(new ProgressResponseBody(originalResponse.body(), downloadProgressListener))
                                .build();
                    }
                })
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        downloadListener.onStart();
        client.newCall(new Request.Builder().url(url).build()).enqueue(downloadCallback);
    }

    private static Context downloadContext;
    private static String downloadFilePath;
    private static DownloadListener downloadListener;

    private static ProgressListener downloadProgressListener = new ProgressListener() {
        @Override
        public void update(long bytesRead, long contentLength, boolean done) {
            int progress = (int) (100.0 * bytesRead / contentLength);
            downloadListener.onProgress(progress);
        }
    };

    interface DownloadListener {
        void onStart();
        void onProgress(int progress);
        void onFailure(Exception e);
        void onSuccess(File f);
    }

    //下载的回调
    private static Callback downloadCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            final Exception exception = e;
            ((Activity)downloadContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    downloadListener.onFailure(exception);
                }
            });
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if(response != null) {
                //下载完成，保存数据到文件
                InputStream is = response.body().byteStream();
                File downloadFile = new File(downloadFilePath);
                if(downloadFile.exists()) {
                    downloadFile.delete();
                }
                downloadFile.createNewFile();
                FileOutputStream fos = new FileOutputStream(downloadFile);
                byte[] buf = new byte[1024];
                int hasRead = 0;
                while((hasRead = is.read(buf)) > 0) {
                    fos.write(buf, 0, hasRead);
                }
                fos.close();
                is.close();
                downloadListener.onSuccess(downloadFile);
            }
        }
    };

}
