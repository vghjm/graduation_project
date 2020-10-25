package com.example.graduateproject;

import android.app.Service;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.OrientationEventListener;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

public class CameraService extends Service {
    FaceScanner mFaceScanner = new FaceScanner();
    private OrientationEventListener orientationListener;
    public static int ori;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mFaceScanner.preparescanner();
        orientationListener = new OrientationEventListener(this,
                SensorManager.SENSOR_DELAY_UI) {
            @Override
            public void onOrientationChanged(int orientation) {
                ori = orientation;
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        orientationListener.enable();
        return super.onStartCommand(intent, flags, startId);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onDestroy() {
        super.onDestroy();
        // 얼굴 감지 종료
        if (mFaceScanner != null) {
            mFaceScanner.endScanning();
            mFaceScanner = null;
        }
        if (orientationListener != null) {
            orientationListener.disable();
        }
    }
}
