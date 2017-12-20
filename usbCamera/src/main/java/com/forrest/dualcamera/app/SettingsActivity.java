package com.forrest.dualcamera.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.serenegiant.dualcamera.R;
import com.forrest.dualcamera.ui.ActionSheetDialog;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "SettingsActivity";
    private RelativeLayout mLayoutRecord;
    private RelativeLayout mLayoutPicture;
    private RelativeLayout mLayoutRecordTime;
    private TextView mTVRecord;
    private TextView mTVPicture;
    private TextView mTVRecordTime;

    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mSharedPreferences = getSharedPreferences(Constant.APP_NAME, Context.MODE_PRIVATE);
        initView();
        Log.d(TAG, "[SettingsActivity] : onCreate");
    }

    @Override
    protected void onStart() {
        super.onStart();
        mTVRecord.setText(mSharedPreferences.getString(Constant.KEY_RECORD_SIZE, Constant.VALUE_SIZE_480P));
        mTVPicture.setText(mSharedPreferences.getString(Constant.KEY_PICTURE_SIZE, Constant.VALUE_SIZE_1080P));
        mTVRecordTime.setText(String.valueOf(mSharedPreferences.getInt(Constant.KEY_RECORD_TIME, 5) ) + "分钟");
        Log.d(TAG, "[SettingsActivity] : onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "[SettingsActivity] : onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "[SettingsActivity] : onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "[SettingsActivity] : onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "[SettingsActivity] : onDestroy");
    }

    private void initView() {
        mLayoutRecord = (RelativeLayout) findViewById(R.id.layout_record);
        mLayoutPicture = (RelativeLayout) findViewById(R.id.layout_picture);
        mLayoutRecordTime = (RelativeLayout) findViewById(R.id.layout_record_time);
        mTVRecord = (TextView) findViewById(R.id.tv_record);
        mTVPicture = (TextView) findViewById(R.id.tv_picture);
        mTVRecordTime = (TextView) findViewById(R.id.tv_record_time);
        mLayoutRecord.setOnClickListener(this);
        mLayoutPicture.setOnClickListener(this);
        mLayoutRecordTime.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(SettingsActivity.this, PreviewActivity.class);
                startActivity(intent);
                finish();
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_record:
                new ActionSheetDialog(this)
                        .builder()
                        .setTitle("录像分辨率")
                        .setCancelable(false)
                        .setCanceledOnTouchOutside(false)
                        .addSheetItem(Constant.VALUE_SIZE_480P, ActionSheetDialog.SheetItemColor.Blue,
                                new ActionSheetDialog.OnSheetItemClickListener() {
                                    @Override
                                    public void onClick(int which) {
                                        mTVRecord.setText(Constant.VALUE_SIZE_480P);
                                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                                        editor.putString(Constant.KEY_RECORD_SIZE, Constant.VALUE_SIZE_480P);
                                        editor.apply();
                                    }
                                })
                        .addSheetItem(Constant.VALUE_SIZE_600P, ActionSheetDialog.SheetItemColor.Blue,
                                new ActionSheetDialog.OnSheetItemClickListener() {
                                    @Override
                                    public void onClick(int which) {
                                        mTVRecord.setText(Constant.VALUE_SIZE_600P);
                                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                                        editor.putString(Constant.KEY_RECORD_SIZE, Constant.VALUE_SIZE_600P);
                                        editor.apply();
                                    }
                                })
                        .show();
                break;

            case R.id.layout_picture:
                new ActionSheetDialog(this)
                        .builder()
                        .setTitle("图片分辨率")
                        .setCancelable(false)
                        .setCanceledOnTouchOutside(false)
                        .addSheetItem(Constant.VALUE_SIZE_480P, ActionSheetDialog.SheetItemColor.Blue,
                                new ActionSheetDialog.OnSheetItemClickListener() {
                                    @Override
                                    public void onClick(int which) {
                                        mTVPicture.setText(Constant.VALUE_SIZE_480P);
                                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                                        editor.putString(Constant.KEY_PICTURE_SIZE, Constant.VALUE_SIZE_480P);
                                        editor.apply();
                                    }
                                })
                        .addSheetItem(Constant.VALUE_SIZE_600P, ActionSheetDialog.SheetItemColor.Blue,
                                new ActionSheetDialog.OnSheetItemClickListener() {
                                    @Override
                                    public void onClick(int which) {
                                        mTVPicture.setText(Constant.VALUE_SIZE_600P);
                                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                                        editor.putString(Constant.KEY_PICTURE_SIZE, Constant.VALUE_SIZE_600P);
                                        editor.apply();
                                    }
                                })
                        .addSheetItem(Constant.VALUE_SIZE_720P, ActionSheetDialog.SheetItemColor.Blue,
                                new ActionSheetDialog.OnSheetItemClickListener() {
                                    @Override
                                    public void onClick(int which) {
                                        mTVPicture.setText(Constant.VALUE_SIZE_720P);
                                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                                        editor.putString(Constant.KEY_PICTURE_SIZE, Constant.VALUE_SIZE_720P);
                                        editor.apply();
                                    }
                                })
                        .addSheetItem(Constant.VALUE_SIZE_1080P, ActionSheetDialog.SheetItemColor.Blue,
                                new ActionSheetDialog.OnSheetItemClickListener() {
                                    @Override
                                    public void onClick(int which) {
                                        mTVPicture.setText(Constant.VALUE_SIZE_1080P);
                                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                                        editor.putString(Constant.KEY_PICTURE_SIZE, Constant.VALUE_SIZE_1080P);
                                        editor.apply();
                                    }
                                })
                        .show();
                break;

            case R.id.layout_record_time:
                new ActionSheetDialog(this)
                        .builder()
                        .setTitle("录像时长")
                        .setCancelable(false)
                        .setCanceledOnTouchOutside(false)
                        .addSheetItem("5分钟", ActionSheetDialog.SheetItemColor.Blue,
                                new ActionSheetDialog.OnSheetItemClickListener() {
                                    @Override
                                    public void onClick(int which) {
                                        mTVRecordTime.setText("5分钟");
                                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                                        editor.putInt(Constant.KEY_RECORD_TIME, 5);
                                        editor.apply();
                                    }
                                })
                        .addSheetItem("15分钟", ActionSheetDialog.SheetItemColor.Blue,
                                new ActionSheetDialog.OnSheetItemClickListener() {
                                    @Override
                                    public void onClick(int which) {
                                        mTVRecordTime.setText("15分钟");
                                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                                        editor.putInt(Constant.KEY_RECORD_TIME, 15);
                                        editor.apply();
                                    }
                                })
                        .addSheetItem("30分钟", ActionSheetDialog.SheetItemColor.Blue,
                                new ActionSheetDialog.OnSheetItemClickListener() {
                                    @Override
                                    public void onClick(int which) {
                                        mTVRecordTime.setText("30分钟");
                                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                                        editor.putInt(Constant.KEY_RECORD_TIME, 30);
                                        editor.apply();
                                    }
                                })
                        .show();
                break;
        }
    }
}
