package com.example.graduateproject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FaceScanner implements Camera.PreviewCallback {
    private Camera mCamera;
    private static final String TAG = FaceScanner.class.getSimpleName();
    private SurfaceTexture mCameratexture;

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

    void preparescanner() {
        if (mCamera == null) {
            mCamera = opencamera();
        }
        if (mCamera == null) {
            Log.e(TAG, "prepare scanner couldn't connect to camera!");
            return;
        }

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

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        // 1초에 1프레임
        if (count != 15) {
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
            bitmap = Bitmap.createScaledBitmap(bitmap, 400, 300, false);

            String date = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss").format(new Date());
            String filepath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Gproject";
            String filename = date + ".png";
            SaveBitmapToFileCache(bitmap, filepath, filename);
            File tempfile = new File(filepath+"/", filename);
            //send bitmap
            FileUploadUtil.send2Server(tempfile);
            count = 0;
        }
    }

    protected void stopcamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
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
}
