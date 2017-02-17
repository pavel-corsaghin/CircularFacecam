package com.example.macos.cvtest1;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

/**
 * Created by macos on 17/02/2017.
 */

public class FaceCamService extends Service implements View.OnTouchListener, CameraBridgeViewBase.CvCameraViewListener2 {
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mParamsFloatingMode;
    private static final int WINDOW_TYPE = WindowManager.LayoutParams.TYPE_PHONE;
    private int mFlagHideStatusAndNavigation;
    private View mParentView;
    private int initialX;
    private int initialY;
    private int initialTouchX;
    private int initialTouchY;
    private CustomCameraView mCameraView;
    private boolean sCameraPreviewStarted = false;
    private Mat mRgba;

    private static final String TAG = "FaceCamService";


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        int windowSize = 400;

        mParamsFloatingMode = new WindowManager.LayoutParams(
                windowSize,
                windowSize,
                WINDOW_TYPE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
                PixelFormat.TRANSLUCENT);

        mParamsFloatingMode.gravity = Gravity.TOP | Gravity.START;
        mParamsFloatingMode.x = 50;
        mParamsFloatingMode.y = 100;

        mFlagHideStatusAndNavigation = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        loadViews();
    }

    private void loadViews() {
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        mParentView = inflater.inflate(R.layout.front_camera, null);
        mParentView.setFocusable(true);
        mParentView.setFocusableInTouchMode(true);
        mParentView.setSystemUiVisibility(mFlagHideStatusAndNavigation);
        mParentView.setOnTouchListener(this);

        mCameraView = (CustomCameraView) mParentView.findViewById(R.id.camera_view);
        mCameraView.setVisibility(SurfaceView.VISIBLE);
        mCameraView.setCvCameraViewListener(this);
        mCameraView.setZOrderOnTop(true);
        mCameraView.disableView();
        SurfaceHolder holder = mCameraView.getHolder();
        holder.setFormat(PixelFormat.TRANSLUCENT);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (view == mParentView) {
            int lastTouchX = (int) motionEvent.getRawX();
            int lastTouchY = (int) motionEvent.getRawY();
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initialX = mParamsFloatingMode.x;
                    initialY = mParamsFloatingMode.y;
                    initialTouchX = lastTouchX;
                    initialTouchY = lastTouchY;
                    break;
                case MotionEvent.ACTION_UP:
                    break;
                case MotionEvent.ACTION_MOVE:
                    int deltaX = lastTouchX - initialTouchX;
                    int deltaY = lastTouchY - initialTouchY;
                    if (Math.max(Math.abs(deltaX), Math.abs(deltaY)) > 10) {
                        mParamsFloatingMode.x = Math.max(initialX + deltaX, 5);
                        mParamsFloatingMode.y = Math.max(initialY + deltaY, 5);
                        mWindowManager.updateViewLayout(mParentView, mParamsFloatingMode);
                    }
                    break;
                default:
                    break;
            }
        }
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action;
        if (intent != null && (action = intent.getAction()) != null) {
            switch (action) {
                case AppConstants.ACTION_START_FACE_CAM:
                    mCameraView.enableView();
                    if (!OpenCVLoader.initDebug()) {
                        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
                    }
                    startCameraPreview();
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(width, height, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        return mRgba;
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS:
                    mCameraView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    private void startCameraPreview() {
        Log.v(TAG, "startCameraPreview");
        if (!sCameraPreviewStarted) {
            if (mParentView.getWindowToken() == null) {
                mWindowManager.addView(mParentView, mParamsFloatingMode);
            }
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            sCameraPreviewStarted = true;
        }
    }
}
