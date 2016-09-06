package com.test.testokhttp.http;

import android.content.Context;

import com.test.testokhttp.BaseHttpManager;

import org.xmlpull.v1.XmlPullParser;

/**
 * Created by yubo7 on 2016/9/6.
 */
public class TestUploadHttpManager extends BaseHttpManager<String> {

    public TestUploadHttpManager(Context context, String url, DataType returnDataType) {
        super(context, url, returnDataType);
    }

    @Override
    public String parseXml(XmlPullParser xmlPullParser) {
        return null;
    }

    @Override
    public String parseJson(String json) {
        return json;
    }
}
