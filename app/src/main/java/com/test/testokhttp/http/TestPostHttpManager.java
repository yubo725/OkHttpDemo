package com.test.testokhttp.http;

import android.content.Context;
import android.util.Log;

import com.test.testokhttp.BaseHttpManager;

import org.xmlpull.v1.XmlPullParser;

/**
 * Created by yubo7 on 2016/9/6.
 */
public class TestPostHttpManager extends BaseHttpManager<String> {

    public TestPostHttpManager(Context context, String url, DataType returnDataType) {
        super(context, url, returnDataType);
    }

    @Override
    public String parseXml(XmlPullParser xmlPullParser) {
        return null;
    }

    @Override
    public String parseJson(String json) {
        Log.d("yubo", "TestPostHttpManager return: " + json);
        return json;
    }
}
