package com.example.mediacodecplayer.CodecState;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.util.Log;
import android.view.SurfaceHolder;

import com.example.mediacodecplayer.Holder.CameraHolder;
import com.example.mediacodecplayer.Holder.MicHolder;
import com.example.mediacodecplayer.Utils.MediaData;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class CodecState {
    private SurfaceHolder previewSurfaceHolder, displaySurfaceHolder;
    private Queue<MediaData> mediaDataQueue;
    private Context context;
    private CameraHolder cameraHolder;
    private MicHolder micHolder;

    private final String TAG = "CodecState";
    private static final int STATE_IDLE = 1;
    private static final int STATE_PREVIEW = 2;
    private static final int STATE_TAKEPICTURE = 3;
    private static final int STATE_PAUSED = 4;

    public CodecState(SurfaceHolder previewSurfaceHolder, SurfaceHolder disPlaySurfaceHolder, Context applicationContext) {
        Log.d(TAG,"initCamera begin");
        this.previewSurfaceHolder = previewSurfaceHolder;
        this.displaySurfaceHolder = disPlaySurfaceHolder;
        this.context = applicationContext;
        mediaDataQueue = new ArrayBlockingQueue(100);
        initCamera();
    }
    public void initCamera(){
        cameraHolder = new CameraHolder(previewSurfaceHolder,context);
        cameraHolder.init();
    }
    public void takeOnePicture(){
        try{
            cameraHolder.takePicture();
        }catch (CameraAccessException e){
            Log.d(TAG,"takeOnePicture fail: "+e.getMessage());
        }
    }
    public void release(){
        cameraHolder.release();
    }
}

