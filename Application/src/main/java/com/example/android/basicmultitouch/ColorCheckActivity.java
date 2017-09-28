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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
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
import java.util.Random;

/**
 * adb shell am start -a android.intent.action.MAIN -n com.example.android.basicmultitouch/com.example.android.basicmultitouch.ColorCheckActivity --es Color BLUE --ei Brightness 55
 * adb shell am start -a android.intent.action.MAIN -n com.example.android.basicmultitouch/com.example.android.basicmultitouch.ColorCheckActivity --es Pass true
 * */
public class ColorCheckActivity extends Activity{
    public static final String TAG = "ColorCheckActivity";
    private Context context = ColorCheckActivity.this;
    private Button mFailButton;
    private Button mPassButton;
    private TextView mInfoText;
    private Button mNextColorButton;
    private RelativeLayout mBackgroundRelativeLayout;

    private String COLOR_NAME_PARAMETER = "RED";
    private int BRIGHTNESS_PERCENT_PARAMETER = 100;
    private String PASS_PARAMETER = "";
    private int mCurrentColorIndex = 0;


    enum RESULT{PASS, FAIL}

    private final String COLORS[] = new String[]{
            "RED",
            "GREEN",
            "BLUE",
            "BLACK",
            "GRAY",
            "WHITE"
    };

    private final int COLOR_ARRAY[] = new int[]{
            Color.RED,
            Color.GREEN,
            Color.BLUE,
            Color.BLACK,
            Color.GRAY,
            Color.WHITE
    };


    int REQUEST_WRITE_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_checkcoloractivity);
        initViews();
        checkIntent(getIntent());
        //setBackgroundColor(COLOR_ARRAY[mCurrentColorIndex], BRIGHTNESS_PERCENT_PARAMETER);
        requestWriteFilePermission();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        checkIntent(intent);
        //setBackgroundColor(COLOR_ARRAY[mCurrentColorIndex], BRIGHTNESS_PERCENT_PARAMETER);
        super.onNewIntent(intent);
    }

    private void checkIntent(Intent intent){
        if(intent != null) {
            Bundle bundle = intent.getExtras();
            if(bundle != null) {
                PASS_PARAMETER = bundle.getString("Pass", "");
                COLOR_NAME_PARAMETER = bundle.getString("Color", "RED");
                BRIGHTNESS_PERCENT_PARAMETER = intent.getIntExtra("Brightness", 100);
                checkParameters(PASS_PARAMETER, COLOR_NAME_PARAMETER, BRIGHTNESS_PERCENT_PARAMETER);
            }
        }
    }

    private void initViews(){
        mFailButton = (Button) findViewById(R.id.fail_button);
        mPassButton = (Button) findViewById(R.id.pass_button);
        mNextColorButton = (Button) findViewById(R.id.next_color_button);
        mInfoText = (TextView) findViewById(R.id.info_textView);
        mBackgroundRelativeLayout = (RelativeLayout) findViewById(R.id.background_relativeLayout);

        mNextColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //nextColorAndBrightness();
                showColorPicker();
            }
        });

        mFailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateReport(RESULT.FAIL);
                finishTest();
            }
        });

        mPassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateReport(RESULT.PASS);
                finishTest();
            }
        });


    }

    private void showColorPicker() {
//        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
//
//        LayoutInflater inflater = this.getLayoutInflater();
//        View dialogView = inflater.inflate(R.layout.layout_color_picker, null);
//        dialogBuilder.setView(dialogView);
//        AlertDialog alertDialog = dialogBuilder.create();
//
//        alertDialog.show();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels / 4;

        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_color_picker);
        LinearLayout colorsLinearLayout = (LinearLayout)dialog.findViewById(R.id.colors_linearLayout);
        SeekBar seekBar = (SeekBar) dialog.findViewById(R.id.bright_seekBar);
        seekBar.setMax(100);
        seekBar.setProgress(BRIGHTNESS_PERCENT_PARAMETER);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setBackgroundBright(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        for(int i = 0 ; i < COLOR_ARRAY.length ; i++) {
            final int index = i;
            FrameLayout layout = new FrameLayout(context);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    width,
                    width,
                    Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
            lp.setMargins(10, 10, 10, 10);
            layout.setPadding(10, 10, 10,  10);
            layout.setBackgroundColor(COLOR_ARRAY[index]);
            layout.setLayoutParams(lp);
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCurrentColorIndex = index;
                    COLOR_NAME_PARAMETER = COLORS[mCurrentColorIndex];
                    setBackgroundColor(COLOR_ARRAY[index], BRIGHTNESS_PERCENT_PARAMETER);
                }
            });

            colorsLinearLayout.addView(layout);
        }


        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.dimAmount = 0.0f;
        lp.gravity = Gravity.CENTER;
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

        /*
        ColorPickerDialogBuilder
                .with(context)
                .setTitle("Choose color")
                .initialColor(COLOR_ARRAY[mCurrentColorIndex])
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .lightnessSliderOnly()
                .setOnColorSelectedListener(new OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int selectedColor) {
                        //toast("onColorSelected: 0x" + Integer.toHexString(selectedColor));
                    }
                })
                .setPositiveButton("ok", new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                        setBackgroundColor(selectedColor, 100);
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .build()
                .show();
        */
    }


    private void nextColor(){
        mCurrentColorIndex++;
        if(mCurrentColorIndex > COLORS.length) {
            mCurrentColorIndex = 0;
        }

        COLOR_NAME_PARAMETER = COLORS[mCurrentColorIndex];
        setBackgroundColor(COLOR_ARRAY[mCurrentColorIndex], BRIGHTNESS_PERCENT_PARAMETER);
    }

    private void nextColorAndBrightness(){
        mCurrentColorIndex++;
        if(mCurrentColorIndex > COLORS.length - 1) {
            mCurrentColorIndex = 0;
        }

        COLOR_NAME_PARAMETER = COLORS[mCurrentColorIndex];
        BRIGHTNESS_PERCENT_PARAMETER = new Random().nextInt(100);

        setBackgroundColor(COLOR_ARRAY[mCurrentColorIndex], BRIGHTNESS_PERCENT_PARAMETER);
    }

    private void setBackgroundColor(int color, int brightness){
        mInfoText.setText(String.format("當前顏色 %s , 當前亮度: %d", COLOR_NAME_PARAMETER, brightness));

        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        float value = brightness / 100.0f;
        hsv[2] = value;
        color = Color.HSVToColor(hsv);
        mBackgroundRelativeLayout.setBackgroundColor(color);
    }

    private void setBackgroundBright(int brightness){
        BRIGHTNESS_PERCENT_PARAMETER = brightness;
        mInfoText.setText(String.format("當前顏色 %s , 當前亮度: %d", COLOR_NAME_PARAMETER, brightness));
        float[] hsv = new float[3];
        int color = ((ColorDrawable)mBackgroundRelativeLayout.getBackground()).getColor();
        Color.colorToHSV(color, hsv);
        float value = brightness / 100.0f;
        hsv[2] = value;
        color = Color.HSVToColor(hsv);
        mBackgroundRelativeLayout.setBackgroundColor(color);
    }

    private void checkParameters(String pass, String colorName, int brightness){
        if(!TextUtils.isEmpty(pass)) {
            /* shutdown and get report */
            if(pass.equalsIgnoreCase("true")) {
                mPassButton.performClick();
            } else if(pass.equalsIgnoreCase("false")) {
                mFailButton.performClick();
            } else {
                /* set current */
                setBackgroundColor(COLOR_ARRAY[mCurrentColorIndex], BRIGHTNESS_PERCENT_PARAMETER);
            }
        } else {
            mCurrentColorIndex = getColorIndex(colorName);
            if(mCurrentColorIndex == -1){
            /* reset failure setting */
                mCurrentColorIndex = 0;
            }
            COLOR_NAME_PARAMETER = COLORS[mCurrentColorIndex];

            if(!isBrightnessAvailable(brightness)) {
            /* reset failure setting */
                BRIGHTNESS_PERCENT_PARAMETER = 100;
            }
            setBackgroundColor(COLOR_ARRAY[mCurrentColorIndex], BRIGHTNESS_PERCENT_PARAMETER);
        }
    }

    private int getColorIndex(String colorName){
        for(int i = 0 ; i < COLORS.length ; i++) {
            if(COLORS[i].equalsIgnoreCase(colorName)){
                return i;
            }
        }
        return -1;
    }

    private boolean isBrightnessAvailable(float brightness) {
        if(brightness < 0 || brightness > 100) {
            return false;
        }
        return true;
    }

    private void generateReport(RESULT result){
        String date = new SimpleDateFormat("[yyyy/MM/dd HH:mm:ss] ", Locale.TAIWAN).format(new Date());
        String report = String.format("%s 結果: %s", date, result.name());
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
        File file = new File("/sdcard/ColorCheckResult.txt");
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
            //startCountDownTimer();
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
                    //startCountDownTimer();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
