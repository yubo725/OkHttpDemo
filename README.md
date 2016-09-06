# OkHttpDemo
OkHttp的使用Demo，包括一个封装了OkHttp的网络请求类BaseHttpManager，可以极大地简化OkHttp的使用。

# 介绍
BaseHttpManager是一个封装了OkHttp的网络请求工具类，可以很简单的完成GET, POST, PUT, DELETE请求以及文件的上传和下载

# 使用方法
BaseHttpManager是一个抽象类，要使用GET, POST, PUT, DELETE请求，需要定义一个BaseHttpManager的实现类，如下代码所示：
```
//自定义TestPostHttpManager类继承自BaseHttpManager，并指定请求返回后解析出UserBean对象
public class TestPostHttpManager extends BaseHttpManager<UserBean> {

    public TestPostHttpManager(Context context, String url, DataType returnDataType) {
        super(context, url, returnDataType);
    }

    @Override
    public UserBean parseXml(XmlPullParser xmlPullParser) {
        //如果服务端返回XML格式的数据，则在这里实现解析XML的代码
        return null;
    }

    @Override
    public UserBean parseJson(String json) {
        //如果服务端返回的JSON格式的数据，则在这里实现解析JSON的代码
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
```
编写好TestPostHttpManager类后，使用方法如下：
```
//请求地址
String url = "http://192.168.1.170:8088/TestOkHttp/test_post.php";
TestPostHttpManager manager = new TestPostHttpManager(this, url, BaseHttpManager.DataType.JSON);
//给请求添加Header
manager.addHeader("Authorization", "Bearer 12345");
//设置请求的回调监听器
manager.setOnRequestListener(new BaseHttpManager.OnRequestListener<UserBean>() {
    @Override
    public void onStart() {
        Log.d("yubo", "onStart...");
    }

    @Override
    public void onLoading(int progress) {

    }

    @Override
    public void onSuccess(UserBean result) {
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
//请求参数
Map<String, String> params = new HashMap<>();
params.put("name", "zhangsan");
params.put("age", "25");
//开始请求，指定请求方式为POST
manager.startManager(params, BaseHttpManager.Method.POST);
```
其他的请求如GET, PUT, DELETE用法都跟上面类似，如果是带有多个参数加文件的请求，可以使用
```
public void startPostRequest(Map<String, String> strParams, Map<String, File> fileParams)
```
该方法的第一个参数为字符串类型的键值对，第二个参数为文件类型的键值对。
如果要下载，可以使用BaseHttpManager的静态方法：
```
public static void download(Context context, String url, String filePath, DownloadListener listener)
```
其中context为上下文，url为下载地址，filePath为下载后的文件保存路径，listener为下载的监听器，可处理下载进度等。
