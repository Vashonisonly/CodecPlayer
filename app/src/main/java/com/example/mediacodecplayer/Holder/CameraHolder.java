package com.example.mediacodecplayer.Holder;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
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
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CameraHolder {
    private Context context;
    private SurfaceHolder surfaceHolder;
    private CameraManager cameraManager;
    private CameraDevice mCameraDevice;
    private ImageReader cameraImageReader;
    //创建用于预览的Builder
    private CaptureRequest.Builder previewBuilder;
    //创建用于拍照的Builder
    private CaptureRequest.Builder pictureBuilder;
    private CameraCaptureSession mCameraCaptureSession;

    //消息队列
    //private Handler ImageSaverHandler;
    private Handler cameraHandler;
    private HandlerThread cameraHandlerThread;

    private final String TAG = "CameraHolder";
    private static final String CAMERAID = "0";

    public CameraHolder(SurfaceHolder surfaceHolder, Context context){
        this.surfaceHolder = surfaceHolder;
        this.context = context;
    }
    public void release(){
        mCameraDevice.close();
        cameraImageReader.close();
        this.release();
    }

    //初始化,起两个线程.UIThread,cameraThread
    public void init(){
        cameraHandlerThread = new HandlerThread("CameraHolder");
        cameraHandlerThread.start();
        cameraHandler = new Handler(cameraHandlerThread.getLooper());
        //UI线程,用来拍照
        //ImageSaverHandler = new Handler(context.getMainLooper());

        int width = surfaceHolder.getSurfaceFrame().width(),height = surfaceHolder.getSurfaceFrame().height();
        try{
            cameraManager = (CameraManager)context.getSystemService(Context.CAMERA_SERVICE);
            //函数体需要用到cameraManager
            //Size largest =  getCameraSizeMap();

            //拍照,JPEG,seven photos
            cameraImageReader = ImageReader.newInstance(width,height, ImageFormat.JPEG,7);
            cameraImageReader.setOnImageAvailableListener(on_ImageAvailableListener,cameraHandler);

            //动态权限申请
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                return;
            }
            String[] cameraIds = cameraManager.getCameraIdList();
            Log.i(TAG,"!!!!!!!!!!!!!");
            cameraManager.openCamera(CAMERAID,device_stateCallback,cameraHandler);
            Log.i(TAG,"!!!!!!!!!!!!!");
        }catch (CameraAccessException e){
            Toast.makeText(context,"openCamera fail: "+e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    //摄像完成执行此回调来进行图片的保存
    private ImageReader.OnImageAvailableListener on_ImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader imageReader) {
            Log.i(TAG,"ImageAvailable: ");
            File file = context.getExternalCacheDir();
            cameraHandler.post(new ImageSaver(imageReader.acquireNextImage(),file));
            Toast.makeText(context,"拍照完成 文件目录："+file.toString(),Toast.LENGTH_LONG).show();
        }
    };
    //此回调函数监控cameraDevice的当前状态
    private CameraDevice.StateCallback device_stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            Log.i(TAG,"cameraDevice can be open");
            //打开设备即开启预览
            mCameraDevice = cameraDevice;
            try{
                takePreview();
            }catch (CameraAccessException e){
                Toast.makeText(context,"start preview fail: "+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            if(mCameraDevice != null){
                mCameraDevice.close();
                mCameraDevice = null;
                cameraImageReader.close();
            }
        }

        @Override
        public void onError(CameraDevice cameraDevice, int i) {
            mCameraDevice.close();
            mCameraDevice = null;
            cameraImageReader.close();
        }
    };
    //监控摄像头配置完成与否情况的回调
    private CameraCaptureSession.StateCallback session_previewStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
            mCameraCaptureSession = cameraCaptureSession;
            try{
                //自动对焦
                previewBuilder.set(CaptureRequest.CONTROL_AF_MODE,CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                //打开闪光灯
                previewBuilder.set(CaptureRequest.CONTROL_AF_MODE,CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                //重复获取图像
                mCameraCaptureSession.setRepeatingRequest(previewBuilder.build(),null,cameraHandler);
            }catch (CameraAccessException e){
                Toast.makeText(context,"configured preview fail: "+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
            Toast.makeText(context,"onConfigureFailed: ",Toast.LENGTH_SHORT).show();
        }
    };
    //session_captureCallback 是上面onConfigured中的一个参数，不过目前设置为null,是用来处理拍照结果的反馈.(暂时不用)
    private CameraCaptureSession.CaptureCallback session_captureCallback = new CameraCaptureSession.CaptureCallback() {
        //正在拍照
        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
        }

        //拍照完成
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            try {
                Log.d(TAG,"onCaptureProgressed: take picture over");
                mCameraCaptureSession.setRepeatingRequest(previewBuilder.build(),null,cameraHandler);
            }catch (CameraAccessException e){
                Log.d(TAG,"restart preview fail: "+e.getMessage());
            }
        }

        //拍照失败
        @Override
        public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
            try {
                mCameraCaptureSession.setRepeatingRequest(previewBuilder.build(),null,cameraHandler);
            }catch (CameraAccessException e){
                Log.d(TAG,"restart preview fail: "+e.getMessage());
            }
        }
    };


    //拍照
    public void takePicture()throws CameraAccessException{
        if(mCameraDevice == null){
            Log.e(TAG,"take picture fail: because cameraDevice is null");
            return;
        }
        pictureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
        pictureBuilder.addTarget(cameraImageReader.getSurface());
        mCameraCaptureSession.stopRepeating();
        mCameraCaptureSession.abortCaptures();
        mCameraCaptureSession.capture(pictureBuilder.build(),session_captureCallback,null);
    }
    //预览
    public void takePreview()throws CameraAccessException{
        if(mCameraDevice == null){
            Log.e(TAG,"take preview fail: because cameraDevice is null");
            return;
        }
        previewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        previewBuilder.addTarget(surfaceHolder.getSurface());
        mCameraDevice.createCaptureSession(Arrays.asList(surfaceHolder.getSurface(),cameraImageReader.getSurface()),session_previewStateCallback,cameraHandler);
    }




    public Size getCameraSizeMap() throws CameraAccessException{
        CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(CAMERAID);
        //获取StreamConfigurationMap，它是管理摄像头支持的所有输出格式和尺寸
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        assert map != null;
        Size[] sizeMap = map.getOutputSizes(SurfaceTexture.class);
        for(Size option : sizeMap){
            Log.d(TAG,"size support is: "+option);
        }
        //对于静态图片，使用可用的最大值来拍摄。
        Size largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new Comparator<Size>(){
            @Override
            public int compare(Size lhs, Size rhs){
                return Long.signum(lhs.getHeight()*lhs.getWidth() - rhs.getHeight()*rhs.getWidth());
            }
        });
        return largest;
    }
}
