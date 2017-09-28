/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.basicmultitouch;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.common.logger.DKLog;
import com.example.android.common.logger.Trace;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * adb shell am start -a android.intent.action.MAIN -n com.example.android.basicmultitouch/com.example.android.basicmultitouch.MainActivity --ei Detect_Points 10 --ei Time_Out_Sec 10
 * */
public class MainActivity extends Activity implements ResultCallback{
    public static final String TAG = "MainActivity";
    private Context context = MainActivity.this;
    private TouchDisplayView mTouchDisplayView;
    private TextView setMaxTouchCountTextView;
    private TextView currentTouchCountTextView;
    private TextView setTimeoutTextView;
    private TextView currentTimeTextView;

    private int EXPECT_MAX_TOUCH_POINTERS = 5;
    private int TIMEOUT = 5;

    private CountDownTimer mCountDownTimer;

    enum RESULT{OK, TIMEOUT}

    int REQUEST_WRITE_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Intent intent = getIntent();
        if(intent != null) {
            EXPECT_MAX_TOUCH_POINTERS = intent.getIntExtra("Detect_Points", 5);
            TIMEOUT = intent.getIntExtra("Time_Out_Sec", 5);
        }
        setContentView(R.layout.layout_mainactivity);
        initViews();
        settingDefaultParameters(EXPECT_MAX_TOUCH_POINTERS, TIMEOUT);
        requestWriteFilePermission();

    }



    private void initViews(){
        mTouchDisplayView = (TouchDisplayView) findViewById(R.id.touchDisplayView);
        setMaxTouchCountTextView = (TextView) findViewById(R.id.set_max_touch_count_textView);
        currentTouchCountTextView = (TextView) findViewById(R.id.current_touch_count_textView);
        setTimeoutTextView = (TextView) findViewById(R.id.set_timeout_textView);
        currentTimeTextView = (TextView) findViewById(R.id.current_time_textView);

        mTouchDisplayView.bindShowInfoViews(
                setMaxTouchCountTextView,
                currentTouchCountTextView,
                setTimeoutTextView,
                currentTimeTextView);

        mTouchDisplayView.setResultCallback(this);
    }

    private void settingDefaultParameters(int maxPoints, int timeOut){
        if(maxPoints > 0 && maxPoints <= 10) {
            EXPECT_MAX_TOUCH_POINTERS = maxPoints;
        }

        if(timeOut > 0 && timeOut <= 600) {
            TIMEOUT = timeOut;
        }

        setMaxTouchCountTextView.setText(String.valueOf(EXPECT_MAX_TOUCH_POINTERS));
        setTimeoutTextView.setText(String.valueOf(TIMEOUT));
    }

    private void startCountDownTimer(){
        mCountDownTimer = new CountDownTimer(TIMEOUT * 1000 , 1000) {

            public void onTick(long millisUntilFinished) {
                currentTimeTextView.setText(String.valueOf(millisUntilFinished / 1000));
            }

            public void onFinish() {
                currentTimeTextView.setTextColor(Color.RED);
                currentTimeTextView.setText("超過時間!");
                /* generate report */
                generateReport(RESULT.TIMEOUT);
                /* quit */
                finishTest();
            }

        };
        mCountDownTimer.start();
    }


    @Override
    public void result(int maxPoints) {
        DKLog.i(TAG, Trace.getCurrentMethod() + "Max touch points: " + maxPoints);
        if(maxPoints >= EXPECT_MAX_TOUCH_POINTERS) {
            if(mCountDownTimer != null) {
                mCountDownTimer.cancel();
                mCountDownTimer = null;
                currentTouchCountTextView.setTextColor(Color.GREEN);
                /* generate report */
                generateReport(RESULT.OK);
                /* quit */
                finishTest();
            }
        }
    }

    private void generateReport(RESULT result){
        String date = new SimpleDateFormat("[yyyy/MM/dd HH:mm:ss] ", Locale.TAIWAN).format(new Date());
        String report = String.format("%s 最多觸碰點: %d , 結果: %s", date, mTouchDisplayView.getMaxTouchCount(), result.name());
        Toast.makeText(this, "測試結束: " + report, Toast.LENGTH_LONG).show();
        DKLog.d(TAG, Trace.getCurrentMethod() + report);
        writeToFile(report);
    }

    private void finishTest() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 1000);
    }

    private void writeToFile(String result) {
        File file = new File("/sdcard/TouchPointsResult.txt");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            BufferedWriter buf = new BufferedWriter(
                    new FileWriter(file,true));
            buf.append(result);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void requestWriteFilePermission() {
        ArrayList<String> arrayList = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            arrayList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(arrayList.size() > 0) {
            ActivityCompat.requestPermissions(this,arrayList.toArray(new String[0]), REQUEST_WRITE_PERMISSION);
        }else {
            startCountDownTimer();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,int[] grantResults) {
        if(requestCode == REQUEST_WRITE_PERMISSION){
            for(int grantResult : grantResults){
                if(grantResult != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "Permission Denied: 無權限輸出結果檔案!", Toast.LENGTH_SHORT).show();
                    break;
                }else{
                    startCountDownTimer();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
