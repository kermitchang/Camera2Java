package com.kermit.cameraforjava;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private  String TAG = "MainActivity";
    private String TAG_KERMIT = "Kermit ";

    private static Button mBtnStartCamera = null;
    private static SurfaceView mSurfacePreview = null;
    private static SurfaceHolder mSurfaceHolder = null;

    private int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private String []strPermission = { Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};

    private ImageReader mImageReader = null;
    private CameraManager mCameraManager = null;
    private CameraDevice mCameraDevice = null;
    private CameraCaptureSession mCameraCaptureSession = null;
    private CaptureRequest.Builder mCaptureRequestBuilder = null;


    private Handler mainHandler = null;
    private Handler childHandler = null;
    private int CameraWidth = 0;
    private int CameraHeighth = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, TAG_KERMIT + "onCreate");

        mBtnStartCamera = (Button)findViewById(R.id.btnStrCamera);
        mSurfacePreview = (SurfaceView) findViewById(R.id.surfaceView);

        mBtnStartCamera.setOnClickListener(mBtnStartClickCallback);

        mSurfaceHolder = mSurfacePreview.getHolder();
        mSurfaceHolder.setKeepScreenOn(true);
        mSurfaceHolder.addCallback(mSurfaceHolderCallback);

    }

    @Override
    public void onStart(){
        super.onStart();
        Log.d(TAG, TAG_KERMIT + "onStart");

    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d(TAG, TAG_KERMIT + "onResume");

    }

    @Override
    public void onPause(){
        super.onPause();
        Log.d(TAG, TAG_KERMIT + "onPause");

    }

    @Override
    public void onStop(){
        super.onStop();
        Log.d(TAG, TAG_KERMIT + "onStop");

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d(TAG, TAG_KERMIT + "onDestroy");

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        Log.d(TAG, TAG_KERMIT + "requestCode " + requestCode);
    }

    private void checkCameraPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, TAG_KERMIT + "checkCameraPermission hasn't permission");
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)){
                Log.d(TAG, TAG_KERMIT + "shouldShowRequestPermissionRationale true");
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
                mBuilder.setMessage("Please give me some permission");
                mBuilder.setPositiveButton("OK", mAlertDialogBuilderOK);
                mBuilder.setNegativeButton("NO", mAlertDialogBuilderNO);
                mBuilder.show();
            } else{
                Log.d(TAG, TAG_KERMIT + "shouldShowRequestPermissionRationale false");
                ActivityCompat.requestPermissions(this, strPermission, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            }
        } else{
            Log.d(TAG, TAG_KERMIT + "checkCameraPermission has permission");
        }
    }

    private void startCameraSession(){
        Log.d(TAG, TAG_KERMIT + "startCameraSession");

        HandlerThread mHandlerThread = new HandlerThread("Camera2");
        mHandlerThread.start();
        childHandler = new Handler(mHandlerThread.getLooper());
        mainHandler = new Handler(getMainLooper());

        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (mCameraManager.getCameraIdList().length == 0) {
                Log.d(TAG, "Kermit no camera");
                return ;
            }
            Log.d(TAG, TAG_KERMIT + "Cameara Value " + mCameraManager.getCameraIdList().length);
            String firstCamera = mCameraManager.getCameraIdList()[0];
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                mCameraManager.openCamera(firstCamera, mCameraDeviceStateCallback, mainHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void createCameraPreviewSessionLocked(){
        Log.d(TAG, TAG_KERMIT + "createCameraPreviewSessionLocked");
        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(mSurfaceHolder.getSurface());
            mCameraDevice.createCaptureSession(Arrays.asList(mSurfaceHolder.getSurface(), mImageReader.getSurface()), mCameraCaptureSessionStateCallback, childHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private View.OnClickListener mBtnStartClickCallback = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            Log.d(TAG, TAG_KERMIT + "onClick");
            checkCameraPermission();
            startCameraSession();
        }
    };

    private DialogInterface.OnClickListener mAlertDialogBuilderOK = new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Log.d(TAG, TAG_KERMIT + "onclick of mAlertDialogBuilderOK");
            ActivityCompat.requestPermissions(MainActivity.this, strPermission, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        }
    };

    private DialogInterface.OnClickListener mAlertDialogBuilderNO = new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Log.d(TAG, TAG_KERMIT + "onclick of mAlertDialogBuilderNO");
        }
    };

    private SurfaceHolder.Callback mSurfaceHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.d(TAG, TAG_KERMIT + "surfaceCreated");
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.d(TAG, TAG_KERMIT + "surfaceChanged");
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.d(TAG, TAG_KERMIT + "surfaceDestroyed");
        }
    };

    private final CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.d(TAG, TAG_KERMIT + "onclick of onOpened");
            mCameraDevice = camera;
            try {
                CameraCharacteristics mCameraCharacteristics = mCameraManager.getCameraCharacteristics(mCameraDevice.getId());
                StreamConfigurationMap mStreamConfigurationMap = mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                Size[] mOutputSize = mStreamConfigurationMap.getOutputSizes(ImageFormat.YUV_420_888);
                CameraWidth = mOutputSize[0].getWidth();
                CameraHeighth = mOutputSize[0].getHeight();
                mImageReader = ImageReader.newInstance(mOutputSize[0].getWidth(), mOutputSize[0].getHeight(), ImageFormat.JPEG, 5);
                mImageReader.setOnImageAvailableListener(mImageReaderOnImageAvailableListener, mainHandler);
                Log.d(TAG, TAG_KERMIT + "Kermit W:" + mOutputSize[0].getWidth() + " H:" + mOutputSize[0].getHeight());
                createCameraPreviewSessionLocked();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.d(TAG, TAG_KERMIT + "onclick of onDisconnected");

        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.d(TAG, TAG_KERMIT + "onclick of onError");

        }
    };

    private ImageReader.OnImageAvailableListener mImageReaderOnImageAvailableListener = new ImageReader.OnImageAvailableListener(){
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image mImage = reader.acquireNextImage();
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
        }
    };

    private CameraCaptureSession.StateCallback mCameraCaptureSessionStateCallback = new CameraCaptureSession.StateCallback(){

        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            Log.d(TAG, TAG_KERMIT + "onConfigured");
            try {
                mCameraCaptureSession = session;
                mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                CaptureRequest mCaptureRequest = mCaptureRequestBuilder.build();
                mCameraCaptureSession.setRepeatingRequest(mCaptureRequest, mCameraCaptureSessionCaptureCallback, childHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.d(TAG, TAG_KERMIT + "onConfigureFailed");
        }
    };

    private CameraCaptureSession.CaptureCallback mCameraCaptureSessionCaptureCallback = new CameraCaptureSession.CaptureCallback(){
        @Override
        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
            //Log.d(TAG, TAG_KERMIT + "onCaptureStarted");
        }

        @Override
        public void onCaptureBufferLost (CameraCaptureSession session, CaptureRequest request, Surface target, long frameNumber){
            //Log.d(TAG, TAG_KERMIT + "onCaptureBufferLost");
        }

        @Override
        public void onCaptureCompleted (CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result){
            //Log.d(TAG, TAG_KERMIT + "onCaptureCompleted");
        }

        @Override
        public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure){
            //Log.d(TAG, TAG_KERMIT + "onCaptureFailed");
        }

        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult){
            //Log.d(TAG, TAG_KERMIT + "onCaptureProgressed");
        }

        @Override
        public void onCaptureSequenceAborted(CameraCaptureSession session, int sequenceId){
            //Log.d(TAG, TAG_KERMIT + "onCaptureSequenceAborted");
        }

        @Override
        public void onCaptureSequenceCompleted(CameraCaptureSession session, int sequenceId, long frameNumber){
            //Log.d(TAG, TAG_KERMIT + "onCaptureSequenceCompleted");
        }

    };
}