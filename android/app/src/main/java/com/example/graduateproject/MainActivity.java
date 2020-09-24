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
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    //Context context = getApplicationContext();
    Button startbutton;
    public static boolean servicecheck = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        startbutton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent tmpIntent = new Intent(getApplicationContext(), CameraService.class);
                if(!servicecheck)
                {
                    servicecheck = true;
                    Toast.makeText(getApplicationContext(), "SERVICE START", Toast.LENGTH_SHORT);
                    startService(tmpIntent);
                }else{
                    servicecheck = false;
                    Toast.makeText(getApplicationContext(), "SERVICE END", Toast.LENGTH_SHORT);
                    stopService(tmpIntent);
                }

            }
        });

    }

}

