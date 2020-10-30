package com.example.graduateproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FaceScanner implements Camera.PreviewCallback{
    private Camera mCamera;
    private static final String TAG = FaceScanner.class.getSimpleName();
    private SurfaceTexture mCameratexture;
    public TextToSpeech tts;
    private Handler mHandler;
    private int count = 0;

    // 후면 카메라 개방
    private Camera opencamera() {
        int cameraCount = 0;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                try {
                    cam = Camera.open(camIdx);
                } catch (RuntimeException e) {
                    Log.e(TAG, "Camera failed to open: " + e.getLocalizedMessage());
                }
            }
        }
        return cam;
    }

    @SuppressLint("HandlerLeak")
    void preparescanner() {
        tts = new TextToSpeech(MainActivity.mContext, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e(TAG, "not supported language");
                    } else {
                        speakOut("Service start");
                    }
                }
            }
        });
        if (mCamera == null) {
            mCamera = opencamera();
        }
        if (mCamera == null) {
            Log.e(TAG, "prepare scanner couldn't connect to camera!");
            return;
        }
        mHandler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg){
                super.handleMessage(msg);
                String str = (String)msg.obj;
                int len=0;
                String[] names = new String[10];
                float[] prob = new float[10];
                try {
                    JSONArray arr = new JSONArray(str);
                    for(int i=0; i<arr.length(); i++)
                    {
                        JSONObject temp = arr.getJSONObject(i);
                        names[i]=temp.getString("name");
                        prob[i]=Float.parseFloat(temp.getString("prob"));
                        len++;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //big probility speaks out
                float temp = 0;
                String voice = "";
                for(int i=0; i<len; i++){
                    if(temp < prob[i])
                    {
                        temp = prob[i];
                        voice = names[i];
                    }
                }
                if(voice.equals(""))
                {
                    ((MainActivity) MainActivity.mContext).setText("nothing");
                }else{
                    ((MainActivity) MainActivity.mContext).setText(voice);
                    speakOut(voice);
                }
            }
        };
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(640, 480);
        parameters.setPreviewFpsRange(15000, 15000);
        mCamera.setParameters(parameters);
        Log.e(TAG, "prepare end");
        makePreviewGo();
    }

    private boolean makePreviewGo() {
        mCameratexture = new SurfaceTexture(0);
        try {
            mCamera.setPreviewTexture(mCameratexture);
        } catch (IOException e) {
            Log.e(TAG, "can't setPreviewTexture", e);
            return false;
        }
        mCamera.startPreview();
        mCamera.setPreviewCallback(this);
        Log.e(TAG, "makepreviewgo");
        return true;
    }

    /* 화면 회전했을 때 영상 처리용 Bitmap image를 회전시켜 줌 */
    public static Bitmap setBitMapImage(Bitmap bitmap, int i) {
        //Log.e("CameraService", "setBitMapImage");
        if (i >= 45 && i < 135) {
            bitmap = RotateBitmap(bitmap, -180);
            Log.e(TAG, "1");
        } else if (i >= 135 && i < 225) {
            bitmap = RotateBitmap(bitmap, -90);
            Log.e(TAG, "2");
        } else if (i >= 225 && i < 315) {
            //do nothing
            Log.e(TAG, "3");
        } else {
            // 정화면
            bitmap = RotateBitmap(bitmap, 90);
            Log.e(TAG, "4");
        }
        return bitmap;
    }

    /* 화면을 회전시켜 주는 메소드 */
    public static Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        // 1초에 1프레임
        if (count != 45) {
            count++;
        } else {
            if (data == null) {
                Log.w(TAG, "frame is null! skipping");
                return;
            }
            Log.e(TAG, "6초");
            Camera.Parameters parameters = camera.getParameters();
            int width = parameters.getPreviewSize().width;
            int height = parameters.getPreviewSize().height;

            YuvImage yuv = new YuvImage(data, parameters.getPreviewFormat(), width, height, null);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            yuv.compressToJpeg(new Rect(0, 0, width, height), 50, out);

            byte[] bytes = out.toByteArray();
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            bitmap = Bitmap.createScaledBitmap(bitmap, 640, 480, false);

            //Log.e("orientation: ", ""+CameraService.ori);
            bitmap = setBitMapImage(bitmap, CameraService.ori);

            String date = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss").format(new Date());
            String filepath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Gproject";
            String filename = date + ".png";
            SaveBitmapToFileCache(bitmap, filepath, filename);
            File tempfile = new File(filepath+"/", filename);
            //send bitmap
            FileUploadUtil.send2Server(mHandler, tempfile);
            count = 0;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void endScanning() {
        if (mCamera != null) {
            mHandler = null;
            mCameratexture.release();
            while(!mCameratexture.isReleased())
            {
                Log.e(TAG, "release waiting.....");
                //wait
            }
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
            speakOut("service end");
            tts = null;
            Log.d(TAG, "releaseCamera -- done");
        }
    }

    // Bitmap to File
    public void SaveBitmapToFileCache(Bitmap bitmap, String strFilePath, String filename) {
        File file = new File(strFilePath);
        // If no folders
        if (!file.exists()) {
            Log.e(TAG, ""+file.mkdirs());
            // Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
        }
        File fileCacheItem = new File(strFilePath + "/" + filename);
        OutputStream out = null;
        try {
            fileCacheItem.createNewFile();
            out = new FileOutputStream(fileCacheItem);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void speakOut(String text) {
        //todo Parse STRING
        if (text == null || text.isEmpty()) {
            return;
        }
        if (tts != null && !tts.isSpeaking()) {
            tts.setSpeechRate((float) 0.7); // 재생속도
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                String utteranceId = this.hashCode() + "";
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
            } else {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }
}
