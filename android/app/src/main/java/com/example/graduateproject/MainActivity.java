package com.example.graduateproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    //Context context = getApplicationContext();
    Button startbutton;
    TextView output;
    public static boolean servicecheck = false;
    public static Context mContext;

    public void setText(String text) {
        output.setText(text);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        // 권한 설정
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, 200);
        }

        startbutton = (Button) findViewById(R.id.button);
        output = (TextView) findViewById(R.id.textView);
        startbutton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent tmpIntent = new Intent(getApplicationContext(), CameraService.class);
                if(!servicecheck)
                {
                    servicecheck = true;
                    startbutton.setText("SERVICE END");
                    startService(tmpIntent);
                }else{
                    servicecheck = false;
                    startbutton.setText("SERVICE START");
                    stopService(tmpIntent);
                }

            }
        });

    }
}

