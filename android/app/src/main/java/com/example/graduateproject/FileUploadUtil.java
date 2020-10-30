package com.example.graduateproject;

import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.ref.PhantomReference;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FileUploadUtil {
    private static String URL1 = "http://qmoment.qmoment.tk:8080/upload";
    private static String URL2 = "http://qmoment.qmoment.tk:8080/delete";
    private static Handler mHandler;

    //handle message to detectionservice
    public static void handleMessage(String sending_str) {
        Message msg = Message.obtain();
        msg.obj = (Object)sending_str;
        mHandler.sendMessage(msg);
    }

    public static void deleteServerFiles(){
        RequestBody requestBody = RequestBody.create(null, new byte[0]);
        Request request = new Request.Builder()
                .url(URL2) // Server URL 은 본인 IP를 입력
                .delete(requestBody)
                .build();
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
            }
        });
    }

    public static void send2Server(Handler handler ,final File file) {
        mHandler = handler;
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("pngfile", file.getName(), RequestBody.create(MediaType.parse("image/png"), file))
                .build();
        Request request = new Request.Builder()
                .url(URL1) // Server URL 은 본인 IP를 입력
                .post(requestBody)
                .build();
        //Log.e("11",requestBody+"");
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String str = response.body().string();
                    handleMessage(str);
                    //((MainActivity) MainActivity.mContext).setText(str);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                file.delete();
            }
        });
    }
}
