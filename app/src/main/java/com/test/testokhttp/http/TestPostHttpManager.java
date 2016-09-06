package com.test.testokhttp.http;

import android.content.Context;
import android.text.TextUtils;

import com.test.testokhttp.BaseHttpManager;
import com.test.testokhttp.bean.UserBean;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

public class TestPostHttpManager extends BaseHttpManager<UserBean> {

    public TestPostHttpManager(Context context, String url, DataType returnDataType) {
        super(context, url, returnDataType);
    }

    @Override
    public UserBean parseXml(XmlPullParser xmlPullParser) {
        return null;
    }

    @Override
    public UserBean parseJson(String json) {
        try {
            if(!TextUtils.isEmpty(json)) {
                JSONObject jobj = new JSONObject(json);
                UserBean bean = new UserBean();
                bean.setName(jobj.getString("name"));
                bean.setId(jobj.getString("id"));
                bean.setAge(jobj.getInt("age"));
                return bean;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
