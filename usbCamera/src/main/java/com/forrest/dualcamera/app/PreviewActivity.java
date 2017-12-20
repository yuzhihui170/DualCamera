package com.forrest.dualcamera.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.serenegiant.common.BaseActivity;
import com.serenegiant.dualcamera.R;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.USBMonitor.OnDeviceConnectListener;
import com.serenegiant.usb.USBMonitor.UsbControlBlock;
import com.serenegiant.usb.UVCCamera;
import com.forrest.widget.UVCCameraGLSurfaceView;
import com.forrest.usbcameracommon.UVCCameraHandlerSimple;
import com.forrest.dualcamera.utils.FileUtil;
import com.forrest.dualcamera.utils.MemoryUtil;

import java.lang.ref.WeakReference;
import java.util.List;

public final class PreviewActivity extends BaseActivity implements CameraDialog.CameraDialogParent {
	private static final boolean DEBUG = true;	// FIXME set false when production
	private static final String TAG = "PreviewActivity";

	private static final int H_AUTO_START_RECORD = 0x01;
	private static final int H_AUTO_STOP_RECORD =  0x02;
	private static final int MEMORY_LIMIT = 60 * 1024 * 1024;

	private static final float[] BANDWIDTH_FACTORS = { 0.5f, 0.5f };

    // for accessing USB and USB camera
    private USBMonitor mUSBMonitor;

	private FrameLayout mLayoutR;
	private UVCCameraHandlerSimple mHandlerR;
	private UVCCameraGLSurfaceView mUVCCameraViewR;
	private ImageButton mCaptureButtonR;
	private Surface mRightPreviewSurface;

	private FrameLayout mLayoutL;
	private UVCCameraHandlerSimple mHandlerL;
	private UVCCameraGLSurfaceView mUVCCameraViewL;
	private ImageButton mCaptureButtonL;
	private Surface mLeftPreviewSurface;

	private int mWidthL = 1920; // Sonix(凹) mProductName=V930AF ["2592x1944","2048x1536","1920x1080","1600x1200","1280x720","1024x768","800x600","640x480","2592x1944"]
	private int mHeightL = 1080;
	private int mWidthR = 800;  // Etron(平) mProductName=Camera33 ["640x480","800x600","1280x1024","1600x1200","2048x1536"]
	private int mHeightR = 600;
	private int mRecordTime = 5;

	private boolean mSurfaceCreatefinish = false;
	private int mSurfaceCnt = 0;
	private final Object mReadyFence = new Object();
	private List<UsbDevice> list;

	private ImageView mIVSetting;
	private ImageView mIVGallery;
	private ImageView mIVCameraSelect;
	private Chronometer mCHRecordTime;

	private SharedPreferences mSharedPreferences;
	private Handler mHandler;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_preview);
		initView();
		getPreferences();
		mHandler = new MyHandler(this);
		Log.d(TAG, "[PreviewActivity] : onCreate");
	}

	@Override
	protected void onStart() {
		super.onStart();

		mLayoutL = (FrameLayout) findViewById(R.id.camera_layout_L);
		mUVCCameraViewL = new UVCCameraGLSurfaceView(this); //(UVCCameraTextureView)findViewById(R.id.camera_view_L);
		mUVCCameraViewL.setAspectRatio((float)mWidthL / (float)mHeightL);
		mUVCCameraViewL.setSurfaceListener(mySurfaceListener);
		mLayoutL.addView(mUVCCameraViewL);
		mCaptureButtonL = (ImageButton)findViewById(R.id.capture_button_L);
		mCaptureButtonL.setOnClickListener(mOnClickListener);
		mCaptureButtonL.setVisibility(View.INVISIBLE);
		mHandlerL = UVCCameraHandlerSimple.createHandler(this,  mWidthL, mHeightL, UVCCamera.FRAME_FORMAT_MJPEG, BANDWIDTH_FACTORS[0]);

		mLayoutR = (FrameLayout) findViewById(R.id.camera_layout_R);
		mUVCCameraViewR = new UVCCameraGLSurfaceView(this); // (CameraViewInterface)findViewById(R.id.camera_view_R);
		mUVCCameraViewR.setAspectRatio(mWidthR / (float)mHeightR);
		mUVCCameraViewR.setSurfaceListener(mySurfaceListener);
		mLayoutR.addView(mUVCCameraViewR);
		mCaptureButtonR = (ImageButton)findViewById(R.id.capture_button_R);
		mCaptureButtonR.setOnClickListener(mOnClickListener);
		mCaptureButtonR.setVisibility(View.INVISIBLE);
//		mHandlerR = UVCCameraHandler.createHandler(this, mUVCCameraViewR, mWidthR, mHeightR, BANDWIDTH_FACTORS[1]);
		mHandlerR = UVCCameraHandlerSimple.createHandler(this,  mWidthR, mHeightR, UVCCamera.FRAME_FORMAT_MJPEG, BANDWIDTH_FACTORS[1]);

		mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);

		mUSBMonitor.register();
//		mUSBMonitor.dumpDevices();
		final List<DeviceFilter> filter = DeviceFilter.getDeviceFilters(this, com.serenegiant.uvccamera.R.xml.device_filter);
		list = mUSBMonitor.getDeviceList(filter.get(0));
		for (int i=0; i<list.size(); i++) {
			mUSBMonitor.requestPermission(list.get(i));
		}
		if (mUVCCameraViewL != null) {
			mUVCCameraViewL.onResume();
		}
		if (mUVCCameraViewR != null) {
			mUVCCameraViewR.onResume();
		}
		Log.d(TAG, "[PreviewActivity] : onStart");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "[PreviewActivity] : onResume");
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "[PreviewActivity] : onPause");
	}

	@Override
	protected void onStop() {
        Log.d(TAG, "[PreviewActivity] : 0");
		super.onStop();
        Log.d(TAG, "[PreviewActivity] : 1");
		mHandlerR.close();
		if (mUVCCameraViewR != null) {
			mUVCCameraViewR.onPause();
		}
        Log.d(TAG, "[PreviewActivity] : 2");
		mCaptureButtonR.setVisibility(View.INVISIBLE);

		mCHRecordTime.stop();
		mCHRecordTime.setVisibility(View.INVISIBLE);
        Log.d(TAG, "[PreviewActivity] : 3");
        mHandlerL.close();
		if (mUVCCameraViewL != null) {
			mUVCCameraViewL.onPause();
		}
		mCaptureButtonL.setVisibility(View.INVISIBLE);
		mUSBMonitor.unregister();
        Log.d(TAG, "[PreviewActivity] : 4");
		mLayoutL.removeView(mUVCCameraViewL);
		mLayoutR.removeView(mUVCCameraViewR);
		Log.d(TAG, "[PreviewActivity] : onStop");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mHandlerR != null) {
			mHandlerR = null;
  		}
		if (mHandlerL != null) {
			mHandlerL = null;
  		}
		if (mUSBMonitor != null) {
			mUSBMonitor.destroy();
			mUSBMonitor = null;
		}
		mUVCCameraViewR = null;
		mCaptureButtonR = null;
		mUVCCameraViewL = null;
		mCaptureButtonL = null;
		Log.d(TAG, "[PreviewActivity] : onDestroy");
	}

	private void initView() {
		mIVSetting = (ImageView) findViewById(R.id.iv_setting);
		mIVGallery = (ImageView) findViewById(R.id.iv_gallery);
		mIVCameraSelect = (ImageView) findViewById(R.id.iv_camera_select);
		mIVSetting.setOnClickListener(mOnClickListener);
		mIVGallery.setOnClickListener(mOnClickListener);
		mIVCameraSelect.setOnClickListener(mOnClickListener);
		mCHRecordTime = (Chronometer) findViewById(R.id.ch_record_time);
	}

	private void getPreferences() {
		mSharedPreferences = getSharedPreferences(Constant.APP_NAME, Context.MODE_PRIVATE);
		String recordSize = mSharedPreferences.getString(Constant.KEY_RECORD_SIZE, Constant.VALUE_SIZE_480P);
		if (recordSize.equals(Constant.VALUE_SIZE_480P)) {
			mWidthR = 640;
			mHeightR = 480;
		} else if (recordSize.equals(Constant.VALUE_SIZE_600P)) {
			mWidthR = 800;
			mHeightR = 600;
		}

		String pictureSize = mSharedPreferences.getString(Constant.KEY_PICTURE_SIZE, Constant.VALUE_SIZE_1080P);
		if (pictureSize.equals(Constant.VALUE_SIZE_480P)) {
			mWidthL = 640;
			mHeightL = 480;
		} else if (pictureSize.equals(Constant.VALUE_SIZE_600P)) {
			mWidthL = 800;
			mHeightL = 600;
		} else if (pictureSize.equals(Constant.VALUE_SIZE_720P)) {
			mWidthL = 1280;
			mHeightL = 720;
		} else if (pictureSize.equals(Constant.VALUE_SIZE_1080P)) {
			mWidthL = 1920;
			mHeightL = 1080;
		}

		mSharedPreferences.getInt(Constant.KEY_RECORD_TIME, 5);
	}

	private final OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(final View view) {
			switch (view.getId()) {
				case R.id.iv_gallery:
					Intent intent = new Intent();
					intent.setClassName("com.google.android.apps.photos", "com.google.android.apps.photos.home.HomeActivity");
					startActivity(intent);
					break;

				case R.id.iv_setting:
					Intent intent2 = new Intent(PreviewActivity.this, SettingsActivity.class);
					startActivity(intent2);
					finish();
					break;

				case R.id.iv_camera_select:
					if (mHandlerL != null && mHandlerR != null) {
						if (!mHandlerL.isOpened() || !mHandlerR.isOpened()) {
							CameraDialog.showDialog(PreviewActivity.this);

						} else {
							mHandlerL.close();
							mHandlerR.close();
							setCameraButton();
						}
					}
					break;

				case R.id.capture_button_L:
					if (mHandlerL != null) {
						if (mHandlerL.isOpened()) {
							if (checkPermissionWriteExternalStorage()) {
								if (MemoryUtil.getSystemAvailableSize() < MEMORY_LIMIT) {
									Toast.makeText(PreviewActivity.this, "内存不足, 无法拍照!!!", Toast.LENGTH_LONG).show();
									break;
								}
								String picturePath = FileUtil.getPictureFile(Environment.DIRECTORY_DCIM, ".jpg").getAbsolutePath();
								mUVCCameraViewL.takePicture(mWidthL, mHeightL, picturePath);
								Log.d(TAG, "Picture : " + picturePath);
								Toast.makeText(PreviewActivity.this, "拍照", Toast.LENGTH_SHORT).show();
							}
						}
					}
					break;

				case R.id.capture_button_R:
					if (mHandlerR != null) {
						if (mHandlerR.isOpened()) {
							if (checkPermissionWriteExternalStorage() && checkPermissionAudio()) {
								if (!mUVCCameraViewR.isRecording()) {
									if (MemoryUtil.getSystemAvailableSize() < MEMORY_LIMIT) {
										Toast.makeText(PreviewActivity.this, "内存不足, 无法录像!!!", Toast.LENGTH_LONG).show();
										break;
									}
									mCaptureButtonR.setImageResource(R.drawable.record_off);
									String videoPath = FileUtil.getVideoFile(Environment.DIRECTORY_DCIM, ".mp4").getAbsolutePath();
									mUVCCameraViewR.startRecord(mWidthR, mHeightR, videoPath);
									mCHRecordTime.setBase(SystemClock.elapsedRealtime());
									mCHRecordTime.setVisibility(View.VISIBLE);
									mCHRecordTime.start();
									Toast.makeText(PreviewActivity.this, "录像开始", Toast.LENGTH_LONG).show();
									mHandler.removeMessages(H_AUTO_STOP_RECORD);
									mHandler.sendEmptyMessageDelayed(H_AUTO_STOP_RECORD, mRecordTime*1000*60);
									mHandler.post(mMemRunnable);

								} else {
									mCaptureButtonR.setImageResource(R.drawable.record_onn);	// return to default color
									mUVCCameraViewR.stopRecord();
									mCHRecordTime.stop();
									mCHRecordTime.setVisibility(View.INVISIBLE);
									Toast.makeText(PreviewActivity.this, "录像停止", Toast.LENGTH_LONG).show();
									mHandler.removeMessages(H_AUTO_START_RECORD);
									mHandler.removeMessages(H_AUTO_STOP_RECORD);
								}
							}
						}
					}
				break;
			}
		}
	};

	private final OnDeviceConnectListener mOnDeviceConnectListener = new OnDeviceConnectListener() {

		@Override
		public void onAttach(final UsbDevice device) {
//			if (DEBUG) Log.v(TAG, "onAttach:" + device);
			Toast.makeText(PreviewActivity.this, "USB_DEVICE_ATTACHED", Toast.LENGTH_SHORT).show();
		}

		@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
		@Override
		public void onConnect(final UsbDevice device, final UsbControlBlock ctrlBlock, final boolean createNew) {
			if (DEBUG) Log.v(TAG, "onConnect:");
			Log.v(TAG, "getProductName.name :" + device.getProductName());
			synchronized (mReadyFence) {
				while (!mSurfaceCreatefinish) {
					try {
						mReadyFence.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			if (!mHandlerR.isOpened() /*&& device.getProductName().trim().equals(Constant.CAMERA_PRODUCT_NAME_FOR_RECORD)*/) {
				if (DEBUG) Log.v(TAG, "onConnect: mHandler R");
				mHandlerR.open(ctrlBlock);
				final SurfaceTexture st = mUVCCameraViewR.getSurfaceTexture();
				mHandlerR.startPreview(new Surface(st));
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mCaptureButtonR.setVisibility(View.VISIBLE);
					}
				});

			} else if (!mHandlerL.isOpened() /*&& device.getProductName().trim().equals(Constant.CAMERA_PRODUCT_NAME_FOR_PICTURE)*/) {
				if (DEBUG) Log.v(TAG, "onConnect: mHandlerL ");
				mHandlerL.open(ctrlBlock);
				final SurfaceTexture st = mUVCCameraViewL.getSurfaceTexture();
				mHandlerL.startPreview(new Surface(st));
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mCaptureButtonL.setVisibility(View.VISIBLE);
					}
				});
			}
		}

		@Override
		public void onDisconnect(final UsbDevice device, final UsbControlBlock ctrlBlock) {
			if (DEBUG) Log.v(TAG, "onDisconnect:");
			if ((mHandlerL != null) && !mHandlerL.isEqual(device)) {
				queueEvent(new Runnable() {
					@Override
					public void run() {
						mHandlerL.close();
						if (mLeftPreviewSurface != null) {
							mLeftPreviewSurface.release();
							mLeftPreviewSurface = null;
						}
						setCameraButton();
					}
				}, 0);

			} else if ((mHandlerR != null) && !mHandlerR.isEqual(device)) {
				queueEvent(new Runnable() {
					@Override
					public void run() {
						mHandlerR.close();
						if (mRightPreviewSurface != null) {
							mRightPreviewSurface.release();
							mRightPreviewSurface = null;
						}
						setCameraButton();
					}
				}, 0);
			}
		}

		@Override
		public void onDettach(final UsbDevice device) {
			if (DEBUG) Log.v(TAG, "onDettach:" + device.getProductName());
			Toast.makeText(PreviewActivity.this, "USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onCancel(final UsbDevice device) {
			if (DEBUG) Log.v(TAG, "onCancel:");
		}
	};

	/**
	 * to access from CameraDialog
	 * @return
	 */
	@Override
	public USBMonitor getUSBMonitor() {
		return mUSBMonitor;
	}

	@Override
	public void onDialogResult(boolean canceled) {
		if (canceled) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					setCameraButton();
				}
			}, 0);
		}
	}

	private void setCameraButton() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if ((mHandlerL != null) && !mHandlerL.isOpened() && (mCaptureButtonL != null)) {
					mCaptureButtonL.setVisibility(View.INVISIBLE);
				}
				if ((mHandlerR != null) && !mHandlerR.isOpened() && (mCaptureButtonR != null)) {
					mCaptureButtonR.setVisibility(View.INVISIBLE);
				}
			}
		}, 0);
	}

	private final UVCCameraGLSurfaceView.MySurfaceListener mySurfaceListener = new UVCCameraGLSurfaceView.MySurfaceListener() {
		@Override
		public void surfaceCreated() {
			synchronized (mReadyFence) {
				if (++mSurfaceCnt == 2) {
					mSurfaceCreatefinish = true;
					mReadyFence.notify();
				}
			}
		}

		@Override
		public void surfaceChanged() {

		}

		@Override
		public void surfaceDestroyed() {
			synchronized (mReadyFence) {
				mSurfaceCnt--;
				if (mSurfaceCnt == 0) {
					mSurfaceCreatefinish = false;
				}
			}
		}
	};

	private static class MyHandler extends Handler {
		private final WeakReference<PreviewActivity> mActivity;

		public MyHandler(PreviewActivity activity) {
			this.mActivity = new WeakReference<PreviewActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			PreviewActivity activity = mActivity.get();
			if (activity != null) {
				switch (msg.what) {
					case H_AUTO_START_RECORD:
						if (activity.mHandlerR != null && activity.mHandlerR.isOpened() && !activity.mUVCCameraViewR.isRecording()) {
							activity.mCaptureButtonR.setImageResource(R.drawable.record_off);
                            String videoPath = FileUtil.getVideoFile(Environment.DIRECTORY_DCIM, ".mp4").getAbsolutePath();
							activity.mUVCCameraViewR.startRecord(activity.mWidthR, activity.mHeightR, videoPath);
							activity.mCHRecordTime.setBase(SystemClock.elapsedRealtime());
							activity.mCHRecordTime.setVisibility(View.VISIBLE);
							activity.mCHRecordTime.start();
							this.sendEmptyMessageDelayed(H_AUTO_STOP_RECORD, activity.mRecordTime * 1000 * 60);
							Toast.makeText(activity, "录像开始", Toast.LENGTH_LONG).show();
						}
						break;

					case H_AUTO_STOP_RECORD:
						if (activity.mHandlerR != null && activity.mHandlerR.isOpened() && activity.mUVCCameraViewR.isRecording()) {
							activity.mCaptureButtonR.setImageResource(R.drawable.record_onn);	// return to default color
							activity.mUVCCameraViewR.stopRecord();
							activity.mCHRecordTime.stop();
							activity.mCHRecordTime.setVisibility(View.INVISIBLE);
							Toast.makeText(activity, "本段录像时间到,停止录像.", Toast.LENGTH_LONG).show();
							this.sendEmptyMessageDelayed(H_AUTO_START_RECORD, 3000);
						}
						break;
				}
			}
		}
	}


	private Runnable mMemRunnable = new Runnable() {
		@Override
		public void run() {
			if (mUVCCameraViewR.isRecording()) {
				if (MemoryUtil.getSystemAvailableSize() < MEMORY_LIMIT) {
					mCaptureButtonR.setImageResource(R.drawable.record_onn);	// return to default color
                    mUVCCameraViewR.stopRecord();
					mCHRecordTime.stop();
					mCHRecordTime.setVisibility(View.INVISIBLE);
					Toast.makeText(PreviewActivity.this, "内存不足, 录像停止", Toast.LENGTH_LONG).show();
					mHandler.removeMessages(H_AUTO_START_RECORD);
					mHandler.removeMessages(H_AUTO_STOP_RECORD);
				}
				mHandler.postDelayed(mMemRunnable, 1000);
			}
		}
	};

	private void loadApps() {
		List<ResolveInfo> apps;
		Intent intent = new Intent(Intent.ACTION_MAIN, null);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		apps = getPackageManager().queryIntentActivities(intent, 0);
		//for循环遍历ResolveInfo对象获取包名和类名
		for (int i = 0; i < apps.size(); i++) {
			ResolveInfo info = apps.get(i);
			String packageName = info.activityInfo.packageName;
			CharSequence cls = info.activityInfo.name;
			CharSequence name = info.activityInfo.loadLabel(getPackageManager());
			Log.d(TAG,name + "----" + packageName + "----"+cls);
		}
	}
}
