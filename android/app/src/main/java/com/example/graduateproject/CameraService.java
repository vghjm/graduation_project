package com.example.graduateproject;

import android.app.Service;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.view.OrientationEventListener;
import android.widget.Toast;

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
        // 서비스가 호출될 때마다 실행
        orientationListener.enable();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 얼굴 감지 종료
        Log.e("11", "1");
        if (mFaceScanner != null) {
            mFaceScanner.endScanning();
            mFaceScanner = null;
        }
        Log.e("22", "1");
        if (orientationListener != null) {
            orientationListener.disable();
        }
    }
}
