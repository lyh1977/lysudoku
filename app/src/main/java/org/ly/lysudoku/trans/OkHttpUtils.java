package org.ly.lysudoku.trans;

import android.os.Handler;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.ly.lysudoku.utils.LogUtil;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Description : OkHttp网络连接封装工具类
 * Author :
 * Date   : 2020年7月1日15:51:20
 */
public class OkHttpUtils {

    private static final String TAG = "OkHttpUtils";
    //handler主要用于异步请求数据之后更新UI
    private static Handler handler = new Handler();

    public static void getAsync(String url,ResponseCallBack responseCallBack) {
        OkHttpClient client = new OkHttpClient();
        //LogUtil.d(TAG,"请求地址=>"+url);
        Request request = new Request
                .Builder()
                //.addHeader("Cookie","token="+ HttpUrlVo.TOKEN)
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                LogUtil.e(TAG,"响应失败=>"+e.getMessage());
                handler.post(()->{
                    responseCallBack.error(e.getMessage());
                });
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String respBody=response.body().string();
                //LogUtil.d(TAG,"响应成功=>"+respBody);
                handler.post(()->{
                    try {
                        if(response.code()==200)
                            responseCallBack.success(respBody);
                        else
                            responseCallBack.error("Back Code "+response.code());
                    } catch (Exception e) {
                        e.printStackTrace();
                        LogUtil.e(TAG,e.getMessage());
                        //BckApplication.getInstance().showLogToast("程序出现异常:"+e.getMessage());
                    }
                });
            }
        });
    }

    /**
     * 表单提交数据
     * @param url 请求地址
     * @param formData 表单回调
     * @param responseCallBack 响应回调
     */
    public static void postAsyncFormData(String url, Map<String,String> formData, ResponseCallBack responseCallBack) {
        OkHttpClient client = new OkHttpClient().newBuilder().
                readTimeout(30, TimeUnit.SECONDS)
                .build();
        FormBody.Builder builder = new FormBody.Builder();
        StringBuffer showData=new StringBuffer();
        for (String key:formData.keySet()){
            builder.add(key,formData.get(key));
            showData.append("   "+key+":"+formData.get(key));
        }
        FormBody formBody = builder
                .build();
        Request request = new Request
                .Builder()
                //.addHeader("Cookie","token="+ HttpUrlVo.TOKEN)
                .url(url)
                .post(formBody)
                .build();

        LogUtil.i(TAG,"开始发送请求：请求地址【"+url+"】,请求参数=>"+showData);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                LogUtil.e(TAG,"响应失败=>"+e.getMessage());
                handler.post(()->{
                    responseCallBack.error(e.getMessage());
                });
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String respBody=response.body().string();
                LogUtil.i(TAG,"响应成功=>"+respBody);
                handler.post(()->{
                    try {
                        if(response.code()==200)
                            responseCallBack.success(respBody);
                        else
                            responseCallBack.error("Back Code "+response.code());
                    } catch (Exception e) {
                        LogUtil.e(TAG,e.getMessage());
                        //BckApplication.getInstance().showLogToast("程序出现异常:"+e.getMessage());
                    }
                });
            }
        });
    }
    /**
     * json提交数据
     * @param url 请求地址
     * @param json json数据
     * @param responseCallBack 响应回调
     */
    public static void postAsyncJson(String url, String json, ResponseCallBack responseCallBack) {

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);
        Request request = new Request.
                Builder()
                .url(url)
                //.addHeader("Cookie","token="+ HttpUrlVo.TOKEN)
                .post(requestBody)
                .build();

        LogUtil.i(TAG,"开始发送请求：请求地址【"+url+"】,请求参数=>"+json);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                LogUtil.e(TAG,"响应失败=>"+e.getMessage());
                handler.post(()->{
                    responseCallBack.error(e.getMessage());
                });
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String respBody=response.body().string();
                //LogUtil.i(TAG,"响应成功=>"+respBody);
                handler.post(()->{
                    try {
                        if(response.code()==200)
                            responseCallBack.success(respBody);
                        else
                            responseCallBack.error("Back Code "+response.code());
                    } catch (Exception e) {
                        LogUtil.e(TAG,e.getMessage());
                        //BckApplication.getInstance().showLogToast("程序出现异常:"+e.getMessage());
                    }
                });
            }
        });
    }

    /**
     * json提交数据
     * @param url 请求地址
     * @param json json数据

     */
    public static OkHttpSyncResult postSyncJson(String url, String json) {
        OkHttpSyncResult resut=new OkHttpSyncResult();
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);
        Request request = new Request.
                Builder()
                .url(url)
                //.addHeader("Cookie","token="+ HttpUrlVo.TOKEN)
                .post(requestBody)
                .build();

        LogUtil.i(TAG,"开始发送请求：请求地址【"+url+"】,请求参数=>"+json);
        try {
            Response response= client.newCall(request).execute();
            String respBody=response.body().string();
            resut.setSuccess(response.code()==200);
            resut.setRespBody(respBody);
            resut.setCode(response.code());
        }
        catch (IOException er)
        {
            resut.setSuccess(false);
            resut.setRespBody(er.getMessage());
        }
        return resut;
    }

    /**
     * json提交数据
     * @param url 请求地址
     * @param json json数据
     * @param responseCallBack 响应回调
     */
    public static void putAsyncJson(String url, String json, ResponseCallBack responseCallBack) {
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);
        Request request = new Request.
                Builder()
                .url(url)
                //.addHeader("Cookie","token="+ HttpUrlVo.TOKEN)
                .put(requestBody)
                .build();

        LogUtil.i(TAG,"开始发送请求：请求地址【"+url+"】,请求参数=>"+json);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                LogUtil.e(TAG,"响应失败=>"+e.getMessage());
                handler.post(()->{
                    responseCallBack.error(e.getMessage());
                });
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String respBody=response.body().string();
                LogUtil.i(TAG,"响应成功=>"+respBody);
                handler.post(()->{
                    try {
                        if(response.code()==200)
                            responseCallBack.success(respBody);
                        else
                            responseCallBack.error("Back Code "+response.code());
                    } catch (Exception e) {
                        LogUtil.e(TAG,e.getMessage());
                        //BckApplication.getInstance().showLogToast("程序出现异常:"+e.getMessage());
                    }
                });
            }
        });
    }

    /**
     * json提交数据
     * @param url 请求地址
     * @param formData 表单数据
     * @param responseCallBack 响应回调
     */
    public static void putAsyncForm(String url, Map<String,String> formData, ResponseCallBack responseCallBack) {
        OkHttpClient client = new OkHttpClient().newBuilder().
                readTimeout(30, TimeUnit.SECONDS)
                .build();
        FormBody.Builder builder = new FormBody.Builder();
        StringBuffer showData=new StringBuffer();
        for (String key:formData.keySet()){
            builder.add(key,formData.get(key));
            showData.append("   "+key+":"+formData.get(key));
        }
        FormBody formBody = builder
                .build();
        Request request = new Request
                .Builder()
                //.addHeader("Cookie","token="+ HttpUrlVo.TOKEN)
                .url(url)
                .put(formBody)
                .build();
        LogUtil.i(TAG,"开始发送请求：请求地址【"+url+"】,请求参数=>"+showData);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                LogUtil.e(TAG,"响应失败=>"+e.getMessage());
                handler.post(()->{
                    responseCallBack.error(e.getMessage());
                });
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String respBody=response.body().string();
                LogUtil.i(TAG,"响应成功=>"+respBody);
                handler.post(()->{
                    try {
                        if(response.code()==200)
                            responseCallBack.success(respBody);
                        else
                            responseCallBack.error("Back Code "+response.code());
                    } catch (Exception e) {
                        LogUtil.e(TAG,e.getMessage());
                        //BckApplication.getInstance().showLogToast("程序出现异常:"+e.getMessage());
                    }
                });
            }
        });
    }
}
