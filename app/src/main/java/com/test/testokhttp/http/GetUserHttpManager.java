package com.test.testokhttp.http;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.test.testokhttp.BaseHttpManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

/**
 * Created by yubo7 on 2016/9/6.
 */
public class GetUserHttpManager extends BaseHttpManager<String> {

    public GetUserHttpManager(Context context, String url, DataType returnDataType) {
        super(context, url, returnDataType);
    }

    @Override
    public String parseXml(XmlPullParser xmlPullParser) {
        return null;
    }

    @Override
    public String parseJson(String json) {
        Log.d("yubo", "get user return : " + json);
        return json;
    }
}
