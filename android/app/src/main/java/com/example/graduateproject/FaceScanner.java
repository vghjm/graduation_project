package com.example.graduateproject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class FaceScanner implements Camera.PreviewCallback{
    private Camera mCamera;
    private static final String TAG = FaceScanner.class.getSimpleName();
    private SurfaceTexture mCameratexture;

    // 후면 카메라 개방
    private Camera opencamera()
    {
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

    void preparescanner()
    {
        if(mCamera == null){
            mCamera = opencamera();
        }
        if (mCamera == null) {
            Log.e(TAG, "prepare scanner couldn't connect to camera!");
            return;
        }

        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(640,480);
        parameters.setPreviewFpsRange(15000,15000);
        mCamera.setParameters(parameters);
        Log.e(TAG,"prepare end");

        makePreviewGo();
    }

    private boolean makePreviewGo() {

        mCameratexture = new SurfaceTexture(0);
        try{
            mCamera.setPreviewTexture(mCameratexture);
        }catch (IOException e) {
            Log.e(TAG, "can't setPreviewTexture", e);
            return false;
        }

        mCamera.startPreview();
        mCamera.setPreviewCallback(this);
        Log.e(TAG, "makepreviewgo");

        return true;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (data == null) {
            Log.w(TAG, "frame is null! skipping");
            return;
        }

        Camera.Parameters parameters = camera.getParameters();
        int width = parameters.getPreviewSize().width;
        int height = parameters.getPreviewSize().height;

        YuvImage yuv = new YuvImage(data, parameters.getPreviewFormat(), width, height, null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuv.compressToJpeg(new Rect(0, 0, width, height), 50, out);

        byte[] bytes = out.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        bitmap = Bitmap.createScaledBitmap(bitmap, 400, 300, false);

        //send bitmap


    }


    protected void stopcamera()
    {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            Log.d(TAG, "releaseCamera -- done");
        }
    }
}
