package com.example.graduateproject;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FileUploadUtil {
    private static String URL = "http://b2e9dc8f37fd.ngrok.io/upload";

    public static void send2Server(final File file) {
        Log.e("11", file.getName());
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("pngfile", file.getName(), RequestBody.create(MediaType.parse("image/png"), file))
                .build();
        Request request = new Request.Builder()
                .url(URL) // Server URL 은 본인 IP를 입력
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
                //Log.d("TEST : ", response.body().string());
                try {
                    //JSONObject jsonObject = new JSONObject(response.body().string());
                    String str = response.body().string();
                    ((MainActivity) MainActivity.mContext).setText(str);
//                }catch(JSONException e){
//                    e.printStackTrace();
                }catch(IOException e){
                    e.printStackTrace();
                }
                file.delete();
            }
        });
    }
}
