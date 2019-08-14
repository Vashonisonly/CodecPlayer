package com.example.mediacodecplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.example.mediacodecplayer.CodecState.CodecState;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback{
    private SurfaceHolder previewSurfaceHolder,disPlaySurfaceHolder;
    private SurfaceView previewSurface,disPlaySurface;
    private CodecState codecState;

    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        previewSurface = findViewById(R.id.LASurface);
        previewSurface.getHolder().addCallback(this);
        disPlaySurface = findViewById(R.id.RBSurface);
        disPlaySurface.getHolder().addCallback(this);
        Log.d(TAG,"finish");
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        this.finish();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent){
        Log.d(TAG,"onKeyDown: keyCode is: "+keyCode+" keyEvent is: "+keyEvent);
        switch (keyCode){
            //按1拍照
            case KeyEvent.KEYCODE_1:
                if(codecState != null){
                    codecState.takeOnePicture();
                }else {
                    Toast.makeText(this,"wrong,camera is not ready!",Toast.LENGTH_SHORT);
                }
                break;
                //返回
            case KeyEvent.KEYCODE_BACK:
                this.finish();
                break;
            default:
                Log.d(TAG,"nu vailable keyCode");
        }
        return true;
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder){
        Log.d(TAG,"created surface "+holder.getSurface());
        if(previewSurface.getHolder() == holder){
            previewSurfaceHolder = holder;
        }else {
            disPlaySurfaceHolder = holder;
        }
        if(previewSurfaceHolder != null && disPlaySurfaceHolder != null){
            Log.d(TAG,"start CodecState");
            new CodecPlayerTask().execute();
        }else{
            Log.e("MainActivity","can not open camera");
        }
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder,int format, int width, int height){

    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder){
        //codecState.release();
    }

    public class CodecPlayerTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            //this runs on a new thread
            Log.i(TAG,"xxxxxxxxx");
            initMediaCodecPlayer();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            //this runs on ui thread
        }
    }
    public void initMediaCodecPlayer(){
        Log.i(TAG,"---------------------");
        //动态权限申请
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            //申请权限，REQUEST_TAKE_PHOTO_PERMISSION是自定义的常量
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},2);
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},2);
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},2);
            //Log.e(TAG,"can not find permission for open camera!");
            //return;
        }
        Log.d(TAG,"---------------------");
        codecState = new CodecState(previewSurfaceHolder,disPlaySurfaceHolder,this);
    }

}
